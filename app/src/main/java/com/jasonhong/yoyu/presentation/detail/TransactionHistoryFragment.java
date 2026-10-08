package com.jasonhong.yoyu.presentation.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jasonhong.yoyu.core.base.Resource;
import com.jasonhong.yoyu.core.widgets.NotificationToast;
import com.jasonhong.yoyu.databinding.FragmentTransactionHistoryBinding;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;

import java.util.List;

/**
 * Fragment responsible for displaying the transaction history list and handling pull-to-refresh.
 * Shares the DetailViewModel scoped to the parent Activity.
 */
public class TransactionHistoryFragment extends Fragment {

    private FragmentTransactionHistoryBinding binding;
    private DetailViewModel viewModel;
    private TransactionAdapter adapter;

    public static TransactionHistoryFragment newInstance() {
        return new TransactionHistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DetailViewModel.class);

        adapter = new TransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getRawTransactionsResource().observe(getViewLifecycleOwner(), res -> {
            if (res == null) return;
            if (res.getStatus() == Resource.Status.LOADING) {
                adapter.setLoading(true);
                binding.tvTransactionsEmpty.setVisibility(View.GONE);
            } else if (res.getStatus() == Resource.Status.SUCCESS) {
                adapter.setLoading(false);
                List<YoyuTransaction> currentList = viewModel.getFilteredTransactions().getValue();
                binding.tvTransactionsEmpty.setVisibility((currentList == null || currentList.isEmpty()) ? View.VISIBLE : View.GONE);
            } else if (res.getStatus() == Resource.Status.ERROR) {
                adapter.setLoading(false);
                if (res.getMessage() != null && getContext() != null) {
                    NotificationToast.showError(requireActivity(), res.getMessage());
                }
            }
        });

        viewModel.getFilteredTransactions().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            if (!adapter.isLoading()) {
                binding.tvTransactionsEmpty.setVisibility((list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);
            } else {
                binding.tvTransactionsEmpty.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
