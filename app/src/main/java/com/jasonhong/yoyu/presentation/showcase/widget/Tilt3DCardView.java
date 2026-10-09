package com.jasonhong.yoyu.presentation.showcase.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

/**
 * High-performance 3D Floating Collectible Card View:
 * - Hardware-accelerated 3D rotationX/rotationY perspective projection with calibrated camera distance.
 * - Dual-input parallax tilt: Interactive finger drag tracking + smooth Gyroscope sensor orientation.
 * - Physics-based SpringAnimation for tactile return-to-center feel.
 * - Integrated dynamic holographic foil and specular glare sheen overlay.
 */
public class Tilt3DCardView extends FrameLayout {

    private static final float MAX_TILT_DEG = 22.0f;
    private static final float CAMERA_DISTANCE_DENSITY_MULT = 8000.0f;

    public interface OnTouchInteractionListener {
        void onTouchDown();
        void onTouchUp();
    }

    private CardView cardContainer;
    private ImageView ivCardFace;
    private HolographicSheenOverlay sheenOverlay;

    private boolean isUserTouching = false;
    private OnTouchInteractionListener interactionListener;

    private SpringAnimation springRotX;
    private SpringAnimation springRotY;
    private SpringAnimation springScaleX;
    private SpringAnimation springScaleY;

    public Tilt3DCardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public Tilt3DCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setClipChildren(false);
        setClipToPadding(false);

        float density = context.getResources().getDisplayMetrics().density;
        setCameraDistance(density * CAMERA_DISTANCE_DENSITY_MULT);

        // Card Container with authentic 16dp rounded corners and elevation
        cardContainer = new CardView(context);
        cardContainer.setRadius(16f * density);
        cardContainer.setCardElevation(12f * density);
        cardContainer.setMaxCardElevation(16f * density);
        cardContainer.setCardBackgroundColor(Color.WHITE);
        cardContainer.setPreventCornerOverlap(false);

        LayoutParams containerLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(cardContainer, containerLp);

        // Card Image
        ivCardFace = new ImageView(context);
        ivCardFace.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LayoutParams imgLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        cardContainer.addView(ivCardFace, imgLp);

        // Holographic Sheen Overlay
        sheenOverlay = new HolographicSheenOverlay(context);
        sheenOverlay.setCornerRadius(16f * density);
        LayoutParams sheenLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        cardContainer.addView(sheenOverlay, sheenLp);

        initSprings();
    }

    private void initSprings() {
        SpringForce springForce = new SpringForce(0f)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW);

        springRotX = new SpringAnimation(this, DynamicAnimation.ROTATION_X, 0f)
                .setSpring(springForce);
        springRotX.addUpdateListener((animation, value, velocity) -> updateSheenFromCurrentRotation());

        springRotY = new SpringAnimation(this, DynamicAnimation.ROTATION_Y, 0f)
                .setSpring(springForce);
        springRotY.addUpdateListener((animation, value, velocity) -> updateSheenFromCurrentRotation());

        SpringForce scaleForce = new SpringForce(1.0f)
                .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_MEDIUM);

        springScaleX = new SpringAnimation(this, DynamicAnimation.SCALE_X, 1.0f).setSpring(scaleForce);
        springScaleY = new SpringAnimation(this, DynamicAnimation.SCALE_Y, 1.0f).setSpring(scaleForce);
    }

    public void setOnTouchInteractionListener(OnTouchInteractionListener listener) {
        this.interactionListener = listener;
    }

    public void loadCardImage(@NonNull String url) {
        Glide.with(getContext())
                .load(url)
                .transform(new Rotate(90), new CenterCrop())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .placeholder(new ColorDrawable(0x18000000))
                .into(ivCardFace);
    }

    public void applyGyroTilt(float normPitch, float normRoll) {
        if (isUserTouching) return;

        float targetRotX = normPitch * MAX_TILT_DEG;
        float targetRotY = normRoll * MAX_TILT_DEG;

        setRotationX(targetRotX);
        setRotationY(targetRotY);

        sheenOverlay.setTilt(normRoll, -normPitch);
    }

    private void updateSheenFromCurrentRotation() {
        float normX = getRotationY() / MAX_TILT_DEG;
        float normY = -getRotationX() / MAX_TILT_DEG;
        sheenOverlay.setTilt(normX, normY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return super.onTouchEvent(event);

        float centerX = width * 0.5f;
        float centerY = height * 0.5f;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isUserTouching = true;
                if (interactionListener != null) interactionListener.onTouchDown();

                springRotX.cancel();
                springRotY.cancel();
                springScaleX.cancel();
                springScaleY.cancel();

                // Gentle tactile pop
                animate().scaleX(1.04f).scaleY(1.04f).setDuration(120).start();
                updateTiltFromTouch(event.getX(), event.getY(), centerX, centerY);
                return true;

            case MotionEvent.ACTION_MOVE:
                updateTiltFromTouch(event.getX(), event.getY(), centerX, centerY);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isUserTouching = false;
                if (interactionListener != null) interactionListener.onTouchUp();

                // Physics spring snapback to center
                springRotX.animateToFinalPosition(0f);
                springRotY.animateToFinalPosition(0f);

                animate().scaleX(1.0f).scaleY(1.0f).setDuration(220).start();
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void updateTiltFromTouch(float x, float y, float centerX, float centerY) {
        float normDx = clamp((x - centerX) / centerX, -1.0f, 1.0f);
        float normDy = clamp((y - centerY) / centerY, -1.0f, 1.0f);

        float rotY = normDx * MAX_TILT_DEG;
        float rotX = -normDy * MAX_TILT_DEG;

        setRotationX(rotX);
        setRotationY(rotY);

        sheenOverlay.setTilt(normDx, -normDy);
    }

    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}
