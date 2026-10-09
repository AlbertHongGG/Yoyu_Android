package com.jasonhong.yoyu.presentation.home;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.core.widgets.NotificationToast;
import com.jasonhong.yoyu.databinding.DialogAddCardBinding;
import com.jasonhong.yoyu.domain.model.BatchCardOperationResult;
import com.jasonhong.yoyu.domain.model.BatchCardSequenceGenerator;
import com.jasonhong.yoyu.domain.model.CardEntity;

import java.math.BigInteger;
import java.util.Locale;

public class AddCardBottomSheetDialog extends BottomSheetDialogFragment {

    private DialogAddCardBinding binding;
    private HomeViewModel viewModel;
    private boolean isBatchMode = false;

    public static AddCardBottomSheetDialog newInstance() {
        return new AddCardBottomSheetDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupClickListeners();
        setupLivePreview();

        // Initial focus
        binding.etCardNo.requestFocus();
        binding.etCardNo.postDelayed(() -> showKeyboard(binding.etCardNo), 150);
    }

    private void setupClickListeners() {
        binding.btnToggleMode.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            toggleMode();
        });

        binding.btnSubmit.setOnClickListener(v -> submit());
    }

    private void toggleMode() {
        isBatchMode = !isBatchMode;

        if (isBatchMode) {
            binding.layoutSingleInputs.setVisibility(View.GONE);
            binding.layoutBatchInputs.setVisibility(View.VISIBLE);
            binding.tvHeaderTitle.setText(R.string.add_card_batch_title);
            binding.ivToggleMode.setImageResource(R.drawable.ic_credit_card);
            binding.ivToggleMode.setContentDescription(getString(R.string.batch_mode_toggle_single_tooltip));

            // Transfer single input text to batch start card no if present
            String currentNo = binding.etCardNo.getText().toString().trim();
            if (!currentNo.isEmpty() && binding.etBatchStartCardNo.getText().toString().trim().isEmpty()) {
                binding.etBatchStartCardNo.setText(currentNo);
                binding.etBatchStartCardNo.setSelection(currentNo.length());
            }

            updateBatchPreview();
            binding.etBatchStartCardNo.requestFocus();
            showKeyboard(binding.etBatchStartCardNo);
        } else {
            binding.layoutSingleInputs.setVisibility(View.VISIBLE);
            binding.layoutBatchInputs.setVisibility(View.GONE);
            binding.tvHeaderTitle.setText(R.string.add_card);
            binding.ivToggleMode.setImageResource(R.drawable.ic_batch_cards);
            binding.ivToggleMode.setContentDescription(getString(R.string.batch_mode_toggle_batch_tooltip));

            // Transfer batch start card no to single input text if present
            String currentNo = binding.etBatchStartCardNo.getText().toString().trim();
            if (!currentNo.isEmpty() && binding.etCardNo.getText().toString().trim().isEmpty()) {
                binding.etCardNo.setText(currentNo);
                binding.etCardNo.setSelection(currentNo.length());
            }

            binding.etCardNo.requestFocus();
            showKeyboard(binding.etCardNo);
        }
    }

    private void setupLivePreview() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBatchPreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etBatchStartCardNo.addTextChangedListener(watcher);
        binding.etBatchRange.addTextChangedListener(watcher);
        updateBatchPreview();
    }

    private void updateBatchPreview() {
        if (binding == null) return;

        String startNo = binding.etBatchStartCardNo.getText().toString().trim();
        String rangeStr = binding.etBatchRange.getText().toString().trim();

        int count = 0;
        if (!rangeStr.isEmpty()) {
            try {
                count = Integer.parseInt(rangeStr);
            } catch (NumberFormatException ignored) {}
        }

        if (count > BatchCardSequenceGenerator.MAX_BATCH_COUNT) {
            binding.tvPreviewCount.setText("超過上限 (最多 " + BatchCardSequenceGenerator.MAX_BATCH_COUNT + " 張)");
            binding.tvPreviewRange.setText("—");
            return;
        }

        if (startNo.isEmpty() || count <= 0) {
            binding.tvPreviewCount.setText("共 " + Math.max(count, 0) + " 張");
            binding.tvPreviewRange.setText("—");
            return;
        }

        binding.tvPreviewCount.setText("共 " + count + " 張");

        try {
            String endNo = BatchCardSequenceGenerator.calculateEndCardNo(startNo, count);
            if (count == 1) {
                binding.tvPreviewRange.setText(startNo);
            } else {
                binding.tvPreviewRange.setText(startNo + " ~ " + endNo);
            }
        } catch (Exception e) {
            // Prospective calculation while user is still completing input digits
            if (startNo.matches("^\\d+$")) {
                BigInteger start = new BigInteger(startNo);
                BigInteger end = start.add(BigInteger.valueOf(count - 1));
                String endNo = String.format(Locale.US, "%0" + startNo.length() + "d", end);
                binding.tvPreviewRange.setText(startNo + " ~ " + endNo);
            } else {
                binding.tvPreviewRange.setText("—");
            }
        }
    }

    private void submit() {
        hideKeyboard();
        if (isBatchMode) {
            submitBatch();
        } else {
            submitSingle();
        }
    }

    private void submitSingle() {
        String cardNo = binding.etCardNo.getText().toString().trim();
        String cardName = binding.etCardName.getText().toString().trim();

        if (cardNo.isEmpty()) {
            NotificationToast.showWarning(requireActivity(), "請輸入卡號");
            return;
        }

        binding.btnSubmit.setVisibility(View.GONE);
        binding.pbLoading.setVisibility(View.VISIBLE);
        binding.btnToggleMode.setEnabled(false);
        binding.etCardNo.setEnabled(false);
        binding.etCardName.setEnabled(false);
        setCancelable(false);

        viewModel.addCard(cardNo, cardName, new HomeViewModel.AddCardCallback() {
            @Override
            public void onSuccess(CardEntity card) {
                if (getContext() == null) return;
                NotificationToast.showSuccess(requireActivity(), "新增卡片成功");
                dismiss();
            }

            @Override
            public void onError(String message) {
                if (getContext() == null) return;
                binding.pbLoading.setVisibility(View.GONE);
                binding.btnSubmit.setVisibility(View.VISIBLE);
                binding.btnToggleMode.setEnabled(true);
                binding.etCardNo.setEnabled(true);
                binding.etCardName.setEnabled(true);
                setCancelable(true);
                NotificationToast.showError(requireActivity(), message != null ? message : "新增失敗");
            }
        });
    }

    private void submitBatch() {
        String startCardNo = binding.etBatchStartCardNo.getText().toString().trim();
        String rangeStr = binding.etBatchRange.getText().toString().trim();

        if (startCardNo.isEmpty()) {
            NotificationToast.showWarning(requireActivity(), "請輸入起始卡號");
            return;
        }

        if (rangeStr.isEmpty()) {
            NotificationToast.showWarning(requireActivity(), "請輸入連續數量");
            return;
        }

        int range;
        try {
            range = Integer.parseInt(rangeStr);
        } catch (NumberFormatException e) {
            NotificationToast.showWarning(requireActivity(), "請輸入有效的數量");
            return;
        }

        try {
            BatchCardSequenceGenerator.validate(startCardNo, range);
        } catch (IllegalArgumentException e) {
            NotificationToast.showWarning(requireActivity(), e.getMessage());
            return;
        }

        // Lock UI and show determinate progress
        binding.btnSubmit.setVisibility(View.GONE);
        binding.btnToggleMode.setEnabled(false);
        binding.etBatchStartCardNo.setEnabled(false);
        binding.etBatchRange.setEnabled(false);
        setCancelable(false);

        binding.layoutBatchProgress.setVisibility(View.VISIBLE);
        binding.pbBatchProgress.setMax(range);
        binding.pbBatchProgress.setProgress(0);
        binding.tvBatchProgress.setText("準備中 (0/" + range + ")...");

        viewModel.batchAddCards(startCardNo, range, new HomeViewModel.BatchCallback() {
            @Override
            public void onProgress(int processed, int total, String message) {
                if (binding == null) return;
                binding.pbBatchProgress.setMax(total);
                binding.pbBatchProgress.setProgress(processed);
                binding.tvBatchProgress.setText(message);
            }

            @Override
            public void onSuccess(BatchCardOperationResult result) {
                if (getContext() == null) return;
                StringBuilder sb = new StringBuilder();
                sb.append("批量新增成功：共 ").append(result.getSuccessCount()).append(" 張");
                if (result.getDuplicateCount() > 0) {
                    sb.append(" (跳過 ").append(result.getDuplicateCount()).append(" 張重複)");
                }
                if (result.getFailedCount() > 0) {
                    sb.append(" (").append(result.getFailedCount()).append(" 張無效)");
                }

                if (result.hasAnySuccess()) {
                    NotificationToast.showSuccess(requireActivity(), sb.toString());
                } else if (result.getDuplicateCount() > 0) {
                    NotificationToast.showInfo(requireActivity(), "所選卡號皆已在清單中，無須重複新增");
                } else {
                    NotificationToast.showError(requireActivity(), "驗證失敗，查無卡片資料");
                }
                dismiss();
            }

            @Override
            public void onError(String message) {
                if (binding == null || getContext() == null) return;
                binding.layoutBatchProgress.setVisibility(View.GONE);
                binding.btnSubmit.setVisibility(View.VISIBLE);
                binding.btnToggleMode.setEnabled(true);
                binding.etBatchStartCardNo.setEnabled(true);
                binding.etBatchRange.setEnabled(true);
                setCancelable(true);
                NotificationToast.showError(requireActivity(), message != null ? message : "批量新增失敗");
            }
        });
    }

    private void showKeyboard(View view) {
        if (getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
