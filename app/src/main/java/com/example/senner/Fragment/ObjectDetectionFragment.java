package com.example.senner.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.senner.Helper.CameraProcess;
import com.example.senner.Helper.ImageAnalyse;
import com.example.senner.Helper.SharedPreferenceHelper;
import com.example.senner.Helper.Yolov5TFLiteDetector;
import com.example.senner.R;
import com.example.senner.UI.ChartView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public final class ObjectDetectionFragment extends Fragment  {

    private PreviewView cameraPreviewMatch;
    private ImageView boxLabelCanvas;
    private TextView inferenceTimeTextView;
    private TextView frameSizeTextView;
    private Yolov5TFLiteDetector yolov5TFLiteDetector;
    private ImageAnalyse imageAnalyse;
    private int rotation;

    private final CameraProcess cameraProcess = new CameraProcess();

    //位移相关
    private FrameLayout camera;
    private Button recordButton;
    private boolean IsClickRecording = false;
    private LineChart disChart;
    private final ChartView chartView = new ChartView();

    // 参数相关
    private SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper();
    private boolean isDebugMode;
    private float histGray;
    private float targetSize;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_object_detection, container, false);

        Init(rootView);
        return rootView;
    }

    private void Init(View view) {


        // 全图画面
        cameraPreviewMatch = view.findViewById(R.id.camera_preview_match);
        cameraPreviewMatch.setScaleType(PreviewView.ScaleType.FILL_START);

        // box/label画面
        boxLabelCanvas = view.findViewById(R.id.box_label_canvas);

        // 下拉按钮
        Spinner modelSpinner = view.findViewById(R.id.model);

        // 实时更新的一些view
        inferenceTimeTextView = view.findViewById(R.id.inference_time);
        frameSizeTextView = view.findViewById(R.id.frame_size);

        // 获取手机摄像头拍照旋转参数
        rotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();

        cameraProcess.showCameraSupportSize(requireActivity());

        // 初始化加载yolov5s
        initModel("target");

        isDebugMode = sharedPreferenceHelper.getBoolean(requireActivity(), "isObjectDetectionDebugMode", true);
        histGray = sharedPreferenceHelper.getFloat(requireActivity(), "Gray Ratio", 0.38F);
        targetSize = sharedPreferenceHelper.getFloat(requireActivity(), "Target Size", 20F);

        // 监听模型切换按钮
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String model = (String) adapterView.getItemAtPosition(i);
                initModel(model);
                    imageAnalyse = new ImageAnalyse(
                            cameraPreviewMatch,
                            boxLabelCanvas,
                            rotation,
                            inferenceTimeTextView,
                            frameSizeTextView,
                            yolov5TFLiteDetector,
                            histGray,
                            targetSize,
                            isDebugMode);
                    cameraProcess.startCamera(requireActivity(), imageAnalyse, cameraPreviewMatch);
                }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        camera = view.findViewById(R.id.camera);
        recordButton = view.findViewById(R.id.btn_record);
        //初始化折线图，设置图例
        disChart = view.findViewById(R.id.disChart);
        LegendEntry legendEntry_locX = new LegendEntry("LocationX(mm)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        LegendEntry legendEntry_locY = new LegendEntry("LocationY(mm)", Legend.LegendForm.LINE, 12f, 2f, null, Color.GREEN);
        List<LegendEntry> entries_location = new ArrayList<>();
        entries_location.add(legendEntry_locX);
        entries_location.add(legendEntry_locY);
        chartView.InitWhiteChartView(disChart, 5, 0, entries_location);

    }

    /**
     * 加载模型
     *
     */
    private void initModel(String modelName) {
        // 加载模型
        try {
            this.yolov5TFLiteDetector = new Yolov5TFLiteDetector();
            this.yolov5TFLiteDetector.setModelFile(modelName);
            //this.yolov5TFLiteDetector.addNNApiDelegate();
            this.yolov5TFLiteDetector.addGPUDelegate();
            this.yolov5TFLiteDetector.initialModel(requireActivity());
            Log.i("model", "Success loading model" + this.yolov5TFLiteDetector.getModelFile() + "Success loading label" + this.yolov5TFLiteDetector.getLabelFile() );
        } catch (Exception e) {
            Log.e("image", "load model error: " + e.getMessage() + e);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Bind record
        SetRecordEvent();

    }

    /**
     * 点击按钮开始记录位移，需要依次处理的事件是：振动、更改图标、记录开始点击时间、记录完毕绘制图表
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void SetRecordEvent() {

        recordButton.setOnClickListener(v -> {

            Vibrate(50);

            if(!IsClickRecording){
                //保证绘制完毕再清除
                imageAnalyse.clearLocation();
                IsClickRecording = true;
                recordButton.setBackground(requireActivity().getDrawable(R.drawable.round_stop_36dp));
                imageAnalyse.record = System.currentTimeMillis();
                // 先创建文件
                String ProjectPath = sharedPreferenceHelper.getString(requireActivity(),"Project Path", "");
                //时间
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 将时区设置为东八区
                Date currentDate = new Date();
                String currentTime = sdf.format(currentDate.getTime());
                imageAnalyse.DisData = CreateDataFile(ProjectPath + "/ObjectDetection Data", "/" + currentTime +" " + "Displacement.txt");
                imageAnalyse.IsRecording = true;
            }else {
                IsClickRecording = false;
                recordButton.setBackground(requireActivity().getDrawable(R.drawable.round_record_36dp));
                imageAnalyse.IsRecording = false;
                ShowDisLineChart();
            }
        });

    }

    /**
     *依次实现以下功能：拿到imageAnalyse记录的框位移数据、将数据赋给表格、清除所有历史数据
     */
    private void ShowDisLineChart() {
        chartView.SetDisplacementChartData(requireActivity(), disChart, imageAnalyse.getLocationX(), imageAnalyse.getLocationY() , "Pixel LocationX", "Pixel LocationY", Color.RED, Color.GREEN, true);

    }

//    @SuppressLint("ClickableViewAccessibility")
//    private void setMenuView() {
//        //主线程执行避免阻塞
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.post(() -> camera.setOnTouchListener((v, event) -> {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    posY = event.getRawY();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    curPosY = event.getRawY();
//                    break;
//                case MotionEvent.ACTION_UP:
//                    if ((curPosY - posY > 0) && (Math.abs(curPosY - posY) > 25)) {
//                        if (!isSheetOpen) {
//                            animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.camera_menu_down);
//                            //设置背景隐藏
//                            camera.setClickable(false);
//                            camera.setFocusable(false);
//                            camera.setVisibility(View.INVISIBLE);
//                            //设置菜单可视
//                            menu.setVisibility(View.VISIBLE);
//                            menu.startAnimation(animation);
//                            menu.setFocusable(true);
//                            menu.setClickable(true);
//                            isSheetOpen = true;
//                            arrow.setImageResource(R.drawable.round_arrow_up_48dp);
//
//                            menu.setOnTouchListener((sheet, slide) -> {
//                                switch (slide.getAction()) {
//                                    case MotionEvent.ACTION_DOWN:
//                                        posY = slide.getRawY();
//                                        break;
//                                    case MotionEvent.ACTION_MOVE:
//                                        curPosY = slide.getRawY();
//                                        break;
//                                    case MotionEvent.ACTION_UP:
//                                        if ((curPosY - posY < 0) && (Math.abs(curPosY - posY) > 25)) {
//                                            if (isSheetOpen) {
//                                                animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.camera_menu_up);
//                                                //设置背景显示
//                                                camera.setClickable(true);
//                                                camera.setFocusable(true);
//                                                camera.setVisibility(View.VISIBLE);
//                                                //设置菜单隐藏
//                                                menu.startAnimation(animation);
//                                                menu.setVisibility(View.INVISIBLE);
//                                                menu.setClickable(false);
//                                                menu.setFocusable(false);
//                                                arrow.setImageResource(R.drawable.round_arrow_down_48dp);
//                                                isSheetOpen = false;
//                                            }
//                                        }
//                                        break;
//                                }
//                                return true;
//                            });
//                        }
//                    }
//                    break;
//            }
//            return true;
//        }));
//    }


    //Switch back will do this:
    @Override
    public void onResume() {

        super.onResume();

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            imageAnalyse = new ImageAnalyse(
                    cameraPreviewMatch,
                    boxLabelCanvas,
                    rotation,
                    inferenceTimeTextView,
                    frameSizeTextView,
                    yolov5TFLiteDetector,
                    histGray,
                    targetSize,
                    isDebugMode);

            if (isVisible() && imageAnalyse != null && cameraPreviewMatch != null) {
                cameraProcess.startCamera(requireActivity(), imageAnalyse, cameraPreviewMatch);
            }
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    private void Vibrate(int amplify) {
        //触发震动事件
        Vibrator vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect effect = VibrationEffect.createOneShot(100, amplify);
        // 检查设备是否支持 VibratorEffect 对象
        if (vibrator.hasAmplitudeControl()) {
            // 设备支持 VibratorEffect 对象
            vibrator.vibrate(effect);
        } else {
            // 设备不支持 VibratorEffect 对象
            vibrator.vibrate(100);
        }
    }

    @Override
    public void onDestroyView() {
        boxLabelCanvas.setImageBitmap(null);
        super.onDestroyView();

        // Shut down our background executor

    }

    //Switch will do this:
    @Override
    public void onPause() {

        boxLabelCanvas.setImageBitmap(null);
        cameraProcess.stopCamera(requireActivity());
        super.onPause();
    }

    @Override
    public void onStop() {

        cameraProcess.stopCamera(requireActivity());
        super.onStop();

    }
    private File CreateDataFile(String filePath, String fileName) {

        File path = new File(filePath);
        if(!path.exists()){
            path.mkdirs();
        }
        //打开文件，如果不存在则创建
        File file = new File(filePath + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
