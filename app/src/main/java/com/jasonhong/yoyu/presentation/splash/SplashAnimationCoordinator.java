package com.jasonhong.yoyu.presentation.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;

/**
 * Dedicated Animation Coordinator managing the splash screen visual branding pipeline:
 * - Decouples animation choreography from Activity lifecycle.
 * - Manages concurrent animators and handles safe cancellation.
 */
public class SplashAnimationCoordinator {

    public interface AnimationCallback {
        void onAnimationFinished();
    }

    private final View logoView;
    private final View titleLayout;
    private final View lineView;
    private final int targetLineWidth;

    private ViewPropertyAnimator logoAnimator;
    private ViewPropertyAnimator titleAnimator;
    private ValueAnimator lineAnimator;
    private boolean isCanceled = false;

    public SplashAnimationCoordinator(@NonNull View logoView,
                                      @NonNull View titleLayout,
                                      @NonNull View lineView,
                                      int targetLineWidth) {
        this.logoView = logoView;
        this.titleLayout = titleLayout;
        this.lineView = lineView;
        this.targetLineWidth = targetLineWidth;
    }

    public void startAnimation(@NonNull AnimationCallback callback) {
        // Reset initial visual states
        logoView.setAlpha(0f);
        logoView.setScaleX(0.9f);
        logoView.setScaleY(0.9f);

        titleLayout.setAlpha(0f);
        titleLayout.setTranslationY(-dpToPx(20, logoView));

        ViewGroup.LayoutParams lineParams = lineView.getLayoutParams();
        lineParams.width = 0;
        lineView.setLayoutParams(lineParams);

        // 1. Logo Animation (starts at 480ms, duration 1000ms)
        logoAnimator = logoView.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setStartDelay(480)
                .setDuration(1000)
                .setInterpolator(new DecelerateInterpolator());
        logoAnimator.start();

        // 2. Title Section Animation (starts at 1200ms, duration 900ms)
        titleAnimator = titleLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(1200)
                .setDuration(900)
                .setInterpolator(new DecelerateInterpolator());
        titleAnimator.start();

        // 3. Central Divider Line expanding animation (starts at 1300ms, duration 900ms)
        lineAnimator = ValueAnimator.ofInt(0, targetLineWidth);
        lineAnimator.setStartDelay(1300);
        lineAnimator.setDuration(900);
        lineAnimator.setInterpolator(new DecelerateInterpolator());
        lineAnimator.addUpdateListener(animation -> {
            if (isCanceled) return;
            int width = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = lineView.getLayoutParams();
            lp.width = width;
            lineView.setLayoutParams(lp);
        });
        lineAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCanceled && callback != null) {
                    callback.onAnimationFinished();
                }
            }
        });
        lineAnimator.start();
    }

    public void cancel() {
        isCanceled = true;
        if (logoAnimator != null) logoAnimator.cancel();
        if (titleAnimator != null) titleAnimator.cancel();
        if (lineAnimator != null) lineAnimator.cancel();
    }

    private int dpToPx(int dp, View view) {
        return (int) (dp * view.getResources().getDisplayMetrics().density + 0.5f);
    }
}
