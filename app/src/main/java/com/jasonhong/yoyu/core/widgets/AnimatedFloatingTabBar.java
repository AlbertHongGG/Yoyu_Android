package com.jasonhong.yoyu.core.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.jasonhong.yoyu.R;

public class AnimatedFloatingTabBar extends LinearLayout {

    public interface OnTabSelectedListener {
        void onTabSelected(int index);
    }

    private OnTabSelectedListener listener;
    private int selectedIndex = 0;

    private LinearLayout tab0;
    private ImageView icon0;
    private TextView label0;

    private LinearLayout tab1;
    private ImageView icon1;
    private TextView label1;

    public AnimatedFloatingTabBar(Context context) {
        super(context);
        init(context);
    }

    public AnimatedFloatingTabBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnimatedFloatingTabBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setBackgroundResource(R.drawable.bg_floating_tab_bar);
        setElevation(16f);

        int pad = dpToPx(6);
        setPadding(pad, pad, pad, pad);

        // Tab 0: 交易紀錄
        tab0 = createTabItem(context, R.drawable.ic_list, "交易紀錄");
        icon0 = (ImageView) tab0.getChildAt(0);
        label0 = (TextView) tab0.getChildAt(1);

        // Tab 1: 分析
        tab1 = createTabItem(context, R.drawable.ic_pie_chart, "分析");
        icon1 = (ImageView) tab1.getChildAt(0);
        label1 = (TextView) tab1.getChildAt(1);

        addView(tab0);
        addView(tab1);

        tab0.setOnClickListener(v -> setSelectedIndex(0, true));
        tab1.setOnClickListener(v -> setSelectedIndex(1, true));

        updateTabState(false);
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, false);
    }

    public void setSelectedIndex(int index, boolean notify) {
        if (this.selectedIndex == index) return;
        this.selectedIndex = index;
        updateTabState(true);
        if (notify && listener != null) {
            listener.onTabSelected(index);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    private LinearLayout createTabItem(Context context, int iconRes, String labelText) {
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(HORIZONTAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10));

        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        LayoutParams iconLp = new LayoutParams(dpToPx(20), dpToPx(20));
        icon.setLayoutParams(iconLp);

        TextView label = new TextView(context);
        label.setText(labelText);
        label.setTextSize(14);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        LayoutParams labelLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelLp.setMarginStart(dpToPx(6));
        label.setLayoutParams(labelLp);

        item.addView(icon);
        item.addView(label);

        LayoutParams itemLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        itemLp.setMargins(dpToPx(2), 0, dpToPx(2), 0);
        item.setLayoutParams(itemLp);

        return item;
    }

    private void updateTabState(boolean animate) {
        int primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        int unselectedColor = ContextCompat.getColor(getContext(), R.color.text_secondary_light);

        // Apply Tab 0
        boolean isTab0Selected = selectedIndex == 0;
        tab0.setBackgroundResource(isTab0Selected ? R.drawable.bg_floating_tab_selected : 0);
        ImageViewCompat.setImageTintList(icon0, ColorStateList.valueOf(isTab0Selected ? primaryColor : unselectedColor));
        label0.setTextColor(primaryColor);
        label0.setVisibility(isTab0Selected ? View.VISIBLE : View.GONE);

        // Apply Tab 1
        boolean isTab1Selected = selectedIndex == 1;
        tab1.setBackgroundResource(isTab1Selected ? R.drawable.bg_floating_tab_selected : 0);
        ImageViewCompat.setImageTintList(icon1, ColorStateList.valueOf(isTab1Selected ? primaryColor : unselectedColor));
        label1.setTextColor(primaryColor);
        label1.setVisibility(isTab1Selected ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
