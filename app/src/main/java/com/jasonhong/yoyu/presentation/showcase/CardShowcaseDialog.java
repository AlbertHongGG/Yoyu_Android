package com.jasonhong.yoyu.presentation.showcase;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.databinding.DialogCardShowcaseBinding;
import com.jasonhong.yoyu.presentation.showcase.model.CardShowcaseItem;
import com.jasonhong.yoyu.presentation.showcase.sensor.GyroscopeTiltController;
import com.jasonhong.yoyu.presentation.showcase.widget.Tilt3DCardView;

/**
 * Fullscreen Immersive 3D Card Showcase Dialog:
 * - Fluid Hero entry expansion from source grid thumbnail coordinates.
 * - Hardware-accelerated 3D parallax tilt with Gyroscope and touch drag spring physics.
 * - Dynamic holographic sheen and specular light reflection.
 * - Pure minimal floating metadata row (#ID on left, Favorite heart on right) strictly aligned to card width.
 * - Tap backdrop outside card to dismiss seamlessly.
 */
public class CardShowcaseDialog extends Dialog {

    public interface OnShowcaseFavoriteListener {
        void onFavoriteToggled(CardShowcaseItem item, boolean isFavorite);
    }

    private final Activity activity;
    private final CardShowcaseItem item;
    @Nullable
    private final OnShowcaseFavoriteListener favoriteListener;

    private DialogCardShowcaseBinding binding;
    private GyroscopeTiltController gyroController;
    private boolean isDismissing = false;

    public CardShowcaseDialog(@NonNull Activity activity,
                              @NonNull CardShowcaseItem item,
                              @Nullable OnShowcaseFavoriteListener listener) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.activity = activity;
        this.item = item;
        this.favoriteListener = listener;
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

        // Tap ID to copy
        binding.tvShowcaseCardId.setOnClickListener(v -> {
            triggerHaptic(20);
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("CardFace ID", item.getFormattedId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, "已複製卡面 ID " + item.getFormattedId(), Toast.LENGTH_SHORT).show();
            }
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

            if (favoriteListener != null) {
                favoriteListener.onFavoriteToggled(item, newFav);
            }
        });
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
            binding.btnShowcaseFavorite.setImageTintList(ColorStateList.valueOf(Color.parseColor("#B3FFFFFF")));
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

        // Initial alpha & translation states
        binding.rootContainer.setAlpha(0f);
        binding.layoutShowcaseFooter.setAlpha(0f);
        binding.layoutShowcaseFooter.setTranslationY(20f);

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
                            .setInterpolator(new OvershootInterpolator(0.85f))
                            .setDuration(330)
                            .start();
                }
            });
        } else {
            binding.cardAnchor.setScaleX(0.72f);
            binding.cardAnchor.setScaleY(0.72f);
            binding.cardAnchor.animate().scaleX(1f).scaleY(1f)
                    .setInterpolator(new OvershootInterpolator(0.85f))
                    .setDuration(300).start();
        }

        binding.layoutShowcaseFooter.animate()
                .alpha(1f)
                .translationY(0f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .setStartDelay(100)
                .start();
    }

    public void dismissWithTransition() {
        if (isDismissing) return;
        isDismissing = true;
        if (gyroController != null) gyroController.stop();

        Rect src = item.getSourceBounds();

        binding.rootContainer.animate().alpha(0f).setDuration(220).start();
        binding.layoutShowcaseFooter.animate().alpha(0f).translationY(16f).setDuration(160).start();

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
                        .setDuration(220)
                        .withEndAction(this::dismiss)
                        .start();
                return;
            }
        }

        binding.cardAnchor.animate()
                .scaleX(0.72f).scaleY(0.72f).alpha(0f)
                .setDuration(200)
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
