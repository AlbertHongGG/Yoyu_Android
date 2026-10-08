package com.jasonhong.yoyu.presentation.home.drag;

import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.jasonhong.yoyu.domain.model.CardEntity;

public class CardDragCoordinator {

    private final CardDragOverlayView overlayView;
    private final IDragTarget dragTarget;
    private final RecyclerView recyclerView;

    private boolean isDragging = false;
    private View activeSourceView;
    private CardEntity activeCard;

    public CardDragCoordinator(CardDragOverlayView overlayView, IDragTarget dragTarget, RecyclerView recyclerView) {
        this.overlayView = overlayView;
        this.dragTarget = dragTarget;
        this.recyclerView = recyclerView;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void startDrag(View sourceItemView, CardEntity card, float touchX, float touchY) {
        isDragging = true;
        activeSourceView = sourceItemView;
        activeCard = card;

        recyclerView.requestDisallowInterceptTouchEvent(true);
        activeSourceView.setAlpha(0.2f);

        overlayView.startDrag(sourceItemView, card, touchX, touchY);
        dragTarget.onDragStarted();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isDragging) return false;

        float screenX = event.getRawX();
        float screenY = event.getRawY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                overlayView.updateDrag(screenX, screenY);
                if (dragTarget.containsPoint(screenX, screenY)) {
                    dragTarget.onDragEntered();
                } else {
                    dragTarget.onDragExited();
                }
                return true;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                if (dragTarget.containsPoint(screenX, screenY)) {
                    dragTarget.onDragDropped(activeCard);
                    View sourceViewToRestore = activeSourceView;
                    overlayView.dropIntoTarget(dragTarget.getHitRectOnScreen(), () -> {
                        if (sourceViewToRestore != null) {
                            sourceViewToRestore.setAlpha(1.0f);
                        }
                    });
                } else {
                    dragTarget.onDragEnded();
                    View sourceViewToRestore = activeSourceView;
                    overlayView.cancelDrag(activeSourceView, () -> {
                        if (sourceViewToRestore != null) {
                            sourceViewToRestore.setAlpha(1.0f);
                        }
                    });
                }
                activeSourceView = null;
                activeCard = null;
                return true;

            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                dragTarget.onDragEnded();
                View sourceViewToRestore = activeSourceView;
                overlayView.cancelDrag(activeSourceView, () -> {
                    if (sourceViewToRestore != null) {
                        sourceViewToRestore.setAlpha(1.0f);
                    }
                });
                activeSourceView = null;
                activeCard = null;
                return true;

            default:
                return true;
        }
    }
}
