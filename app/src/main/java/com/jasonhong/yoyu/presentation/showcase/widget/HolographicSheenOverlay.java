package com.jasonhong.yoyu.presentation.showcase.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Overlay view that renders dynamic specular glare and holographic iridescent foil effects
 * driven by 3D tilt angles.
 */
public class HolographicSheenOverlay extends View {

    private final Paint glarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint holoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF boundsF = new RectF();
    private final Path clipPath = new Path();
    private float cornerRadius = 16f;

    private float tiltX = 0f; // -1.0 to 1.0 (corresponds to roll / rotationY)
    private float tiltY = 0f; // -1.0 to 1.0 (corresponds to pitch / rotationX)

    private LinearGradient glareShader;
    private LinearGradient holoShader;
    private final Matrix glareMatrix = new Matrix();
    private final Matrix holoMatrix = new Matrix();

    // Iridescent foil colors: Gold, Magenta, Cyan, Lime, Violet, Gold
    private static final int[] HOLO_COLORS = new int[]{
            0x00FFD700, // transparent gold
            0x3500FFFF, // cyan
            0x45FF00FF, // magenta
            0x4000FF7F, // spring green
            0x35FFD700, // gold
            0x0000FFFF  // transparent cyan
    };
    private static final float[] HOLO_POSITIONS = new float[]{0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f};

    public HolographicSheenOverlay(Context context) {
        super(context);
        init(context);
    }

    public HolographicSheenOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        cornerRadius = 16f * density;

        // Glare paint: Specular white reflection band
        glarePaint.setStyle(Paint.Style.FILL);
        glarePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));

        // Holo paint: Iridescent rainbow foil
        holoPaint.setStyle(Paint.Style.FILL);
        holoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));

        // Border edge reflection paint
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.2f * density);
        borderPaint.setColor(0x30FFFFFF);
    }

    public void setCornerRadius(float cornerRadiusPx) {
        this.cornerRadius = cornerRadiusPx;
        updateClipPath();
        invalidate();
    }

    public void setTilt(float normX, float normY) {
        this.tiltX = normX;
        this.tiltY = normY;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        boundsF.set(0, 0, w, h);
        updateClipPath();

        if (w > 0 && h > 0) {
            // Diagonal specular sheen
            float diag = (float) Math.hypot(w, h);
            glareShader = new LinearGradient(
                    0, 0, diag * 0.7f, diag * 0.7f,
                    new int[]{0x00FFFFFF, 0x1AFFFFFF, 0x75FFFFFF, 0x1AFFFFFF, 0x00FFFFFF},
                    new float[]{0.0f, 0.35f, 0.5f, 0.65f, 1.0f},
                    Shader.TileMode.CLAMP
            );
            glarePaint.setShader(glareShader);

            // Holo gradient
            holoShader = new LinearGradient(
                    0, 0, w * 1.2f, h * 1.2f,
                    HOLO_COLORS,
                    HOLO_POSITIONS,
                    Shader.TileMode.MIRROR
            );
            holoPaint.setShader(holoShader);
        }
    }

    private void updateClipPath() {
        clipPath.reset();
        clipPath.addRoundRect(boundsF, cornerRadius, cornerRadius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.save();
        canvas.clipPath(clipPath);

        // 1. Draw Holo Foil Shader (shifts dynamically with tilt angle)
        if (holoShader != null) {
            float holoOffsetX = tiltX * w * 0.9f;
            float holoOffsetY = -tiltY * h * 0.9f;
            holoMatrix.reset();
            holoMatrix.setTranslate(holoOffsetX, holoOffsetY);
            holoShader.setLocalMatrix(holoMatrix);

            // Intensity increases slightly as tilt increases
            float tiltMag = (float) Math.hypot(tiltX, tiltY);
            holoPaint.setAlpha((int) (Math.min(1.0f, tiltMag * 1.2f) * 255));
            canvas.drawRect(boundsF, holoPaint);
        }

        // 2. Draw Specular Glare Band
        if (glareShader != null) {
            float glareCenterX = (w * 0.5f) + (tiltX * w * 0.7f);
            float glareCenterY = (h * 0.5f) - (tiltY * h * 0.7f);

            glareMatrix.reset();
            glareMatrix.setTranslate(glareCenterX - w * 0.35f, glareCenterY - h * 0.35f);
            glareShader.setLocalMatrix(glareMatrix);

            canvas.drawRect(boundsF, glarePaint);
        }

        // 3. Draw Beveled Edge Highlight
        canvas.drawRoundRect(boundsF, cornerRadius, cornerRadius, borderPaint);

        canvas.restore();
    }
}
