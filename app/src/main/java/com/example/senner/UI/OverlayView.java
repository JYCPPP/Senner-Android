package com.example.senner.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.senner.R;

import org.tensorflow.lite.task.vision.detector.Detection;

import java.util.LinkedList;
import java.util.List;

public class OverlayView extends View {

    private List<Detection> results = new LinkedList<>();
    private Paint boxPaint;
    private Paint textBackgroundPaint;
    private Paint textPaint;
    private float scaleFactor = 1f;
    private Rect bounds = new Rect();

    private static final int BOUNDING_RECT_TEXT_PADDING = 8;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(Color.BLACK);
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setTextSize(50f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50f);

        boxPaint = new Paint();
        boxPaint.setColor(ContextCompat.getColor(getContext(), R.color.red));
        boxPaint.setStrokeWidth(8F);
        boxPaint.setStyle(Paint.Style.STROKE);
    }

    public void clear() {
        textPaint.reset();
        textBackgroundPaint.reset();
        boxPaint.reset();
        invalidate();
        initPaints();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        for (Detection result : results) {
            RectF boundingBox = result.getBoundingBox();

            float top = boundingBox.top * scaleFactor;
            float bottom = boundingBox.bottom * scaleFactor;
            float left = boundingBox.left * scaleFactor;
            float right = boundingBox.right * scaleFactor;

            RectF drawableRect = new RectF(left, top, right, bottom);
            canvas.drawRect(drawableRect, boxPaint);

            @SuppressLint("DefaultLocale") String drawableText = result.getCategories().get(0).getLabel() + " " +
                    String.format("%.2f", result.getCategories().get(0).getScore());

            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length(), bounds);
            int textWidth = bounds.width();
            int textHeight = bounds.height();
            canvas.drawRect(left, top,
                    left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                    top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint);

            canvas.drawText(drawableText, left, top + bounds.height(), textPaint);
        }
    }

    public void setResults(List<Detection> detectionResults, int imageHeight, int imageWidth) {
        results = detectionResults;
        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.
        scaleFactor = Math.max(getWidth() * 1f / imageWidth, getHeight() * 1f / imageHeight);
    }



}
