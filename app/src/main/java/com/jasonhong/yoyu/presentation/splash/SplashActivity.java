package com.jasonhong.yoyu.presentation.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.jasonhong.yoyu.databinding.ActivitySplashBinding;
import com.jasonhong.yoyu.presentation.home.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initSplashAnimation();
    }

    private void initSplashAnimation() {
        // Initial states
        binding.ivSplashLogo.setAlpha(0f);
        binding.ivSplashLogo.setScaleX(0.9f);
        binding.ivSplashLogo.setScaleY(0.9f);

        binding.layoutSplashTitle.setAlpha(0f);
        binding.layoutSplashTitle.setTranslationY(-dpToPx(20));

        // Total animation pipeline duration: ~2400ms
        // 1. Logo Animation (starts at 480ms, duration 1000ms)
        binding.ivSplashLogo.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setStartDelay(480)
                .setDuration(1000)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 2. Title Section Animation (starts at 1200ms, duration 900ms)
        binding.layoutSplashTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(1200)
                .setDuration(900)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 3. Central Divider Line expanding animation (starts at 1300ms, duration 900ms)
        int targetLineWidth = dpToPx(120);
        ValueAnimator lineAnimator = ValueAnimator.ofInt(0, targetLineWidth);
        lineAnimator.setStartDelay(1300);
        lineAnimator.setDuration(900);
        lineAnimator.setInterpolator(new DecelerateInterpolator());
        lineAnimator.addUpdateListener(animation -> {
            int width = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = binding.vSplashLine.getLayoutParams();
            lp.width = width;
            binding.vSplashLine.setLayoutParams(lp);
        });
        lineAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Short pause then navigate to MainActivity
                binding.getRoot().postDelayed(SplashActivity.this::navigateToHome, 300);
            }
        });
        lineAnimator.start();
    }

    private void navigateToHome() {
        if (isFinishing() || isDestroyed()) return;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
