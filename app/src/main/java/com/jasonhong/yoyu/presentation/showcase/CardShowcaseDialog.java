package com.jasonhong.yoyu.presentation.showcase;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.databinding.DialogCardShowcaseBinding;
import com.jasonhong.yoyu.presentation.showcase.model.CardShowcaseItem;
import com.jasonhong.yoyu.presentation.showcase.sensor.GyroscopeTiltController;
import com.jasonhong.yoyu.presentation.showcase.widget.Tilt3DCardView;

/**
 * Fullscreen Immersive 3D Card Showcase Dialog:
 * - Fluid Hero Orbital entry expansion from clicked card's grid coordinates.
 * - Hardware-accelerated 3D parallax tilt with Gyroscope and touch drag spring physics.
 * - Dynamic holographic sheen and specular light reflection.
 * - Action panel for instant favorite toggle and "Set as Cover" application.
 */
public class CardShowcaseDialog extends Dialog {

    public interface OnShowcaseActionListener {
        void onFavoriteToggled(CardShowcaseItem item, boolean isFavorite);
        void onApplyCover(CardShowcaseItem item);
    }

    private final Activity activity;
    private final CardShowcaseItem item;
    private final OnShowcaseActionListener actionListener;

    private DialogCardShowcaseBinding binding;
    private GyroscopeTiltController gyroController;
    private boolean isDismissing = false;

