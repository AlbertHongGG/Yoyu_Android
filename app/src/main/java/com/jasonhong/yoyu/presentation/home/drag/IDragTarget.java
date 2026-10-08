package com.jasonhong.yoyu.presentation.home.drag;

import android.graphics.Rect;
import com.jasonhong.yoyu.domain.model.CardEntity;

public interface IDragTarget {
    Rect getHitRectOnScreen();
    boolean containsPoint(float screenX, float screenY);
    void onDragStarted();
    void onDragEntered();
    void onDragExited();
    void onDragDropped(CardEntity card);
    void onDragEnded();
}
