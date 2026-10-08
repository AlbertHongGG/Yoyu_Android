package com.jasonhong.yoyu.core.widgets;

import android.content.Context;
import android.content.res.Configuration;
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
 * Height: 1dp, dashWidth: 5dp, color: Colors.black12 / white12, spaceBetween layout.
 * Draws crisp, hardware-accelerated dashes with exact pixel snapping.
 */
public class DashedDividerView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float dashWidthPx;
    private int strokeHeightPx;

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
        strokeHeightPx = Math.max(1, Math.round(dpToPx(1f)));

        paint.setStyle(Paint.Style.FILL);
        boolean isDark = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        paint.setColor(ContextCompat.getColor(context, isDark ? R.color.tx_divider_dark : R.color.tx_divider));
    }

    public void setDividerColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = resolveSize(strokeHeightPx, heightMeasureSpec);
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0 || dashWidthPx <= 0) return;

        int dashCount = (int) Math.floor(width / (2 * dashWidthPx));
        if (dashCount <= 0) return;

        float gap = dashCount > 1
                ? (width - (dashCount * dashWidthPx)) / (dashCount - 1)
                : 0f;

        float currentX = 0f;
        for (int i = 0; i < dashCount; i++) {
            canvas.drawRect(currentX, 0f, currentX + dashWidthPx, height, paint);
            currentX += dashWidthPx + gap;
        }
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