    public CardShowcaseDialog(@NonNull Activity activity,
                              @NonNull CardShowcaseItem item,
                              @NonNull OnShowcaseActionListener listener) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.activity = activity;
        this.item = item;
        this.actionListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        binding = DialogCardShowcaseBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        Window window = getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Enable edge-to-edge fullscreen
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        initViews();
        initSensor();
    }

    private void initViews() {
        // Load high definition card into 3D viewer
        binding.tiltCardView.loadCardImage(item.getUrl());

        binding.tvShowcaseCardId.setText(item.getFormattedId());
        updateFavoriteButton(item.isFavorite());

        // Background tap closes the dialog
        binding.rootContainer.setOnClickListener(v -> dismissWithTransition());
        binding.btnCloseShowcase.setOnClickListener(v -> {
            triggerHaptic(20);
            dismissWithTransition();
        });

        // Favorite Toggle
        binding.btnShowcaseFavorite.setOnClickListener(v -> {
            triggerHaptic(25);
            boolean newFav = !item.isFavorite();
            item.setFavorite(newFav);
            updateFavoriteButton(newFav);

            binding.btnShowcaseFavorite.animate()
                    .scaleX(1.35f).scaleY(1.35f).setDuration(120)
                    .withEndAction(() -> binding.btnShowcaseFavorite.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start())
                    .start();

            actionListener.onFavoriteToggled(item, newFav);
        });

        // Apply as Cover button
        binding.btnApplyCover.setOnClickListener(v -> {
            triggerHaptic(35);
            actionListener.onApplyCover(item);
            dismissWithTransition();
        });

        // Fade out hint text after 2.5 seconds
        binding.tvHintText.postDelayed(() -> {
            if (!isDismissing && binding != null) {
                binding.tvHintText.animate().alpha(0f).setDuration(500).start();
            }
        }, 2500);
    }

    private void initSensor() {
        gyroController = new GyroscopeTiltController(activity);
        gyroController.setListener((pitch, roll) -> {
            if (!isDismissing && binding != null) {
                binding.tiltCardView.applyGyroTilt(pitch, roll);
            }
        });

        binding.tiltCardView.setOnTouchInteractionListener(new Tilt3DCardView.OnTouchInteractionListener() {
            @Override
            public void onTouchDown() {
                if (gyroController != null) gyroController.pause();
            }

            @Override
            public void onTouchUp() {
                if (gyroController != null) gyroController.resume();
            }
        });
    }

    private void updateFavoriteButton(boolean isFav) {
        if (isFav) {
            binding.btnShowcaseFavorite.setImageResource(R.drawable.ic_favorite_filled);
            binding.btnShowcaseFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.expense_red)));
        } else {
            binding.btnShowcaseFavorite.setImageResource(R.drawable.ic_favorite_border);
            binding.btnShowcaseFavorite.setImageTintList(ColorStateList.valueOf(Color.parseColor("#A0FFFFFF")));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (gyroController != null) gyroController.start();
        runEnterHeroTransition();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gyroController != null) gyroController.stop();
    }

    private void runEnterHeroTransition() {
        Rect src = item.getSourceBounds();

        // Initial alpha states
        binding.rootContainer.setAlpha(0f);
        binding.btnCloseShowcase.setAlpha(0f);
        binding.tvHintText.setAlpha(0f);
        binding.layoutControlBar.setAlpha(0f);
        binding.layoutControlBar.setTranslationY(60f);

        binding.rootContainer.animate().alpha(1.0f).setDuration(260).start();

        if (src != null) {
            binding.cardAnchor.post(() -> {
                int[] targetLoc = new int[2];
                binding.cardAnchor.getLocationOnScreen(targetLoc);

                float targetWidth = binding.cardAnchor.getWidth();
                float targetHeight = binding.cardAnchor.getHeight();

                if (targetWidth > 0 && targetHeight > 0) {
                    float startScaleX = (float) src.width() / targetWidth;
                    float startScaleY = (float) src.height() / targetHeight;

                    float startTransX = src.centerX() - (targetLoc[0] + targetWidth * 0.5f);
                    float startTransY = src.centerY() - (targetLoc[1] + targetHeight * 0.5f);

                    binding.cardAnchor.setScaleX(startScaleX);
                    binding.cardAnchor.setScaleY(startScaleY);
                    binding.cardAnchor.setTranslationX(startTransX);
                    binding.cardAnchor.setTranslationY(startTransY);

                    binding.cardAnchor.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .translationX(0f)
                            .translationY(0f)
                            .setInterpolator(new OvershootInterpolator(0.9f))
                            .setDuration(350)
                            .start();
                }
            });
        } else {
            binding.cardAnchor.setScaleX(0.7f);
            binding.cardAnchor.setScaleY(0.7f);
            binding.cardAnchor.animate().scaleX(1f).scaleY(1f)
                    .setInterpolator(new OvershootInterpolator(0.9f))
                    .setDuration(300).start();
        }

        binding.btnCloseShowcase.animate().alpha(1f).setDuration(300).setStartDelay(120).start();
        binding.tvHintText.animate().alpha(1f).setDuration(300).setStartDelay(180).start();
        binding.layoutControlBar.animate().alpha(1f).translationY(0f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(320).setStartDelay(100).start();
    }

    public void dismissWithTransition() {
        if (isDismissing) return;
        isDismissing = true;
        if (gyroController != null) gyroController.stop();

        Rect src = item.getSourceBounds();

        binding.rootContainer.animate().alpha(0f).setDuration(240).start();
        binding.btnCloseShowcase.animate().alpha(0f).setDuration(180).start();
        binding.tvHintText.animate().alpha(0f).setDuration(180).start();
        binding.layoutControlBar.animate().alpha(0f).translationY(60f).setDuration(200).start();

        if (src != null) {
            int[] targetLoc = new int[2];
            binding.cardAnchor.getLocationOnScreen(targetLoc);

            float targetWidth = binding.cardAnchor.getWidth();
            float targetHeight = binding.cardAnchor.getHeight();

            if (targetWidth > 0 && targetHeight > 0) {
                float endScaleX = (float) src.width() / targetWidth;
                float endScaleY = (float) src.height() / targetHeight;

                float endTransX = src.centerX() - (targetLoc[0] + targetWidth * 0.5f);
                float endTransY = src.centerY() - (targetLoc[1] + targetHeight * 0.5f);

                binding.cardAnchor.animate()
                        .scaleX(endScaleX)
                        .scaleY(endScaleY)
                        .translationX(endTransX)
                        .translationY(endTransY)
                        .setInterpolator(new DecelerateInterpolator())
                        .setDuration(240)
                        .withEndAction(this::dismiss)
                        .start();
                return;
            }
        }

        binding.cardAnchor.animate()
                .scaleX(0.7f).scaleY(0.7f).alpha(0f)
                .setDuration(220)
                .withEndAction(this::dismiss)
                .start();
    }

    private void triggerHaptic(int ms) {
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        }
    }
}
