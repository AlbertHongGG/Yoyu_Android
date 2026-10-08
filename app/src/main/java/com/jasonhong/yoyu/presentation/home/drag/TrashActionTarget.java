package com.jasonhong.yoyu.presentation.home.drag;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.widget.ImageViewCompat;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.domain.model.CardEntity;

public class TrashActionTarget implements IDragTarget {

    public interface OnCardDropListener {
        void onCardDropped(CardEntity card);
    }

    private final View targetView;
    private final ImageView ivIcon;
    private final int defaultIconColor;
    private final OnCardDropListener dropListener;
    private boolean isHovered = false;

    public TrashActionTarget(View targetView, ImageView ivIcon, int defaultIconColor, OnCardDropListener dropListener) {
        this.targetView = targetView;
        this.ivIcon = ivIcon;
        this.defaultIconColor = defaultIconColor;
        this.dropListener = dropListener;
    }

    @Override
    public Rect getHitRectOnScreen() {
        int[] loc = new int[2];
        targetView.getLocationOnScreen(loc);
        return new Rect(loc[0], loc[1], loc[0] + targetView.getWidth(), loc[1] + targetView.getHeight());
    }

    @Override
    public boolean containsPoint(float screenX, float screenY) {
        Rect bounds = getHitRectOnScreen();
        // Expand touch target by 16dp for better usability
        int slop = dpToPx(targetView.getContext(), 16);
        Rect expandedBounds = new Rect(
                bounds.left - slop,
                bounds.top - slop,
                bounds.right + slop,
                bounds.bottom + slop
        );
        return expandedBounds.contains((int) screenX, (int) screenY);
    }

    @Override
    public void onDragStarted() {
        isHovered = false;
        ivIcon.setImageResource(R.drawable.ic_delete);
        ImageViewCompat.setImageTintList(ivIcon, ColorStateList.valueOf(defaultIconColor));
        targetView.setBackgroundResource(R.drawable.bg_fab_default);
    }

    @Override
    public void onDragEntered() {
        if (isHovered) return;
        isHovered = true;
        animateSize(dpToPx(targetView.getContext(), 56), dpToPx(targetView.getContext(), 64));
        targetView.setBackgroundResource(R.drawable.bg_fab_hover);
        ivIcon.setImageResource(R.drawable.ic_delete_forever);
        ImageViewCompat.setImageTintList(ivIcon, ColorStateList.valueOf(0xFFFFFFFF));
        targetView.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
    }

    @Override
    public void onDragExited() {
        if (!isHovered) return;
        isHovered = false;
        animateSize(dpToPx(targetView.getContext(), 64), dpToPx(targetView.getContext(), 56));
        targetView.setBackgroundResource(R.drawable.bg_fab_default);
        ivIcon.setImageResource(R.drawable.ic_delete);
        ImageViewCompat.setImageTintList(ivIcon, ColorStateList.valueOf(defaultIconColor));
    }

    @Override
    public void onDragDropped(CardEntity card) {
        targetView.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        if (dropListener != null && card != null) {
            dropListener.onCardDropped(card);
        }
        onDragEnded();
    }

    @Override
    public void onDragEnded() {
        isHovered = false;
        int currentSize = targetView.getWidth();
        int normalSize = dpToPx(targetView.getContext(), 56);
        if (currentSize != normalSize) {
            animateSize(currentSize, normalSize);
        }
        targetView.setBackgroundResource(R.drawable.bg_fab_default);
        ivIcon.setImageResource(R.drawable.ic_add);
        ImageViewCompat.setImageTintList(ivIcon, ColorStateList.valueOf(defaultIconColor));
    }

    private void animateSize(int fromSize, int toSize) {
        if (fromSize == toSize) return;
        ValueAnimator anim = ValueAnimator.ofInt(fromSize, toSize);
        anim.setDuration(150);
        anim.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = targetView.getLayoutParams();
            lp.width = val;
            lp.height = val;
            targetView.setLayoutParams(lp);
        });
        anim.start();
    }

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
