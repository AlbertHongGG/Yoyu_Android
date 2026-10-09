package com.jasonhong.yoyu.core.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.jasonhong.yoyu.R;

public class NotificationToast {

    public enum Type {
        SUCCESS,
        ERROR,
        WARNING,
        INFO
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static View currentToastView = null;
    private static Runnable dismissRunnable = null;

    public static void showSuccess(@NonNull Activity activity, @NonNull String message) {
        show(activity, message, Type.SUCCESS);
    }

    public static void showSuccess(@NonNull ViewGroup container, @NonNull String message) {
        show(container, message, Type.SUCCESS);
    }

    public static void showError(@NonNull Activity activity, @NonNull String message) {
        show(activity, message, Type.ERROR);
    }

    public static void showError(@NonNull ViewGroup container, @NonNull String message) {
        show(container, message, Type.ERROR);
    }

    public static void showWarning(@NonNull Activity activity, @NonNull String message) {
        show(activity, message, Type.WARNING);
    }

    public static void showWarning(@NonNull ViewGroup container, @NonNull String message) {
        show(container, message, Type.WARNING);
    }

    public static void showInfo(@NonNull Activity activity, @NonNull String message) {
        show(activity, message, Type.INFO);
    }

    public static void showInfo(@NonNull ViewGroup container, @NonNull String message) {
        show(container, message, Type.INFO);
    }

    public static synchronized void show(@NonNull Activity activity, @NonNull String message, @NonNull Type type) {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) return;
        show(rootView, message, type);
    }

    public static synchronized void show(@NonNull ViewGroup container, @NonNull String message, @NonNull Type type) {
        if (container == null) return;
        Context context = container.getContext();
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) return;
        }

        // Dismiss any existing toast first
        dismissImmediate();

        View toastView = LayoutInflater.from(context).inflate(R.layout.view_notification_toast, container, false);
        currentToastView = toastView;

        ImageView iconView = toastView.findViewById(R.id.toastIcon);
        TextView messageView = toastView.findViewById(R.id.toastMessage);
        ImageView closeView = toastView.findViewById(R.id.toastClose);

        messageView.setText(message);

        int color;
        int iconRes;

        switch (type) {
            case SUCCESS:
                color = ContextCompat.getColor(context, R.color.success_green);
                iconRes = R.drawable.ic_check_circle;
                break;
            case ERROR:
                color = ContextCompat.getColor(context, R.color.expense_red);
                iconRes = R.drawable.ic_close;
                break;
            case WARNING:
                color = ContextCompat.getColor(context, R.color.warning_amber);
                iconRes = R.drawable.ic_close;
                break;
            case INFO:
            default:
                color = ContextCompat.getColor(context, R.color.primary);
                iconRes = R.drawable.ic_check_circle;
                break;
        }

        iconView.setImageResource(iconRes);
        ImageViewCompat.setImageTintList(iconView, ColorStateList.valueOf(color));

        int closeColor = ContextCompat.getColor(context, R.color.text_secondary_light);
        ImageViewCompat.setImageTintList(closeView, ColorStateList.valueOf(closeColor));

        closeView.setOnClickListener(v -> dismissAnimated(toastView));

        container.addView(toastView);

        // Slide in animation from top
        toastView.setTranslationY(-200f);
        toastView.setAlpha(0f);
        toastView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .start();

        dismissRunnable = () -> dismissAnimated(toastView);
        handler.postDelayed(dismissRunnable, type == Type.ERROR ? 5000 : 4000);
    }

    private static synchronized void dismissAnimated(View toastView) {
        if (toastView == null || toastView.getParent() == null) return;
        if (dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable);
            dismissRunnable = null;
        }

        toastView.animate()
                .translationY(-200f)
                .alpha(0f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewGroup parent = (ViewGroup) toastView.getParent();
                        if (parent != null) {
                            parent.removeView(toastView);
                        }
                        if (currentToastView == toastView) {
                            currentToastView = null;
                        }
                    }
                })
                .start();
    }

    private static synchronized void dismissImmediate() {
        if (dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable);
            dismissRunnable = null;
        }
        if (currentToastView != null) {
            ViewGroup parent = (ViewGroup) currentToastView.getParent();
            if (parent != null) {
                parent.removeView(currentToastView);
            }
            currentToastView = null;
        }
    }
}
