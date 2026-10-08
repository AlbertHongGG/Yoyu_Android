package com.jasonhong.yoyu.core.widgets.skeleton;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Pure native Cupertino/Flutter-style breathing skeleton drawable matching Yoyu's ShimmerImageCard.
 * Interpolates color between 5% and 15% black in light mode, and 10% and 24% white in dark mode.
 */
public class SkeletonPulseDrawable extends Drawable implements Animatable, SkeletonPulseClock.PulseListener {

    private static final int LIGHT_BEGIN_COLOR = 0x0D000000; // 5% black (Colors.black.withValues(alpha: 0.05))
    private static final int LIGHT_END_COLOR = 0x26000000;   // 15% black (Colors.black.withValues(alpha: 0.15))

    private static final int DARK_BEGIN_COLOR = 0x1AFFFFFF;  // 10% white (Colors.white10)
    private static final int DARK_END_COLOR = 0x3DFFFFFF;    // 24% white (Colors.white24)

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF boundsF = new RectF();
    private float cornerRadiusPx = 0f;

    private final int beginColor;
    private final int endColor;
    private boolean isRunning = false;

    public SkeletonPulseDrawable(Context context) {
        this(context, 16f); // Default 16dp corner radius matching Flutter BorderRadius.circular(16)
    }

    public SkeletonPulseDrawable(Context context, float cornerRadiusDp) {
        boolean isDark = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        this.beginColor = isDark ? DARK_BEGIN_COLOR : LIGHT_BEGIN_COLOR;
        this.endColor = isDark ? DARK_END_COLOR : LIGHT_END_COLOR;

        float density = context.getResources().getDisplayMetrics().density;
        this.cornerRadiusPx = cornerRadiusDp * density;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(beginColor);
        start();
    }

    public void setCornerRadius(float cornerRadiusPx) {
        this.cornerRadiusPx = cornerRadiusPx;
        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        boundsF.set(bounds);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (boundsF.isEmpty()) {
            boundsF.set(getBounds());
        }
        if (cornerRadiusPx > 0) {
            canvas.drawRoundRect(boundsF, cornerRadiusPx, cornerRadiusPx, paint);
        } else {
            canvas.drawRect(boundsF, paint);
        }
    }

    @Override
    public void onPulseUpdate(float fraction) {
        int color = evaluateArgb(fraction, beginColor, endColor);
        paint.setColor(color);
        invalidateSelf();
    }

    private int evaluateArgb(float fraction, int startColor, int endColor) {
        int startA = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endA = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        int a = (int) (startA + (endA - startA) * fraction);
        int r = (int) (startR + (endR - startR) * fraction);
        int g = (int) (startG + (endG - startG) * fraction);
        int b = (int) (startB + (endB - startB) * fraction);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (visible) {
            start();
        } else {
            stop();
        }
        return changed;
    }

    @Override
    public void start() {
        if (!isRunning) {
            isRunning = true;
            SkeletonPulseClock.get().register(this);
        }
    }

    @Override
    public void stop() {
        if (isRunning) {
            isRunning = false;
            SkeletonPulseClock.get().unregister(this);
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
