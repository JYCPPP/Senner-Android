package com.example.senner.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.List;

public class ObjectDetectorHelper {

    public float threshold = 0.5f;
    public int numThreads = 2;
    public int maxResults = 3;
    public int currentDelegate = 0;
    public int currentModel = 0;
    private Context context;
    private DetectorListener objectDetectorListener;
    private ObjectDetector objectDetector = null;

    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_GPU = 1;
    public static final int DELEGATE_NNAPI = 2;

    public static final int MODEL_MOBILENETV1 = 0;
    public static final int MODEL_EFFICIENTDETV0 = 1;
    public static final int MODEL_EFFICIENTDETV1 = 2;
    public static final int MODEL_EFFICIENTDETV2 = 3;
    public static final int MODEL_ROAD = 4;

    public ObjectDetectorHelper(float threshold, int numThreads, int maxResults, int currentDelegate,
                                int currentModel, Context context, DetectorListener objectDetectorListener) {
        this.threshold = threshold;
        this.numThreads = numThreads;
        this.maxResults = maxResults;
        this.currentDelegate = currentDelegate;
        this.currentModel = currentModel;
        this.context = context;
        this.objectDetectorListener = objectDetectorListener;
        setupObjectDetector();
    }

    public void clearObjectDetector() {
        objectDetector = null;
    }

    public void setupObjectDetector() {
        ObjectDetector.ObjectDetectorOptions.Builder optionsBuilder =
                ObjectDetector.ObjectDetectorOptions.builder()
                        .setScoreThreshold(threshold)
                        .setMaxResults(maxResults);

        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads);

        switch (currentDelegate) {
            case DELEGATE_CPU:
                // Default
                break;
            case DELEGATE_GPU:
                if (new CompatibilityList().isDelegateSupportedOnThisDevice()) {
                    baseOptionsBuilder.useGpu();
                } else {
                    objectDetectorListener.onError("GPU is not supported on this device");
                }
                break;
            case DELEGATE_NNAPI:
                baseOptionsBuilder.useNnapi();
                break;
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build());

        String modelName = "";
        switch (currentModel) {
            case MODEL_MOBILENETV1:
                modelName = "mobilenetv1.tflite";
                break;
            case MODEL_EFFICIENTDETV0:
                modelName = "efficientdet-lite0.tflite";
                break;
            case MODEL_EFFICIENTDETV1:
                modelName = "efficientdet-lite1.tflite";
                break;
            case MODEL_EFFICIENTDETV2:
                modelName = "efficientdet-lite2.tflite";
                break;
            default:
                modelName = "mobilenetv1.tflite";
                break;
        }

        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(context, modelName, optionsBuilder.build());
        } catch (IllegalStateException | IOException e) {
            if (objectDetectorListener != null) {
                objectDetectorListener.onError("Object detector failed to initialize. See error logs for details");
            }
            Log.e("Test", "TFLite failed to load model with error: " + e.getMessage());
        }
    }

    public void detect(Bitmap image, int imageRotation) {

        if (objectDetector == null) {
            setupObjectDetector();
        }

        if (objectDetector != null) { // add null check
            long inferenceTime = SystemClock.uptimeMillis();

            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new Rot90Op(-imageRotation / 90))
                    .build();

            TensorImage tensorImage = imageProcessor.process(TensorImage.fromBitmap(image));

            if (tensorImage != null && objectDetector != null) {
                List<Detection> results = objectDetector.detect(tensorImage);
                inferenceTime = SystemClock.uptimeMillis() - inferenceTime;
                if (objectDetectorListener != null) {
                    objectDetectorListener.onResults(
                            results,
                            inferenceTime,
                            tensorImage.getHeight(),
                            tensorImage.getWidth()
                    );
                }
            }
        }
    }

    public interface DetectorListener {
        void onError(String error);
        void onResults(List<Detection> results, long inferenceTime, int imageHeight, int imageWidth);
    }



}

