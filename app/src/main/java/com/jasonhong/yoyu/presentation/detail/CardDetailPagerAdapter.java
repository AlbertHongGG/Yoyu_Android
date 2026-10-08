package com.jasonhong.yoyu.presentation.detail;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter for CardDetailActivity's ViewPager2.
 * Holds Tab 0 (Transaction History) and Tab 1 (Transaction Analysis).
 */
public class CardDetailPagerAdapter extends FragmentStateAdapter {

    public static final int TAB_HISTORY = 0;
    public static final int TAB_ANALYSIS = 1;
    public static final int TAB_COUNT = 2;

    public CardDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == TAB_HISTORY) {
            return TransactionHistoryFragment.newInstance();
        } else {
            return TransactionAnalysisFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
