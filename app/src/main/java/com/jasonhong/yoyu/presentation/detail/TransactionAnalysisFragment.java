package com.jasonhong.yoyu.presentation.detail;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.databinding.FragmentTransactionAnalysisBinding;
import com.jasonhong.yoyu.domain.model.AnalysisDataType;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Fragment responsible for displaying the transaction analysis overview, segmented controls,
 * and category breakdown list.
 * Shares the DetailViewModel scoped to the parent Activity.
 */
public class TransactionAnalysisFragment extends Fragment {

    private FragmentTransactionAnalysisBinding binding;
    private DetailViewModel viewModel;
    private AnalysisCategoryAdapter adapter;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.TAIWAN);

    public static TransactionAnalysisFragment newInstance() {
        return new TransactionAnalysisFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionAnalysisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);

        viewModel = new ViewModelProvider(requireActivity()).get(DetailViewModel.class);

        adapter = new AnalysisCategoryAdapter();
        binding.rvAnalysisCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAnalysisCategories.setAdapter(adapter);

        setupSegmentedButtons();
        observeViewModel();
    }

    private void setupSegmentedButtons() {
        binding.btnSegmentExpense.setOnClickListener(v -> viewModel.setAnalysisType(AnalysisDataType.EXPENSE));
        binding.btnSegmentTopUp.setOnClickListener(v -> viewModel.setAnalysisType(AnalysisDataType.TOP_UP));
        binding.btnSegmentAutoTopUp.setOnClickListener(v -> viewModel.setAnalysisType(AnalysisDataType.AUTO_TOP_UP));
    }

    private void observeViewModel() {
        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), total -> {
            binding.tvTotalExpense.setText(currencyFormat.format(total != null ? total : 0));
        });

        viewModel.getTotalTopUp().observe(getViewLifecycleOwner(), total -> {
            binding.tvTotalTopUp.setText(currencyFormat.format(total != null ? total : 0));
        });

        viewModel.getTotalAutoTopUp().observe(getViewLifecycleOwner(), total -> {
            binding.tvTotalAutoTopUp.setText(currencyFormat.format(total != null ? total : 0));
        });

        viewModel.getCurrentAnalysisType().observe(getViewLifecycleOwner(), type -> {
            updateSegmentedUI(type);
            updateAnalysisAdapter();
        });

        viewModel.getCurrentCategories().observe(getViewLifecycleOwner(), categories -> {
            updateAnalysisAdapter();
            binding.tvAnalysisEmpty.setVisibility((categories == null || categories.isEmpty()) ? View.VISIBLE : View.GONE);
        });
    }

    private void updateSegmentedUI(AnalysisDataType type) {
        if (type == null) type = AnalysisDataType.EXPENSE;

        resetSegmentButton(binding.btnSegmentExpense, type == AnalysisDataType.EXPENSE);
        resetSegmentButton(binding.btnSegmentTopUp, type == AnalysisDataType.TOP_UP);
        resetSegmentButton(binding.btnSegmentAutoTopUp, type == AnalysisDataType.AUTO_TOP_UP);
    }

    private void resetSegmentButton(TextView button, boolean isSelected) {
        if (getContext() == null) return;
        if (isSelected) {
            button.setBackgroundResource(R.drawable.bg_segmented_selected);
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light));
            button.setTypeface(null, Typeface.BOLD);
            button.setElevation(dpToPx(2));
        } else {
            button.setBackground(null);
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary_light));
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

        adapter.submitData(viewModel.getCurrentCategories().getValue(), total, type);
    }

    private float dpToPx(float dp) {
        if (getContext() == null) return dp;
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
