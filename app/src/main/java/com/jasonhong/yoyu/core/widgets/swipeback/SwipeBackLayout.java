package com.jasonhong.yoyu.core.widgets.swipeback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Pure native Cupertino-style SwipeBack container inspired by GeminiFlowApp.
 * Provides deterministic edge-swipe gesture detection, left-side depth shadow,
 * background scrim fade, and velocity-based settling.
 */
public class SwipeBackLayout extends FrameLayout {

    private static final int EDGE_THRESHOLD_DP = 40;
    private static final int SHADOW_WIDTH_DP = 16;
    private static final float FINISH_THRESHOLD_RATIO = 0.35f;
    private static final float MIN_FLING_VELOCITY = 800f; // px/s

    private View contentView;
    private SwipeBackListener listener;
    private Activity attachedActivity;

    private float downX;
    private float downY;
    private boolean isEdgeGesturePossible = false;
    private boolean isDragging = false;
    private boolean isSettling = false;

    private float currentOffset = 0f;
    private int screenWidth = 0;
    private int edgeThresholdPx;
    private int shadowWidthPx;
    private int touchSlop;

    private VelocityTracker velocityTracker;
    private GradientDrawable shadowDrawable;
    private final Paint scrimPaint = new Paint();

    public SwipeBackLayout(@NonNull Context context) {
        this(context, null);
    }

    public SwipeBackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeBackLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        edgeThresholdPx = dpToPx(EDGE_THRESHOLD_DP);
        shadowWidthPx = dpToPx(SHADOW_WIDTH_DP);

        // 16dp horizontal shadow gradient cast to the left
        shadowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT,
                new int[]{0x3A000000, 0x15000000, 0x00000000}
        );

        scrimPaint.setColor(Color.BLACK);
        scrimPaint.setAntiAlias(true);
    }

    public void attachToActivity(Activity activity, SwipeBackListener listener) {
        this.attachedActivity = activity;
        this.listener = listener;

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content != null && content.getChildCount() > 0) {
            View child = content.getChildAt(0);
            content.removeView(child);
            addView(child, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            content.addView(this, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            contentView = child;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isSettling) return true;

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                downY = ev.getRawY();
                isEdgeGesturePossible = (downX <= edgeThresholdPx);
                isDragging = false;
                initVelocityTracker();
                velocityTracker.addMovement(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isEdgeGesturePossible) return false;
                if (isDragging) return true;

                float deltaX = ev.getRawX() - downX;
                float deltaY = ev.getRawY() - downY;

                // Slope check: horizontal move must dominate vertical movement
                if (deltaX > touchSlop && deltaX > Math.abs(deltaY) * 1.1f) {
                    isDragging = true;
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    if (velocityTracker != null) {
                        velocityTracker.addMovement(ev);
                    }
                    return true;
                } else if (Math.abs(deltaY) > touchSlop) {
                    // Vertical scroll wins: immediately abandon edge detection
                    isEdgeGesturePossible = false;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isEdgeGesturePossible = false;
                isDragging = false;
                recycleVelocityTracker();
                break;
        }

        return isDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSettling) return true;
        if (!isEdgeGesturePossible && !isDragging) return super.onTouchEvent(ev);

        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float currentX = ev.getRawX();
                    float delta = Math.max(0f, currentX - downX);
                    setScrollOffset(delta);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    float velocityX = 0f;
                    if (velocityTracker != null) {
                        velocityTracker.computeCurrentVelocity(1000);
                        velocityX = velocityTracker.getXVelocity();
                    }
                    settle(velocityX);
                    recycleVelocityTracker();
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    cancelSwipe();
                    recycleVelocityTracker();
                    return true;
                }
                break;
        }

        return super.onTouchEvent(ev);
    }

    private void setScrollOffset(float offset) {
        currentOffset = Math.max(0f, Math.min(offset, screenWidth));
        if (contentView != null) {
            contentView.setTranslationX(currentOffset);
        }
        if (listener != null && screenWidth > 0) {
            listener.onSwipeProgress(currentOffset / screenWidth);
        }
        invalidate();
    }

    private void settle(float velocityX) {
        isSettling = true;
        float progress = screenWidth > 0 ? (currentOffset / screenWidth) : 0f;
        boolean shouldFinish = progress > FINISH_THRESHOLD_RATIO || velocityX > MIN_FLING_VELOCITY;

        float targetOffset = shouldFinish ? screenWidth : 0f;
        float distance = Math.abs(targetOffset - currentOffset);
        int duration = (int) Math.max(120, Math.min(260, (distance / screenWidth) * 260));

        ValueAnimator anim = ValueAnimator.ofFloat(currentOffset, targetOffset);
        anim.setDuration(duration);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(animation -> setScrollOffset((float) animation.getAnimatedValue()));
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isSettling = false;
                isDragging = false;
                if (shouldFinish) {
                    if (listener != null) {
                        listener.onSwipeFinished();
                    } else if (attachedActivity != null) {
                        attachedActivity.finish();
                        attachedActivity.overridePendingTransition(0, 0);
                    }
                } else {
                    if (listener != null) {
                        listener.onSwipeCancel();
                    }
                }
            }
        });
        anim.start();
    }

    private void cancelSwipe() {
        settle(0f);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // Draw black scrim background behind sliding content
        if (screenWidth > 0 && currentOffset > 0) {
            float progress = Math.min(1f, currentOffset / screenWidth);
            int scrimAlpha = (int) (0x40 * (1f - progress)); // max 25% opacity
            scrimPaint.setAlpha(scrimAlpha);
            canvas.drawRect(0, 0, screenWidth, getHeight(), scrimPaint);
        }

        boolean result = super.drawChild(canvas, child, drawingTime);

        // Draw 16dp left shadow gradient attached to the left edge of child
        if (child == contentView && currentOffset > 0) {
            int childLeft = (int) child.getTranslationX();
            shadowDrawable.setBounds(childLeft - shadowWidthPx, 0, childLeft, getHeight());
            shadowDrawable.draw(canvas);
        }

        return result;
    }

    private void initVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
