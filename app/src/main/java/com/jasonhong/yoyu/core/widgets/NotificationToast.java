package com.jasonhong.yoyu.core.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;

import com.jasonhong.yoyu.R;

/**
 * Global Floating Notification Toast Component (LensWise / GeminiFlowApp 1:1 Design):
 * - Semi-translucent layered card design with subtle 1dp border and smooth 8dp elevation.
 * - Dynamic Dark/Light mode adaptation (Slate 800 semi-translucent in dark mode, crisp white translucent in light mode).
 * - Automatic status bar insets detection, perfectly floating below status bar without overlapping clocks/icons.
 * - Elegant outlined vector icons (Emerald CheckCircleOutline, ErrorOutline, WarningOutline, InfoOutline).
 */
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

        boolean isDark = isDarkTheme(context, container);

        // 1. Background & Border styling matching GeminiFlowApp
        // Dark: Color(0xFF1E293B).copy(alpha = 0.94f) with 1dp border Color.White.copy(alpha = 0.12f)
        // Light: Color.White.copy(alpha = 0.94f) with 1dp border Color.Black.copy(alpha = 0.06f)
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(context, 16));
        if (isDark) {
            shape.setColor(Color.parseColor("#F01E293B"));
            shape.setStroke(dpToPx(context, 1), Color.parseColor("#20FFFFFF"));
        } else {
            shape.setColor(Color.parseColor("#F0FFFFFF"));
            shape.setStroke(dpToPx(context, 1), Color.parseColor("#10000000"));
        }
        toastView.setBackground(shape);
        toastView.setElevation(dpToPx(context, 8));

        ImageView iconView = toastView.findViewById(R.id.toastIcon);
        TextView messageView = toastView.findViewById(R.id.toastMessage);
        ImageView closeView = toastView.findViewById(R.id.toastClose);

        messageView.setText(message);
        if (isDark) {
            messageView.setTextColor(Color.parseColor("#EAFFFFFF"));
            ImageViewCompat.setImageTintList(closeView, ColorStateList.valueOf(Color.parseColor("#99FFFFFF")));
        } else {
            messageView.setTextColor(Color.parseColor("#DE000000"));
            ImageViewCompat.setImageTintList(closeView, ColorStateList.valueOf(Color.parseColor("#8A000000")));
        }

        // 2. Outlined vector icons & distinct type colors matching GeminiFlowApp
        int color;
        int iconRes;

        switch (type) {
            case SUCCESS:
                color = Color.parseColor("#10B981"); // Emerald green
                iconRes = R.drawable.ic_check_circle_outline;
                break;
            case ERROR:
                color = Color.parseColor("#EF4444"); // Red
                iconRes = R.drawable.ic_error_outline;
                break;
            case WARNING:
                color = Color.parseColor("#F59E0B"); // Amber
                iconRes = R.drawable.ic_warning_outline;
                break;
            case INFO:
            default:
                color = Color.parseColor("#3B82F6"); // Blue
                iconRes = R.drawable.ic_info_outline;
                break;
        }

        iconView.setImageResource(iconRes);
        ImageViewCompat.setImageTintList(iconView, ColorStateList.valueOf(color));

        // 3. Status-bar-aware Top Margin (solves "位置有點太上面了" status bar collision)
        int statusBarHeight = getStatusBarHeight(context);
        int topMargin = statusBarHeight + dpToPx(context, 10);
        int sideMargin = dpToPx(context, 16);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) toastView.getLayoutParams();
        if (lp != null) {
            lp.setMargins(sideMargin, topMargin, sideMargin, 0);
            toastView.setLayoutParams(lp);
        }

        final int dismissOffsetY = -topMargin - dpToPx(context, 80);

        closeView.setOnClickListener(v -> dismissAnimated(toastView, dismissOffsetY));

        container.addView(toastView);

        // 4. Smooth slide-in animation from top
        toastView.setTranslationY(dismissOffsetY);
        toastView.setAlpha(0f);
        toastView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .start();

        dismissRunnable = () -> dismissAnimated(toastView, dismissOffsetY);
        handler.postDelayed(dismissRunnable, type == Type.ERROR ? 5000 : 3500);
    }

    private static synchronized void dismissAnimated(View toastView, int dismissOffsetY) {
        if (toastView == null || toastView.getParent() == null) return;
        if (dismissRunnable != null) {
            handler.removeCallbacks(dismissRunnable);
            dismissRunnable = null;
        }

        toastView.animate()
                .translationY(dismissOffsetY)
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

    private static boolean isDarkTheme(@NonNull Context context, @NonNull ViewGroup container) {
        if (container.getId() == R.id.rootContainer) {
            return true;
        }
        int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    private static int getStatusBarHeight(@NonNull Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        if (statusBarHeight <= 0) {
            statusBarHeight = dpToPx(context, 32);
        }
        return statusBarHeight;
    }

    private static int dpToPx(@NonNull Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
