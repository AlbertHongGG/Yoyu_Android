package com.jasonhong.yoyu.domain.model;

public class CategoryUIModel {

    private final String scopeName;
    private final int amount;
    private final int count;
    private final int iconResId;

    public CategoryUIModel(String scopeName, int amount, int count, int iconResId) {
        this.scopeName = scopeName != null ? scopeName : "";
        this.amount = amount;
        this.count = count;
        this.iconResId = iconResId;
    }

    public String getScopeName() {
        return scopeName;
    }

    public int getAmount() {
        return amount;
    }

    public int getCount() {
        return count;
    }

    public int getIconResId() {
        return iconResId;
    }
}
