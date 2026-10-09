package com.jasonhong.yoyu.core.navigation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jasonhong.yoyu.presentation.splash.SplashActivity;

import java.io.Serializable;

/**
 * Encapsulates application launch parameters and navigation routing intent:
 * - Eliminates scattered magic strings across Activities and Widgets.
 * - Manages PendingIntent generation with proper flags (NEW_TASK, CLEAR_TOP, IMMUTABLE).
 * - Distinguishes launch sources (Widget, Launcher, Notification) for analytics and UX flow.
 */
public final class AppLaunchPayload implements Serializable {

    public enum LaunchSource {
        LAUNCHER,
        WIDGET_CARD_INFO,
        WIDGET_EMPTY,
        NOTIFICATION
    }

    public static final String EXTRA_TARGET_CARD_NO = "com.jasonhong.yoyu.extra.TARGET_CARD_NO";
    public static final String EXTRA_LAUNCH_SOURCE = "com.jasonhong.yoyu.extra.LAUNCH_SOURCE";

    @Nullable
    private final String targetCardNo;
    @NonNull
    private final LaunchSource source;

    public AppLaunchPayload(@Nullable String targetCardNo, @NonNull LaunchSource source) {
        this.targetCardNo = targetCardNo;
        this.source = source != null ? source : LaunchSource.LAUNCHER;
    }

    @Nullable
    public String getTargetCardNo() {
        return targetCardNo;
    }

    @NonNull
    public LaunchSource getSource() {
        return source;
    }

    public boolean hasTargetCard() {
        return targetCardNo != null && !targetCardNo.trim().isEmpty();
    }

    @NonNull
    public Intent toIntent(@NonNull Context context, @NonNull Class<?> targetClass) {
        Intent intent = new Intent(context, targetClass);
        if (targetCardNo != null) {
            intent.putExtra(EXTRA_TARGET_CARD_NO, targetCardNo);
        }
        intent.putExtra(EXTRA_LAUNCH_SOURCE, source.name());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @NonNull
    public static AppLaunchPayload fromIntent(@Nullable Intent intent) {
        if (intent == null) {
            return new AppLaunchPayload(null, LaunchSource.LAUNCHER);
        }
        String cardNo = intent.getStringExtra(EXTRA_TARGET_CARD_NO);
        String sourceStr = intent.getStringExtra(EXTRA_LAUNCH_SOURCE);
        LaunchSource source = LaunchSource.LAUNCHER;
        if (sourceStr != null) {
            try {
                source = LaunchSource.valueOf(sourceStr);
            } catch (IllegalArgumentException ignored) {}
        }
        return new AppLaunchPayload(cardNo, source);
    }

    @NonNull
    public static PendingIntent createWidgetCardLaunchPendingIntent(@NonNull Context context,
                                                                    int requestCode,
                                                                    @NonNull String cardNo) {
        AppLaunchPayload payload = new AppLaunchPayload(cardNo, LaunchSource.WIDGET_CARD_INFO);
        Intent intent = payload.toIntent(context, SplashActivity.class);
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    @NonNull
    public static PendingIntent createWidgetEmptyLaunchPendingIntent(@NonNull Context context,
                                                                     int requestCode) {
        AppLaunchPayload payload = new AppLaunchPayload(null, LaunchSource.WIDGET_EMPTY);
        Intent intent = payload.toIntent(context, SplashActivity.class);
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
