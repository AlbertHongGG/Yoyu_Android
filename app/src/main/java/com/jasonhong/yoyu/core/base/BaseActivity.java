package com.jasonhong.yoyu.core.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.jasonhong.yoyu.core.widgets.NotificationToast;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    protected VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        initData();
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
