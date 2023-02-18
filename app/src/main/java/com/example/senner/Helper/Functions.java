package com.example.senner.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.senner.UI.ProgressBarDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Functions {

    //Main URL
    private static final String MAIN_URL = "http://c5e7190727.zicp.fun/android_login/";

    // Login URL
    public static String LOGIN_URL = MAIN_URL + "login.php";

    // Register URL
    public static String REGISTER_URL = MAIN_URL + "register.php";

    // OTP Verification
    public static String OTP_VERIFY_URL = MAIN_URL + "verification.php";

    // Forgot Password
    public static String RESET_PASS_URL = MAIN_URL + "reset-password.php";

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;


    /**
     * Function to logout user
     * Resets the temporary data stored in SQLite Database
     * */
    public void logoutUser(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
    }

    /**
     *  Email Address Validation
     */
    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\)]|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     *  Hide Keyboard
     */
    public static void hideSoftKeyboard(View view, Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                view.getWindowToken(), 0);
    }

    public static void showProgressDialog(Context context, String title) {
        FragmentManager fm = ((AppCompatActivity)context).getSupportFragmentManager();
        DialogFragment newFragment = ProgressBarDialog.newInstance(title);
        newFragment.show(fm, "dialog");
    }

    public static void hideProgressDialog(Context context) {
        FragmentManager fm = ((AppCompatActivity)context).getSupportFragmentManager();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }
    }

    /**
     * Memory-map the model file in Assets.
     */
    public static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static void softmax(final float[] vals) {
        float max = Float.NEGATIVE_INFINITY;
        for (final float val : vals) {
            max = Math.max(max, val);
        }
        float sum = 0.0f;
        for (int i = 0; i < vals.length; ++i) {
            vals[i] = (float) Math.exp(vals[i] - max);
            sum += vals[i];
        }
        for (int i = 0; i < vals.length; ++i) {
            vals[i] = vals[i] / sum;
        }
    }

    public static float expit(final float x) {
        return (float) (1. / (1. + Math.exp(-x)));
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
//            return bitmap.copy(Bitmap.Config.ARGB_8888,true);
        } catch (IOException e) {
            // handle exception
            Log.e("getBitmapFromAsset", "getBitmapFromAsset: " + e.getMessage());
        }

        return bitmap;
    }

    /**
     * Returns a transformation matrix from one reference frame into another.
     * Handles cropping (if maintaining aspect ratio is desired) and rotation.
     *
     * @param srcWidth Width of source frame.
     * @param srcHeight Height of source frame.
     * @param dstWidth Width of destination frame.
     * @param dstHeight Height of destination frame.
     * @param applyRotation Amount of rotation to apply from one frame to another.
     *  Must be a multiple of 90.
     * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
     * cropping the image if necessary.
     * @return The transformation fulfilling the desired requirements.
     */
    public static Matrix getTransformationMatrix(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation,
            final boolean maintainAspectRatio) {
        final Matrix matrix = new Matrix();

        if (applyRotation != 0) {
            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;

        final int inWidth = transpose ? srcHeight : srcWidth;
        final int inHeight = transpose ? srcWidth : srcHeight;

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            final float scaleFactorX = dstWidth / (float) inWidth;
            final float scaleFactorY = dstHeight / (float) inHeight;

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                final float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
                matrix.postScale(scaleFactor, scaleFactor);
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY);
            }
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }

    public static Bitmap processBitmap(Bitmap source, int size){

        int image_height = source.getHeight();
        int image_width = source.getWidth();

        Bitmap croppedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Matrix frameToCropTransformations = getTransformationMatrix(image_width,image_height,size,size,0,false);
        Matrix cropToFrameTransformations = new Matrix();
        frameToCropTransformations.invert(cropToFrameTransformations);

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(source, frameToCropTransformations, null);

        return croppedBitmap;
    }

    public static void writeToFile(String data, Context context) {
        try {
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            String fileName = "myFile.txt";

            File file = new File(baseDir + File.separator + fileName);

            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
