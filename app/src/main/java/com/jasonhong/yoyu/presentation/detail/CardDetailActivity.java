package com.jasonhong.yoyu.presentation.detail;

import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.core.base.BaseActivity;
import com.jasonhong.yoyu.databinding.ActivityCardDetailBinding;
import com.jasonhong.yoyu.domain.model.CardEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CardDetailActivity extends BaseActivity<ActivityCardDetailBinding> {

    private DetailViewModel viewModel;
    private CardDetailPagerAdapter pagerAdapter;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

    @Override
    protected ActivityCardDetailBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCardDetailBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        CardEntity card = (CardEntity) getIntent().getSerializableExtra("card");
        if (card == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        // Setup ViewPager2 with FragmentStateAdapter
        pagerAdapter = new CardDetailPagerAdapter(this);
        binding.viewPagerDetail.setAdapter(pagerAdapter);
        binding.viewPagerDetail.setUserInputEnabled(false); // Tab switching driven by bottom bar
        binding.viewPagerDetail.setOffscreenPageLimit(1);   // Retain both tab pages in memory

        // Sync ViewPager2 with FloatingTabBar
        binding.floatingTabBar.setOnTabSelectedListener(this::switchTab);
        binding.viewPagerDetail.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.floatingTabBar.setSelectedIndex(position);
            }
        });

        // Actions
        binding.btnFilterPartner.setOnClickListener(v -> showPartnerFilterDialog());
        binding.btnDateRange.setOnClickListener(v -> showDateRangeDialog());
        binding.btnSearch.setOnClickListener(v -> handleSearchClick());

        viewModel.init(card.getCardNo());
    }

    @Override
    protected void initData() {
        // Date range display
        viewModel.getStartDate().observe(this, sDate -> updateDateRangeDisplay());
        viewModel.getEndDate().observe(this, eDate -> updateDateRangeDisplay());

        // Search query title
        viewModel.getSearchQuery().observe(this, query -> {
            if (query != null && !query.trim().isEmpty()) {
                binding.layoutTitle.setVisibility(View.GONE);
                binding.tvSearchTitle.setVisibility(View.VISIBLE);
                binding.tvSearchTitle.setText("搜尋: " + query);
                binding.btnSearch.setImageResource(R.drawable.ic_close);
            } else {
                binding.layoutTitle.setVisibility(View.VISIBLE);
                binding.tvSearchTitle.setVisibility(View.GONE);
                binding.btnSearch.setImageResource(R.drawable.ic_search_rounded);
            }
        });

        // Partner filter badge dot
        viewModel.getPartnerFilter().observe(this, partner -> {
            boolean hasPartnerFilter = partner != null && !partner.isEmpty();
            binding.dotFilterActive.setVisibility(hasPartnerFilter ? View.VISIBLE : View.GONE);
        });
    }

    private void updateDateRangeDisplay() {
        Date sDate = viewModel.getStartDate().getValue();
        Date eDate = viewModel.getEndDate().getValue();
        if (sDate != null) {
            binding.tvDateStart.setText(dateFormat.format(sDate));
        }
        if (eDate != null) {
            binding.tvDateEnd.setText("~ " + dateFormat.format(eDate));
        }
    }

    private void switchTab(int index) {
        if (binding.viewPagerDetail.getCurrentItem() != index) {
            triggerHaptic();
            binding.viewPagerDetail.setCurrentItem(index, false);
        }
    }

    private void showPartnerFilterDialog() {
        PartnerFilterBottomSheetDialog dialog = PartnerFilterBottomSheetDialog.newInstance(
                viewModel.getAvailablePartners().getValue(),
                viewModel.getPartnerFilter().getValue(),
                partner -> viewModel.setPartnerFilter(partner)
        );
        dialog.show(getSupportFragmentManager(), "PartnerFilterDialog");
    }

    private void showDateRangeDialog() {
        DateRangeBottomSheetDialog dialog = DateRangeBottomSheetDialog.newInstance(
                viewModel.getStartDate().getValue(),
                viewModel.getEndDate().getValue(),
                (sDate, eDate) -> viewModel.setDateRange(sDate, eDate)
        );
        dialog.show(getSupportFragmentManager(), "DateRangeDialog");
    }

    private void handleSearchClick() {
        String currentQuery = viewModel.getSearchQuery().getValue();
        if (currentQuery != null && !currentQuery.trim().isEmpty()) {
            viewModel.setSearchQuery("");
        } else {
            SearchDialog dialog = SearchDialog.newInstance(
                    currentQuery,
                    new SearchDialog.SearchCallback() {
                        @Override
                        public void onSearch(String query) {
                            viewModel.setSearchQuery(query);
                        }

                        @Override
                        public void onClear() {
                            viewModel.setSearchQuery("");
                        }
                    }
            );
            dialog.show(getSupportFragmentManager(), "SearchDialog");
        }
    }

    private void triggerHaptic() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(15);
            }
        }
    }
}
