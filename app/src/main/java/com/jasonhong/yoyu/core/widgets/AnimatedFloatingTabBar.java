package com.jasonhong.yoyu.core.widgets;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.jasonhong.yoyu.R;

/**
 * Pixel-perfect reproduction of Flutter's AnimatedFloatingTabBar:
 * Floating pill bar (30dp radius, 6dp padding, soft shadow).
 * Items:
 * - Selected: 26dp radius pill (#1A475D8E), Icon + Text, primary color (#475D8E).
 * - Unselected: Transparent, Text COLLAPSED (Gone), ONLY Icon (20dp, #757575).
 * Icons: ic_list_alt_rounded & ic_pie_chart_outline_rounded.
 */
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
        setElevation(dpToPx(8));

        // Enable smooth expanding / collapsing transition
        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        transition.setDuration(250);
        setLayoutTransition(transition);

        int pad = dpToPx(6);
        setPadding(pad, pad, pad, pad);

        // Tab 0: 交易紀錄 (ic_list_alt_rounded)
        tab0 = createTabItem(context, R.drawable.ic_list_alt_rounded, "交易紀錄");
        icon0 = (ImageView) tab0.getChildAt(0);
        label0 = (TextView) tab0.getChildAt(1);

        // Tab 1: 分析 (ic_pie_chart_outline_rounded)
        tab1 = createTabItem(context, R.drawable.ic_pie_chart_outline_rounded, "分析");
        icon1 = (ImageView) tab1.getChildAt(0);
        label1 = (TextView) tab1.getChildAt(1);

        addView(tab0);
        addView(tab1);

        tab0.setOnClickListener(v -> setSelectedIndex(0, true));
        tab1.setOnClickListener(v -> setSelectedIndex(1, true));

        updateTabState();
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
        updateTabState();
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

    private void updateTabState() {
        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        int unselectedColor = ContextCompat.getColor(getContext(), isDark ? R.color.text_secondary_dark : R.color.text_secondary_light);

        // Tab 0
        boolean is0 = selectedIndex == 0;
        tab0.setBackgroundResource(is0 ? R.drawable.bg_floating_tab_selected : 0);
        tab0.setPadding(dpToPx(is0 ? 20 : 16), dpToPx(10), dpToPx(is0 ? 20 : 16), dpToPx(10));
        ImageViewCompat.setImageTintList(icon0, ColorStateList.valueOf(is0 ? primaryColor : unselectedColor));
        label0.setTextColor(primaryColor);
        label0.setVisibility(is0 ? View.VISIBLE : View.GONE);

        // Tab 1
        boolean is1 = selectedIndex == 1;
        tab1.setBackgroundResource(is1 ? R.drawable.bg_floating_tab_selected : 0);
        tab1.setPadding(dpToPx(is1 ? 20 : 16), dpToPx(10), dpToPx(is1 ? 20 : 16), dpToPx(10));
        ImageViewCompat.setImageTintList(icon1, ColorStateList.valueOf(is1 ? primaryColor : unselectedColor));
        label1.setTextColor(primaryColor);
        label1.setVisibility(is1 ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
