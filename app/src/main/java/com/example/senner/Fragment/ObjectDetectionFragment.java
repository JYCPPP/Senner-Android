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
import android.provider.Settings;
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
import com.example.senner.Helper.Yolov5TFLiteDetector;
import com.example.senner.R;
import com.example.senner.UI.ChartView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.listener.OnDrawListener;

import java.util.ArrayList;
import java.util.List;

public final class ObjectDetectionFragment extends Fragment  {

    private PreviewView cameraPreviewMatch;
    private ImageView boxLabelCanvas;
    private Spinner modelSpinner;
    private TextView inferenceTimeTextView;
    private TextView frameSizeTextView;
    private Yolov5TFLiteDetector yolov5TFLiteDetector;
    private ImageAnalyse imageAnalyse;
    private int rotation;

    private CameraProcess cameraProcess = new CameraProcess();

    //Menu
    private float posY, curPosY;
    private boolean isSheetOpen = false;
    private Animation animation;
    private FrameLayout menu;
    private ImageView arrow;
    private View rootView;

    //位移相关
    private FrameLayout camera;
    private Button recordButton;
    private boolean IsClickRecording = false;
    private LineChart disChart;
    private ChartView chartView = new ChartView();




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_object_detection, container, false);

        Init(rootView);
        return rootView;
    }

    private void Init(View view) {


        //设置
        menu = view.findViewById(R.id.menu);
        arrow = view.findViewById(R.id.arrow);
        // 全屏画面
        cameraPreviewMatch = view.findViewById(R.id.camera_preview_match);
        cameraPreviewMatch.setScaleType(PreviewView.ScaleType.FILL_START);

        // box/label画面
        boxLabelCanvas = view.findViewById(R.id.box_label_canvas);

        // 下拉按钮
        modelSpinner = view.findViewById(R.id.model);

        // 实时更新的一些view
        inferenceTimeTextView = view.findViewById(R.id.inference_time);
        frameSizeTextView = view.findViewById(R.id.frame_size);

        // 获取手机摄像头拍照旋转参数
        rotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        Log.i("image", "rotation: " + rotation);

        cameraProcess.showCameraSupportSize(requireActivity());

        // 初始化加载yolov5s
        initModel("target");

        // 监听模型切换按钮
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String model = (String) adapterView.getItemAtPosition(i);
                initModel(model);
                    imageAnalyse = new ImageAnalyse(requireActivity(),
                            cameraPreviewMatch,
                            boxLabelCanvas,
                            rotation,
                            inferenceTimeTextView,
                            frameSizeTextView,
                            yolov5TFLiteDetector);
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
        LegendEntry legendEntry_locY = new LegendEntry("LocationY(mm)", Legend.LegendForm.LINE, 12f, 2f, null, Color.BLUE);
        List<LegendEntry> entries_location = new ArrayList<>();
        entries_location.add(legendEntry_locX);
        entries_location.add(legendEntry_locY);
        chartView.InitChartView(disChart, 5, 0, entries_location);

    }

    /**
     * 加载模型
     *
     * @param modelName
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

        //Bind menu
        setMenuView();
        //Bind record
        SetRecordEvent();

    }

    /**
     * 点击按钮开始记录位移，需要依次处理的事件是：振动、更改图标、记录开始点击时间、记录完毕绘制图表
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void SetRecordEvent() {

        recordButton.setOnClickListener(v -> {

            // 获取系统的Vibrator服务
            Vibrator vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

            // 检查是否支持震动
            if (vibrator.hasVibrator()) {

                VibrationEffect vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE);
                // 让手机震动
                vibrator.vibrate(vibrationEffect);
            }

            if(!IsClickRecording){
                //保证绘制完毕再清除
                imageAnalyse.clearLocation();
                IsClickRecording = true;
                recordButton.setBackground(requireActivity().getDrawable(R.drawable.twotone_stop_36dp));
                imageAnalyse.record = System.currentTimeMillis();
                imageAnalyse.IsRecording = true;

            }else {
                IsClickRecording = false;
                recordButton.setBackground(requireActivity().getDrawable(R.drawable.twotone_record_36dp));
                imageAnalyse.IsRecording = false;
                ShowDisLineChart();
            }
        });

    }

    /**
     *依次实现以下功能：拿到imageAnalyse记录的框位移数据、将数据赋给表格、清除所有历史数据
     */
    private void ShowDisLineChart() {

        chartView.SetDisplacementChartData(disChart, imageAnalyse.getLocationX(), imageAnalyse.getLocationY() , "Pixel LocationX", "Pixel LocationY", Color.RED, Color.BLUE, true);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setMenuView() {
        //主线程执行避免阻塞
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            camera.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        posY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        curPosY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if ((curPosY - posY > 0) && (Math.abs(curPosY - posY) > 25)) {
                            if (!isSheetOpen) {
                                animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.view_down);
                                //设置背景隐藏
                                camera.setClickable(false);
                                camera.setFocusable(false);
                                camera.setVisibility(View.INVISIBLE);
                                //设置菜单可视
                                menu.setVisibility(View.VISIBLE);
                                menu.startAnimation(animation);
                                menu.setFocusable(true);
                                menu.setClickable(true);
                                isSheetOpen = true;
                                arrow.setImageResource(R.drawable.round_arrow_up_48dp);

                                menu.setOnTouchListener((sheet, slide) -> {
                                    switch (slide.getAction()) {
                                        case MotionEvent.ACTION_DOWN:
                                            posY = slide.getRawY();
                                            break;
                                        case MotionEvent.ACTION_MOVE:
                                            curPosY = slide.getRawY();
                                            break;
                                        case MotionEvent.ACTION_UP:
                                            if ((curPosY - posY < 0) && (Math.abs(curPosY - posY) > 25)) {
                                                if (isSheetOpen) {
                                                    animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.view_up);
                                                    //设置背景显示
                                                    camera.setClickable(true);
                                                    camera.setFocusable(true);
                                                    camera.setVisibility(View.VISIBLE);
                                                    //设置菜单隐藏
                                                    menu.startAnimation(animation);
                                                    menu.setVisibility(View.INVISIBLE);
                                                    menu.setClickable(false);
                                                    menu.setFocusable(false);
                                                    arrow.setImageResource(R.drawable.round_arrow_down_48dp);
                                                    isSheetOpen = false;
                                                }
                                            }
                                            break;
                                    }
                                    return true;
                                });
                            }
                        }
                        break;
                }
                return true;
            });
        });
    }


    //Switch back will do this:
    @Override
    public void onResume() {

        super.onResume();

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            imageAnalyse = new ImageAnalyse(requireActivity(),
                    cameraPreviewMatch,
                    boxLabelCanvas,
                    rotation,
                    inferenceTimeTextView,
                    frameSizeTextView,
                    yolov5TFLiteDetector);

            if (isVisible() && imageAnalyse != null && cameraPreviewMatch != null) {
                cameraProcess.startCamera(requireActivity(), imageAnalyse, cameraPreviewMatch);
                setMenuView();
            }
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 0);
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

}
