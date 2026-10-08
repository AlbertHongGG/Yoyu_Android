package com.jasonhong.yoyu.domain.service;

import androidx.annotation.DrawableRes;
import com.jasonhong.yoyu.R;

public final class ScopeIconMapper {

    private ScopeIconMapper() {}

    @DrawableRes
    public static int getIconForScope(String scopeName) {
        if (scopeName == null) {
            return R.drawable.ic_scope_default;
        }

        switch (scopeName) {
            case "市區公車":
            case "客運":
                return R.drawable.ic_scope_bus;
            case "捷運":
                return R.drawable.ic_scope_mrt;
            case "小額消費":
            case "便利商店":
                return R.drawable.ic_scope_store;
            case "臺鐵":
            case "高鐵":
                return R.drawable.ic_scope_train;
            case "YouBike":
                return R.drawable.ic_scope_bike;
            case "停車場":
                return R.drawable.ic_scope_parking;
            default:
                return R.drawable.ic_scope_default;
        }
    }
}
