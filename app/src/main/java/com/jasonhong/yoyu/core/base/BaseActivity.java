package com.jasonhong.yoyu.core.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.jasonhong.yoyu.core.widgets.NotificationToast;
import com.jasonhong.yoyu.core.widgets.swipeback.SwipeBackLayout;
import com.jasonhong.yoyu.core.widgets.swipeback.SwipeBackListener;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    protected VB binding;
    protected SwipeBackLayout swipeBackLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());
        if (enableSwipeBack()) {
            setupSwipeBack();
        }
        initView();
        initData();
    }

    protected boolean enableSwipeBack() {
        return true;
    }

    private void setupSwipeBack() {
        swipeBackLayout = new SwipeBackLayout(this);
        swipeBackLayout.attachToActivity(this, new SwipeBackListener() {
            @Override
            public void onSwipeProgress(float progress) {
                // Hook for subclass if needed
            }

            @Override
            public void onSwipeCancel() {
                // Hook for subclass if needed
            }

            @Override
            public void onSwipeFinished() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    protected abstract VB inflateBinding(LayoutInflater inflater);

    protected abstract void initView();

    protected void initData() {
        // Optional override
    }

    public void showSuccess(@NonNull String message) {
        NotificationToast.showSuccess(this, message);
    }

    public void showError(@NonNull String message) {
        NotificationToast.showError(this, message);
    }

    public void showWarning(@NonNull String message) {
        NotificationToast.showWarning(this, message);
    }

    public void showInfo(@NonNull String message) {
        NotificationToast.showInfo(this, message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
