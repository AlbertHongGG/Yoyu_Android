package com.jasonhong.yoyu.presentation.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;

import com.jasonhong.yoyu.core.navigation.AppLaunchPayload;
import com.jasonhong.yoyu.databinding.ActivitySplashBinding;
import com.jasonhong.yoyu.presentation.home.MainActivity;

/**
 * Universal Application Gateway:
 * - Single point of entry for Launcher, AppWidget, and deep links.
 * - Suppresses OS-level splash screen through LaunchTheme.
 * - Smart Dual-Route Dispatcher:
 *   - Cold Launch (isTaskRoot): Orchestrates SplashAnimationCoordinator branding animation (~2.4s).
 *   - Warm/Hot Launch (!isTaskRoot): Instant 0ms passthrough directly to MainActivity.
 * - Passes AppLaunchPayload through to MainActivity.
 */
public class SplashActivity extends ComponentActivity {

    private ActivitySplashBinding binding;
    private SplashAnimationCoordinator animationCoordinator;
    private AppLaunchPayload launchPayload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launchPayload = AppLaunchPayload.fromIntent(getIntent());

        // Warm launch fast path: If the task is already running in background, bypass splash delay immediately!
        if (!isTaskRoot()) {
            navigateToHome(false);
            return;
        }

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int targetLineWidth = (int) (120 * getResources().getDisplayMetrics().density + 0.5f);
        animationCoordinator = new SplashAnimationCoordinator(
                binding.ivSplashLogo,
                binding.layoutSplashTitle,
                binding.vSplashLine,
                targetLineWidth
        );

        animationCoordinator.startAnimation(() -> {
            binding.getRoot().postDelayed(() -> navigateToHome(true), 300);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        launchPayload = AppLaunchPayload.fromIntent(intent);
        navigateToHome(false);
    }

    private void navigateToHome(boolean animatedTransition) {
        if (isFinishing() || isDestroyed()) return;
        Intent intent = launchPayload.toIntent(this, MainActivity.class);
        startActivity(intent);
        if (animatedTransition) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            overridePendingTransition(0, 0);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationCoordinator != null) {
            animationCoordinator.cancel();
        }
    }
}
