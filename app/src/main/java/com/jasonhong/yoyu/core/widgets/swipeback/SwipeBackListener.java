package com.jasonhong.yoyu.core.widgets.swipeback;

public interface SwipeBackListener {
    void onSwipeProgress(float progress);
    void onSwipeCancel();
    void onSwipeFinished();
}
