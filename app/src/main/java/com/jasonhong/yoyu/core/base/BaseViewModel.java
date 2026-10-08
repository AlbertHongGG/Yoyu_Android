package com.jasonhong.yoyu.core.base;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel extends ViewModel {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    protected void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }
}
