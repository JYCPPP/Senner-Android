package com.example.senner.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.github.mikephil.charting.data.Entry;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class ImageAnalyse implements ImageAnalysis.Analyzer {


    private static final float THRESHOLD_CONFINDENCE = 0.65F;

    public static class Result{

        public Result(long costTime, Bitmap bitmap) {
            this.costTime = costTime;
            this.bitmap = bitmap;
        }
        long costTime;
        Bitmap bitmap;
    }

    ImageView boxLabelCanvas;
    PreviewView previewView;
    int rotation;
    private TextView inferenceTimeTextView;
    private TextView frameSizeTextView;
    FirstStageProcess firstStageProcess;
    private Yolov5TFLiteDetector yolov5TFLiteDetector;

    //在保证性能的前提下，应分配初始容量
    private ArrayList<Entry> LocationX = new ArrayList<>(10000);
    private ArrayList<Entry> LocationY = new ArrayList<>(10000);
    public boolean IsRecording = false;
    public long record;
    private float TARGETSIZE = 20;
    private float histGray = 0.38F;
    private boolean isDebug = false;

    public ImageAnalyse(PreviewView previewView,
                        ImageView boxLabelCanvas,
                        int rotation,
                        TextView inferenceTimeTextView,
                        TextView frameSizeTextView,
                        Yolov5TFLiteDetector yolov5TFLiteDetector,
                        float histGray,
                        float targetSize,
                        boolean isDebug) {
        this.previewView = previewView;
        this.boxLabelCanvas = boxLabelCanvas;
        this.rotation = rotation;
        this.inferenceTimeTextView = inferenceTimeTextView;
        this.frameSizeTextView = frameSizeTextView;
        this.firstStageProcess = new FirstStageProcess();
        this.yolov5TFLiteDetector = yolov5TFLiteDetector;
        this.histGray = histGray;
        this.TARGETSIZE = targetSize;
        this.isDebug = isDebug;
    }


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void analyze(@NonNull ImageProxy image) {
        int previewHeight = previewView.getHeight();
        int previewWidth = previewView.getWidth();
        
        // 这里Observable将image analyse的逻辑放到子线程计算, 渲染UI的时候再拿回来对应的数据, 避免前端UI卡顿
        Observable.create( (ObservableEmitter<Result> emitter) -> {
                    long start = System.currentTimeMillis();
                    Log.i("image",""+previewWidth+'/'+previewHeight);

                    byte[][] yuvBytes = new byte[3][];
                    ImageProxy.PlaneProxy[] planes = image.getPlanes();
                    int imageHeight = image.getHeight();
                    int imagewWidth = image.getWidth();

                    firstStageProcess.fillBytes(planes, yuvBytes);
                    int yRowStride = planes[0].getRowStride();
                    final int uvRowStride = planes[1].getRowStride();
                    final int uvPixelStride = planes[1].getPixelStride();

                    int[] rgbBytes = new int[imageHeight * imagewWidth];
                    firstStageProcess.YUV420ToARGB8888(
                            yuvBytes[0],
                            yuvBytes[1],
                            yuvBytes[2],
                            imagewWidth,
                            imageHeight,
                            yRowStride,
                            uvRowStride,
                            uvPixelStride,
                            rgbBytes);

                    // 原图bitmap
                    Bitmap imageBitmap = Bitmap.createBitmap(imagewWidth, imageHeight, Bitmap.Config.ARGB_8888);
                    imageBitmap.setPixels(rgbBytes, 0, imagewWidth, 0, 0, imagewWidth, imageHeight);

                    // 图片适应屏幕fill_start格式的bitmap
                    double scale = Math.max(
                            previewHeight / (double) (rotation % 180 == 0 ? imagewWidth : imageHeight),
                            previewWidth / (double) (rotation % 180 == 0 ? imageHeight : imagewWidth)
                    );
                    Matrix fullScreenTransform = firstStageProcess.getTransformationMatrix(
                            imagewWidth, imageHeight,
                            (int) (scale * imageHeight), (int) (scale * imagewWidth),
                            rotation % 180 == 0 ? 90 : 0, false
                    );

                    // 适应preview的全尺寸bitmap
                    Bitmap fullImageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imagewWidth, imageHeight, fullScreenTransform, false);
                    // 裁剪出跟preview在屏幕上一样大小的bitmap
                    Bitmap cropImageBitmap = Bitmap.createBitmap(
                            fullImageBitmap, 0, 0,
                            previewWidth, previewHeight
                    );

                    // 模型输入的bitmap
                    Matrix previewToModelTransform =
                            firstStageProcess.getTransformationMatrix(
                                    cropImageBitmap.getWidth(), cropImageBitmap.getHeight(),
                                    yolov5TFLiteDetector.getInputSize().getWidth(),
                                    yolov5TFLiteDetector.getInputSize().getHeight(),
                                    0, false);
                    Bitmap firstStageBitmap = Bitmap.createBitmap(cropImageBitmap, 0, 0,
                            cropImageBitmap.getWidth(), cropImageBitmap.getHeight(),
                            previewToModelTransform, false);

                    Matrix modelToPreviewTransform = new Matrix();
                    previewToModelTransform.invert(modelToPreviewTransform);

                    // 这里进行第一阶段的处理
                    ArrayList<Recognition> recognitions = yolov5TFLiteDetector.detect(firstStageBitmap);

                    Bitmap emptyCropSizeBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
                    Canvas cropCanvas = new Canvas(emptyCropSizeBitmap);
                    // 边框画笔
                    Paint boxPaint = new Paint();
                    boxPaint.setStrokeWidth(5);
                    boxPaint.setStyle(Paint.Style.STROKE);
                    boxPaint.setColor(Color.RED);
                    // 字体画笔
                    Paint textPain = new Paint();
                    textPain.setTextSize(50);
                    textPain.setColor(Color.RED);
                    textPain.setStyle(Paint.Style.FILL);
                    // bitmap画笔
                    Paint bitmapPaint = new Paint();

                    for (Recognition res : recognitions) {

                        RectF ROI = res.getLocation();
                        // 这里进行第一阶段的处理
                        String label = res.getLabelName();
                        float confidence = res.getConfidence();
                        modelToPreviewTransform.mapRect(ROI);
                        cropCanvas.drawRect(ROI, boxPaint);
                        cropCanvas.drawText(label + ":" + String.format("%.2f", confidence), ROI.left, ROI.top, textPain);

                        if(confidence > THRESHOLD_CONFINDENCE){
                            // 在这里进行第二阶段的处理
                            // 角点的存储数据为左上、右上、左下、右下
                            List<Point> cornerPoints = new ArrayList<>();
                            cornerPoints.add(0, new Point(0, 0));
                            cornerPoints.add(1, new Point(ROI.width(), 0));
                            cornerPoints.add(2, new Point(0, ROI.height()));
                            cornerPoints.add(3, new Point(ROI.width(), ROI.height()));

                            // bitmap转化成Mat对象
                            // 使用一开始的bitmap， 因为模型输入的尺寸经过缩放
                            Bitmap secondStageBitmap = Bitmap.createBitmap(cropImageBitmap, (int)ROI.left, (int)ROI.top, (int)ROI.width(), (int)ROI.height());
                            // 提取ROI中的图像
                            SecondStageProcess secondStageProcess = new SecondStageProcess(histGray, TARGETSIZE, isDebug);
                            SecondStageProcess.SecondStageResult s = secondStageProcess.Process(secondStageBitmap, cornerPoints);

                            if(IsRecording){
                                LocationX.add(new Entry((float) (start - record), (float) (s.centerPoint.x * s.Ratio.width)));
                                LocationY.add(new Entry((float) (start - record), (float) (s.centerPoint.y * s.Ratio.height)));
                            }

                            cropCanvas.drawBitmap(s.ProcessedImage, ROI.left, ROI.top, bitmapPaint);
                        }
                    }
                    long end = System.currentTimeMillis();
                    long costTime = (end - start);
                    image.close();
                    emitter.onNext(new Result(costTime, emptyCropSizeBitmap));
                }).subscribeOn(Schedulers.io()) //这里定义被观察者,也就是上面代码的线程, 如果没定义就是主线程同步, 非异步
                //这里就是回到主线程, 观察者接受到emitter发送的数据进行处理
                .observeOn(AndroidSchedulers.mainThread())
                // 这里就是回到主线程处理子线程的回调数据.
                .subscribe((Result result) -> {
                    boxLabelCanvas.setImageBitmap(result.bitmap);
                    frameSizeTextView.setText(previewHeight + "x" + previewWidth);
                    inferenceTimeTextView.setText(result.costTime + "ms");
                });

    }
    public ArrayList<Entry> getLocationX(){return this.LocationX;}
    public ArrayList<Entry> getLocationY(){
        return this.LocationY;
    }

    public void clearLocation(){
            LocationX.clear();
            LocationY.clear();

    }


//    private float[] CaculateScale(Activity activity, int previewWidth, int previewHeight) {
//
//        //计算屏幕像素与物理尺寸的比例
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//
//        // 获取屏幕的物理尺寸，单位是英寸
//        float widthInInches = displayMetrics.widthPixels / displayMetrics.xdpi;
//        float heightInInches = displayMetrics.heightPixels / displayMetrics.ydpi;
//
//        float heightInMm = heightInInches * 25.4f; // 英寸转毫米
//        float widthInMm = widthInInches * 25.4f;
//        Log.e("Screen Size", Arrays.toString(new float[]{widthInMm, heightInMm}));
//
//        float scaleX = widthInMm / previewWidth;
//        float scaleY = heightInMm / previewHeight;
//
//        return new float[]{scaleX, scaleY};
//
//    }

}
