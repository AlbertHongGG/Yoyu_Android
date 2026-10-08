package com.jasonhong.yoyu;

import android.app.Application;

public class YoyuApplication extends Application {
    private static YoyuApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static YoyuApplication getInstance() {
        return instance;
    }
}
