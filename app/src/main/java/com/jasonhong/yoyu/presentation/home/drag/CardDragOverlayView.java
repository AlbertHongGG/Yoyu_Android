package com.jasonhong.yoyu.presentation.home.drag;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.jasonhong.yoyu.core.widgets.skeleton.SkeletonPulseDrawable;
import com.jasonhong.yoyu.databinding.ItemCardBinding;
import com.jasonhong.yoyu.domain.model.CardEntity;

import java.text.NumberFormat;
import java.util.Locale;

public class CardDragOverlayView extends FrameLayout {

    private View floatingCardView;
    private ItemCardBinding floatingBinding;
    private float touchOffsetX = 0f;
    private float touchOffsetY = 0f;
    private final int[] overlayLoc = new int[2];
    private final NumberFormat currencyFormat;

    public CardDragOverlayView(@NonNull Context context) {
        this(context, null);
    }

    public CardDragOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardDragOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.TAIWAN);
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);
    }

    public void startDrag(View sourceItemView, CardEntity card, float touchX, float touchY) {
        removeAllViews();
        setVisibility(View.VISIBLE);

        floatingBinding = ItemCardBinding.inflate(LayoutInflater.from(getContext()), this, false);
        floatingCardView = floatingBinding.getRoot();

        // Bind Card Info
        floatingBinding.tvCardName.setText(card.getCardName());
        floatingBinding.tvCardNo.setText(formatCardNo(card.getCardNo()));
        floatingBinding.tvCardBalance.setText(currencyFormat.format(card.getLastTranSum()));
        floatingBinding.tagRegistered.setVisibility(card.isRegister() ? View.VISIBLE : View.GONE);
        floatingBinding.btnChangeCover.setVisibility(View.GONE);

        // Load Card Face
        Glide.with(getContext())
                .load(card.getCardFaceUrl())
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .centerCrop()
                .placeholder(new com.jasonhong.yoyu.core.widgets.skeleton.SkeletonPulseDrawable(getContext(), 0f))
                .into(floatingBinding.ivCardFace);

        int sourceWidth = sourceItemView.getWidth();
        int sourceHeight = sourceItemView.getHeight();

        LayoutParams lp = new LayoutParams(sourceWidth, sourceHeight);
        floatingCardView.setLayoutParams(lp);

        int[] sourceLoc = new int[2];
        sourceItemView.getLocationInWindow(sourceLoc);
        getLocationInWindow(overlayLoc);

        float initialX = sourceLoc[0] - overlayLoc[0];
        float initialY = sourceLoc[1] - overlayLoc[1];

        floatingCardView.setTranslationX(initialX);
        floatingCardView.setTranslationY(initialY);

        // Touch offset relative to floating view top-left
        touchOffsetX = touchX - sourceLoc[0];
        touchOffsetY = touchY - sourceLoc[1];

        // Pivot center for scaling
        floatingCardView.setPivotX(sourceWidth / 2f);
        floatingCardView.setPivotY(sourceHeight / 2f);

        // 3D elevation
        floatingBinding.cardContainer.setCardElevation(dpToPx(16));
        floatingCardView.setElevation(dpToPx(16));

        addView(floatingCardView);

        // Animate pickup: scale down to 0.72f to reveal trash can underneath!
        floatingCardView.setScaleX(1.0f);
        floatingCardView.setScaleY(1.0f);
        floatingCardView.setAlpha(0.95f);

        floatingCardView.animate()
                .scaleX(0.72f)
                .scaleY(0.72f)
                .setDuration(180)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public void updateDrag(float touchX, float touchY) {
        if (floatingCardView == null) return;
        getLocationInWindow(overlayLoc);
        floatingCardView.setTranslationX(touchX - overlayLoc[0] - touchOffsetX);
        floatingCardView.setTranslationY(touchY - overlayLoc[1] - touchOffsetY);
    }

    public void dropIntoTarget(Rect targetScreenBounds, Runnable onComplete) {
        if (floatingCardView == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        getLocationInWindow(overlayLoc);
        float targetCenterX = targetScreenBounds.centerX() - overlayLoc[0];
        float targetCenterY = targetScreenBounds.centerY() - overlayLoc[1];

        float destX = targetCenterX - (floatingCardView.getWidth() / 2f);
        float destY = targetCenterY - (floatingCardView.getHeight() / 2f);

        floatingCardView.animate()
                .translationX(destX)
                .translationY(destY)
                .scaleX(0.1f)
                .scaleY(0.1f)
                .alpha(0.0f)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    removeAllViews();
                    setVisibility(View.GONE);
                    floatingCardView = null;
                    if (onComplete != null) onComplete.run();
                })
                .start();
    }

    public void cancelDrag(View sourceItemView, Runnable onComplete) {
        if (floatingCardView == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        getLocationInWindow(overlayLoc);
        int[] sourceLoc = new int[2];
        sourceItemView.getLocationInWindow(sourceLoc);

        float destX = sourceLoc[0] - overlayLoc[0];
        float destY = sourceLoc[1] - overlayLoc[1];

        floatingCardView.animate()
                .translationX(destX)
                .translationY(destY)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    removeAllViews();
                    setVisibility(View.GONE);
                    floatingCardView = null;
                    if (onComplete != null) onComplete.run();
                })
                .start();
    }

    private String formatCardNo(String rawNo) {
        if (rawNo == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rawNo.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append(" ");
            }
            sb.append(rawNo.charAt(i));
        }
        return sb.toString();
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
