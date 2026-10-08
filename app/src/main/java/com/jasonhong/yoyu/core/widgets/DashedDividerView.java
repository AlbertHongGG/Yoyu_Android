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
 * Pixel-perfect reproduction of Flutter's DashedDivider:
 * Height: 1dp, dashWidth: 5dp, color: Colors.black12 (#1F000000), spaceBetween layout.
 * Draws via pure hardware-accelerated canvas rectangles.
 */
public class DashedDividerView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float dashWidthPx;
    private float strokeHeightPx;

    public DashedDividerView(Context context) {
        super(context);
        init(context, null);
    }

    public DashedDividerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DashedDividerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        dashWidthPx = dpToPx(5f);
        strokeHeightPx = dpToPx(1f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.tx_divider));
    }

    public void setDividerColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (int) Math.ceil(strokeHeightPx);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        if (width <= 0 || dashWidthPx <= 0) return;

        int dashCount = (int) Math.floor(width / (2 * dashWidthPx));
        if (dashCount <= 0) return;

        float gap = dashCount > 1
                ? (width - (dashCount * dashWidthPx)) / (dashCount - 1)
                : 0f;

        float top = (getHeight() - strokeHeightPx) / 2f;
        float bottom = top + strokeHeightPx;

        float currentX = 0f;
        for (int i = 0; i < dashCount; i++) {
            canvas.drawRect(currentX, top, currentX + dashWidthPx, bottom, paint);
            currentX += dashWidthPx + gap;
        }
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
