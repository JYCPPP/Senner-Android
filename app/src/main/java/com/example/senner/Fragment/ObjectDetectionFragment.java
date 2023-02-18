package com.example.senner.Fragment;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.senner.Helper.ObjectDetectorHelper;
import com.example.senner.R;
import com.example.senner.UI.OverlayView;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.task.vision.detector.Detection;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ObjectDetectionFragment extends Fragment implements ObjectDetectorHelper.DetectorListener {

    private ObjectDetectorHelper objectDetectorHelper;
    private Bitmap bitmapBuffer;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private LocationManager locationManager;
    private String locationmessage;


    //View
    private View view;
    private TextView inferenceTimeVal, maxResultsValue, thresholdValue, threadsValue;
    private ImageButton thresholdMinus, thresholdPlus, maxResultsMinus, maxResultsPlus, threadsMinus, threadsPlus;
    private AppCompatSpinner spinnerDelegate, spinnerModel;
    private OverlayView overlayView;
    private PreviewView previewView;
    private TextView locationInfo;

    //Menu
    private float posY, curPosY;
    private boolean isSheetOpen = false;
    private Animation animation;
    private FrameLayout menu;
    private ImageView arrow;

    //Controls: avoid click too fast
    private boolean controlsEnabled = true;

    /**
     * Blocking camera operations are performed using this executor
     */
    private ExecutorService cameraExecutor;


    //使用Handler监听位置信息
    private Handler handler = new Handler(msg -> {
        if ( msg.what == 0x001 ) {
            locationInfo.setText(locationmessage);
        }

        return false;
    });
    //准备位置信息管理器
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 当GPS定位信息发生改变时，更新定位
            //updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

            // 如果没权限，打开设置页面让用户自己设置
            if (ContextCompat.checkSelfPermission(requireActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireActivity(), ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                // 当GPS LocationProvider可用时，更新定位
                //updateLocation(locationManager.getLastKnownLocation(provider));
            }
            else{
                ActivityCompat.requestPermissions(requireActivity(), new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 0);
            }


        }

        @Override
        public void onProviderDisabled(String provider) {
            //updateLocation(null);
        }
    };



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_object_detection, container, false);

        Init();
        return view;
    }

    private void Init() {

        //Bind views
        inferenceTimeVal = view.findViewById(R.id.inference_time_val);
        maxResultsMinus = view.findViewById(R.id.max_results_minus);
        maxResultsValue = view.findViewById(R.id.max_results_value);
        maxResultsPlus = view.findViewById(R.id.max_results_plus);
        thresholdMinus = view.findViewById(R.id.threshold_minus);
        thresholdValue = view.findViewById(R.id.threshold_value);
        thresholdPlus = view.findViewById(R.id.threshold_plus);
        threadsMinus = view.findViewById(R.id.threads_minus);
        threadsValue = view.findViewById(R.id.threads_value);
        threadsPlus = view.findViewById(R.id.threads_plus);
        spinnerDelegate = view.findViewById(R.id.spinner_delegate);
        spinnerModel = view.findViewById(R.id.spinner_model);
        overlayView = view.findViewById(R.id.overlay);
        previewView = view.findViewById(R.id.preview);
        menu = view.findViewById(R.id.menu);
        arrow = view.findViewById(R.id.arrow);
        locationInfo = view.findViewById(R.id.location);

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        objectDetectorHelper = new ObjectDetectorHelper(
    0.5f, 2, 3, 0, 0, requireContext(), this);

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Wait for the views to be properly laid out
        // Set up the camera and its use cases
        previewView.post(this::setUpCamera);

        // Attach listeners to UI control widgets
        initBottomSheetControls();

        //Bind menu
        setMenuView();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setMenuView() {

        //先在总视图上设置滑动事件
        //在子线程进行节约资源
        Thread thread = new Thread(() -> view.setOnTouchListener((v, event) -> {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    posY = event.getRawY();//得到落点相对于左上角的坐标
                    break;
                case MotionEvent.ACTION_MOVE:
                    curPosY = event.getRawY();//得到终点相对于左上角的坐标
                    break;
                case MotionEvent.ACTION_UP:

                    if ((curPosY - posY > 0) && (Math.abs(curPosY - posY) > 25)){//下降

                        if(!isSheetOpen) {
                            animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.view_down);
                            menu.setVisibility(View.VISIBLE);
                            menu.startAnimation(animation);
                            menu.setClickable(true);
                            isSheetOpen = true;
                            arrow.setImageResource(R.drawable.round_arrow_up_48dp);



                            //再监听Sheet上的滑动事件
                            menu.setOnTouchListener((sheet, slide) -> {
                                switch (slide.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        posY = slide.getRawY();//得到落点相对于左上角的坐标
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        curPosY = slide.getRawY();//得到终点相对于左上角的坐标
                                        break;
                                    case MotionEvent.ACTION_UP:

                                        if ((curPosY - posY < 0) && (Math.abs(curPosY - posY) > 25)){//上升

                                            if(isSheetOpen) {
                                                animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.view_up);
                                                menu.startAnimation(animation);
                                                menu.setVisibility(View.GONE);
                                                menu.setClickable(false);
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
        }));
        thread.start();
    }

    private void initBottomSheetControls() {
        // When clicked, lower detection score threshold floor
        thresholdMinus.setOnClickListener(v -> {
            if (controlsEnabled && objectDetectorHelper.threshold >= 0.1) {
                controlsEnabled = false;
                objectDetectorHelper.threshold -= 0.1f;
                updateControlsUi();
                controlsEnabled = true;
            }
        });

        // When clicked, raise detection score threshold floor
        thresholdPlus.setOnClickListener(v -> {
            if (controlsEnabled && objectDetectorHelper.threshold <= 0.8) {
                controlsEnabled = false;
                objectDetectorHelper.threshold += 0.1f;
                updateControlsUi();
                controlsEnabled = true;
            }
        });

        // When clicked, reduce the number of objects that can be detected at a time
        maxResultsMinus.setOnClickListener(v -> {
            if (controlsEnabled && objectDetectorHelper.maxResults > 1) {
                controlsEnabled = false;
                objectDetectorHelper.maxResults -= 1;
                updateControlsUi();
                controlsEnabled = true;
            }
        });

        // When clicked, increase the number of objects that can be detected at a time
        maxResultsPlus.setOnClickListener(v -> {
            if (controlsEnabled && objectDetectorHelper.maxResults < 5) {
                controlsEnabled = false;
                objectDetectorHelper.maxResults += 1;
                updateControlsUi();
                controlsEnabled = true;
            }
        });

        threadsMinus.setOnClickListener(v -> {
            if (controlsEnabled && objectDetectorHelper.numThreads > 1) {
                controlsEnabled = false;
                objectDetectorHelper.numThreads -= 1;
                updateControlsUi();
                controlsEnabled = true;
            }
        });

        threadsPlus.setOnClickListener(v -> {
            if (objectDetectorHelper.numThreads < 4) {
                controlsEnabled = false;
                objectDetectorHelper.numThreads += 1;
                updateControlsUi();
                controlsEnabled = true;
            }
        });

        spinnerDelegate.setSelection(0, false);
        spinnerDelegate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(controlsEnabled) {
                    controlsEnabled = false;
                    objectDetectorHelper.currentDelegate = position;
                    updateControlsUi();
                    controlsEnabled = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        spinnerModel.setSelection(0, false);
        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(controlsEnabled) {
                    controlsEnabled = false;
                    objectDetectorHelper.currentModel = position;
                    updateControlsUi();
                    controlsEnabled = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
        // Update the values displayed in the bottom sheet. Reset detector.
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        private void updateControlsUi() {

            controlsEnabled = false;
            maxResultsValue.setText(Integer.toString(objectDetectorHelper.maxResults));
            thresholdValue.setText(String.format("%.2f", objectDetectorHelper.threshold));
            threadsValue.setText(Integer.toString(objectDetectorHelper.numThreads));

            // Needs to be cleared instead of reinitialized because the GPU
            // delegate needs to be initialized on the thread using it when applicable
            objectDetectorHelper.clearObjectDetector();
            overlayView.clear();
            controlsEnabled = true;

        }

        // Initialize CameraX, and prepare to bind the camera use cases
        private void setUpCamera() {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
            cameraProviderFuture.addListener(() -> {
                // CameraProvider
                try {
                    cameraProvider = cameraProviderFuture.get();

                    // Build and bind the camera use cases
                    bindCameraUseCases();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }, ContextCompat.getMainExecutor(requireContext()));
        }

        // Declare and bind preview, capture and analysis use cases
        @SuppressLint("UnsafeOptInUsageError")
        private void bindCameraUseCases() {
            // CameraProvider
            ProcessCameraProvider cameraProvider = this.cameraProvider;
            if (cameraProvider == null) {
                throw new IllegalStateException("Camera initialization failed.");
            }

            // CameraSelector - makes assumption that we're only using the back camera
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            // Preview. Only using the 4:3 ratio because this is the closest to our models
            preview = new Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(previewView.getDisplay().getRotation())
                    .build();

            // ImageAnalysis. Using RGBA 8888 to match how our models work
            imageAnalyzer = new ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(previewView.getDisplay().getRotation())
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build();

            // The analyzer can then be assigned to the instance
            imageAnalyzer.setAnalyzer(cameraExecutor, image -> {
                if (bitmapBuffer == null) {
                    // The image rotation and RGB image buffer are initialized only once
                    // the analyzer has started running
                    bitmapBuffer = Bitmap.createBitmap(
                            image.getWidth(),
                            image.getHeight(),
                            Bitmap.Config.ARGB_8888
                    );
                }

                detectObjects(image);
            });

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll();

            try {
                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer);

                // Attach the viewfinder's surface provider to preview use case

                if(preview != null) {
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                }

            } catch (Exception e) {
                Log.e("Exception", String.valueOf(e));
            }

    }

    private void detectObjects(ImageProxy image) {

        try (ImageProxy imageProxy = image) {
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
        }

        int imageRotation = image.getImageInfo().getRotationDegrees();
        // Pass Bitmap and rotation to the object detector helper for processing and detection

        if (objectDetectorHelper != null) {
            objectDetectorHelper.detect(bitmapBuffer, imageRotation);
        }  // handle the case where objectDetectorHelper is null


    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (imageAnalyzer != null) {
            imageAnalyzer.setTargetRotation(previewView.getDisplay().getRotation());
        }
    }

    // Update UI after objects have been detected. Extracts original image height/width
    // to scale and place bounding boxes properly through OverlayView
    @SuppressLint("DefaultLocale")
    @Override
    public void onResults(List<Detection> results, long inferenceTime, int imageHeight, int imageWidth) {

        requireActivity().runOnUiThread(() -> {
            inferenceTimeVal.setText(String.format("%d ms", inferenceTime));

            // Pass necessary information to OverlayView for drawing on the canvas
            overlayView.setResults(results != null ? results : new LinkedList<>(),
                    imageHeight,
                    imageWidth);

            // Force a redraw
            overlayView.invalidate();
        });
    }

    @Override
    public void onError(@NonNull String error) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    //Switch back will do this:
    @Override
    public void onResume() {

        super.onResume();

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, do your work here
            // Make sure that all permissions are still present, since the
            // user could have removed them while the app was in paused state.
            // Initialize our background executor
            cameraExecutor = Executors.newSingleThreadExecutor();
            // Wait for the views to be properly laid out
            // Set up the camera and its use cases
            overlayView.invalidate();
            previewView.post(this::setUpCamera);

            // Attach listeners to UI control widgets
            initBottomSheetControls();

            //Bind menu
            setMenuView();
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 0);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Shut down our background executor
        cameraExecutor.shutdown();
        imageAnalyzer.clearAnalyzer();
        overlayView.clear();
    }

    //Switch will do this:
    @Override
    public void onPause() {
        super.onPause();

        // Shut down our background executor
        cameraExecutor.shutdown();
        imageAnalyzer.clearAnalyzer();
        overlayView.clear();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

}
