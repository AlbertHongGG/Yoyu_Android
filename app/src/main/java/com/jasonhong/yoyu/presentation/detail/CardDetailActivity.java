package com.jasonhong.yoyu.presentation.detail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.core.base.BaseActivity;
import com.jasonhong.yoyu.core.base.Resource;
import com.jasonhong.yoyu.core.widgets.NotificationToast;
import com.jasonhong.yoyu.databinding.ActivityCardDetailBinding;
import com.jasonhong.yoyu.domain.model.AnalysisDataType;
import com.jasonhong.yoyu.domain.model.CardEntity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CardDetailActivity extends BaseActivity<ActivityCardDetailBinding> {

    private DetailViewModel viewModel;
    private TransactionAdapter transactionAdapter;
    private AnalysisCategoryAdapter analysisAdapter;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.TAIWAN);

    private int currentTabIndex = 0;

    @Override
    protected ActivityCardDetailBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCardDetailBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);

        CardEntity card = (CardEntity) getIntent().getSerializableExtra("card");
        if (card == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        // Transactions list (Tab 0)
        transactionAdapter = new TransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactions.setAdapter(transactionAdapter);

        // Analysis list (Tab 1)
        analysisAdapter = new AnalysisCategoryAdapter();
        binding.layoutAnalysis.rvAnalysisCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.layoutAnalysis.rvAnalysisCategories.setAdapter(analysisAdapter);

        // Swipe refresh
        binding.swipeRefreshDetail.setOnRefreshListener(() -> viewModel.loadData());

        // Floating Tab Bar
        binding.floatingTabBar.setOnTabSelectedListener(this::switchTab);

        // Actions
        binding.btnFilterPartner.setOnClickListener(v -> showPartnerFilterDialog());
        binding.btnDateRange.setOnClickListener(v -> showDateRangeDialog());
        binding.btnSearch.setOnClickListener(v -> handleSearchClick());

        // Segmented buttons in Analysis tab
        binding.layoutAnalysis.btnSegmentExpense.setOnClickListener(v -> viewModel.setAnalysisType(AnalysisDataType.EXPENSE));
        binding.layoutAnalysis.btnSegmentTopUp.setOnClickListener(v -> viewModel.setAnalysisType(AnalysisDataType.TOP_UP));
        binding.layoutAnalysis.btnSegmentAutoTopUp.setOnClickListener(v -> viewModel.setAnalysisType(AnalysisDataType.AUTO_TOP_UP));

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
                binding.btnSearch.setImageResource(R.drawable.ic_search);
            }
        });

        // Partner filter badge dot
        viewModel.getPartnerFilter().observe(this, partner -> {
            boolean hasPartnerFilter = partner != null && !partner.isEmpty();
            binding.dotFilterActive.setVisibility(hasPartnerFilter ? View.VISIBLE : View.GONE);
        });

        // Transactions state
        viewModel.getRawTransactionsResource().observe(this, res -> {
            if (res == null) return;
            binding.swipeRefreshDetail.setRefreshing(res.getStatus() == Resource.Status.LOADING);
            if (res.getStatus() == Resource.Status.ERROR && res.getMessage() != null) {
                NotificationToast.showError(this, res.getMessage());
            }
        });

        viewModel.getFilteredTransactions().observe(this, list -> {
            transactionAdapter.submitList(list);
            binding.tvTransactionsEmpty.setVisibility((list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);
        });

        // Analysis state
        viewModel.getTotalExpense().observe(this, total -> {
            binding.layoutAnalysis.tvTotalExpense.setText(currencyFormat.format(total != null ? total : 0));
        });

        viewModel.getTotalTopUp().observe(this, total -> {
            binding.layoutAnalysis.tvTotalTopUp.setText(currencyFormat.format(total != null ? total : 0));
        });

        viewModel.getTotalAutoTopUp().observe(this, total -> {
            binding.layoutAnalysis.tvTotalAutoTopUp.setText(currencyFormat.format(total != null ? total : 0));
        });

        viewModel.getCurrentAnalysisType().observe(this, type -> {
            updateSegmentedUI(type);
            updateAnalysisAdapter();
        });

        viewModel.getCurrentCategories().observe(this, categories -> {
            updateAnalysisAdapter();
            binding.layoutAnalysis.tvAnalysisEmpty.setVisibility((categories == null || categories.isEmpty()) ? View.VISIBLE : View.GONE);
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
        if (currentTabIndex == index) return;
        currentTabIndex = index;

        triggerHaptic();

        if (index == 0) {
            // Show Tab 0 (Transactions)
            binding.swipeRefreshDetail.setVisibility(View.VISIBLE);
            binding.swipeRefreshDetail.setAlpha(0f);
            binding.swipeRefreshDetail.animate().alpha(1f).setDuration(250).start();

            binding.layoutAnalysis.getRoot().animate().alpha(0f).setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            binding.layoutAnalysis.getRoot().setVisibility(View.GONE);
                        }
                    }).start();
        } else {
            // Show Tab 1 (Analysis)
            binding.layoutAnalysis.getRoot().setVisibility(View.VISIBLE);
            binding.layoutAnalysis.getRoot().setAlpha(0f);
            binding.layoutAnalysis.getRoot().animate().alpha(1f).setDuration(250).start();

            binding.swipeRefreshDetail.animate().alpha(0f).setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            binding.swipeRefreshDetail.setVisibility(View.GONE);
                        }
                    }).start();
        }
    }

    private void updateSegmentedUI(AnalysisDataType type) {
        if (type == null) type = AnalysisDataType.EXPENSE;

        resetSegmentButton(binding.layoutAnalysis.btnSegmentExpense, type == AnalysisDataType.EXPENSE);
        resetSegmentButton(binding.layoutAnalysis.btnSegmentTopUp, type == AnalysisDataType.TOP_UP);
        resetSegmentButton(binding.layoutAnalysis.btnSegmentAutoTopUp, type == AnalysisDataType.AUTO_TOP_UP);
    }

    private void resetSegmentButton(TextView button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundResource(R.drawable.bg_segmented_selected);
            button.setTextColor(ContextCompat.getColor(this, R.color.text_primary_light));
            button.setTypeface(null, Typeface.BOLD);
            button.setElevation(dpToPx(2));
        } else {
            button.setBackground(null);
            button.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_light));
            button.setTypeface(null, Typeface.NORMAL);
            button.setElevation(0);
        }
    }

    private void updateAnalysisAdapter() {
        AnalysisDataType type = viewModel.getCurrentAnalysisType().getValue();
        if (type == null) type = AnalysisDataType.EXPENSE;

        int total = 0;
        switch (type) {
            case EXPENSE:
                Integer exp = viewModel.getTotalExpense().getValue();
                total = exp != null ? exp : 0;
                break;
            case TOP_UP:
                Integer top = viewModel.getTotalTopUp().getValue();
                total = top != null ? top : 0;
                break;
            case AUTO_TOP_UP:
                Integer auto = viewModel.getTotalAutoTopUp().getValue();
                total = auto != null ? auto : 0;
                break;
        }

        analysisAdapter.submitData(viewModel.getCurrentCategories().getValue(), total, type);
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

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
