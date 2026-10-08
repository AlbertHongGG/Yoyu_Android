package com.jasonhong.yoyu.core.widgets.skeleton;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global synchronous pulse clock matching Flutter Yoyu's AnimationController(duration: 1s)..repeat(reverse: true).
 * Ensures all skeleton cards across the UI breathe in 100% synchrony without wasting CPU cycles.
 */
public final class SkeletonPulseClock {

    public interface PulseListener {
        void onPulseUpdate(float fraction);
    }

    private static final SkeletonPulseClock INSTANCE = new SkeletonPulseClock();

    public static SkeletonPulseClock get() {
        return INSTANCE;
    }

    private final Set<PulseListener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ValueAnimator animator;
    private float currentFraction = 0f;

    private SkeletonPulseClock() {
        // Singleton
    }

    public void register(PulseListener listener) {
        if (listener == null) return;
        listeners.add(listener);
        listener.onPulseUpdate(currentFraction);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ensureStarted();
        } else {
            mainHandler.post(this::ensureStarted);
        }
    }

    public void unregister(PulseListener listener) {
        if (listener == null) return;
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                stop();
            } else {
                mainHandler.post(this::stop);
            }
        }
    }

    public float getCurrentFraction() {
        return currentFraction;
    }

    private void ensureStarted() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(1000);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                currentFraction = (float) animation.getAnimatedValue();
                for (PulseListener listener : listeners) {
                    listener.onPulseUpdate(currentFraction);
                }
            });
        }
        if (!animator.isRunning() && !listeners.isEmpty()) {
            animator.start();
        }
    }

    private void stop() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }
}
