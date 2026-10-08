package com.jasonhong.yoyu.core.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.jasonhong.yoyu.R;

/**
 * Pixel-perfect reproduction of Flutter's Graphic Track column:
 * Fixed 30dp width container, start-aligned (left=0).
 * - Transit mode: Top hollow circle (8dp, 2dp stroke), vertical line (2dp wide, 30% alpha), bottom solid circle (8dp).
 * - Retail mode: Single solid circle (8dp) centered vertically.
 * Leaves 22dp clean breathing room to station texts.
 */
public class TransitTrackView extends View {

    public static final int MODE_TRANSIT = 0;
    public static final int MODE_RETAIL = 1;

    private int mode = MODE_TRANSIT;

    private final Paint solidDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hollowDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float dotSizePx;
    private float strokeWidthPx;
    private float lineWidthPx;

    public TransitTrackView(Context context) {
        super(context);
        init(context);
    }

    public TransitTrackView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TransitTrackView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        dotSizePx = dpToPx(8f);
        strokeWidthPx = dpToPx(2f);
        lineWidthPx = dpToPx(2f);

        int primaryColor = ContextCompat.getColor(context, R.color.primary);
        int trackLineColor = ContextCompat.getColor(context, R.color.primary_track);

        solidDotPaint.setStyle(Paint.Style.FILL);
        solidDotPaint.setColor(primaryColor);

        hollowDotPaint.setStyle(Paint.Style.STROKE);
        hollowDotPaint.setStrokeWidth(strokeWidthPx);
        hollowDotPaint.setColor(primaryColor);

        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(trackLineColor);
    }

    public void setMode(int mode) {
        this.mode = mode;
        invalidate();
    }

    public void setPrimaryColor(int color, int lineColor) {
        solidDotPaint.setColor(color);
        hollowDotPaint.setColor(color);
        linePaint.setColor(lineColor);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) Math.ceil(dpToPx(30f));
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = dotSizePx / 2f;
        float centerX = radius; // Start-aligned at left (x = 0 to 8dp)

        if (mode == MODE_TRANSIT) {
            float height = getHeight();
            if (height < dotSizePx * 2) return;

            // 1. Top hollow circle (center at radius, radius)
            float topCenterY = radius;
            // Stroke is centered on radius - halfStroke to stay within dotSize
            float hollowRadius = radius - (strokeWidthPx / 2f);
            canvas.drawCircle(centerX, topCenterY, hollowRadius, hollowDotPaint);

            // 2. Middle line (from bottom of top circle to top of bottom circle)
            float lineTop = dotSizePx;
            float lineBottom = height - dotSizePx;
            if (lineBottom > lineTop) {
                float lineLeft = centerX - (lineWidthPx / 2f);
                float lineRight = lineLeft + lineWidthPx;
                canvas.drawRect(lineLeft, lineTop, lineRight, lineBottom, linePaint);
            }

            // 3. Bottom solid circle (center at radius, height - radius)
            float bottomCenterY = height - radius;
            canvas.drawCircle(centerX, bottomCenterY, radius, solidDotPaint);

        } else {
            // MODE_RETAIL: Single dot vertically centered
            float centerY = getHeight() / 2f;
            canvas.drawCircle(centerX, centerY, radius, solidDotPaint);
        }
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
