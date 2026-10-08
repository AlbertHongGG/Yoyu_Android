package com.jasonhong.yoyu.presentation.home;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jasonhong.yoyu.core.widgets.NotificationToast;
import com.jasonhong.yoyu.databinding.DialogAddCardBinding;
import com.jasonhong.yoyu.domain.model.CardEntity;

public class AddCardBottomSheetDialog extends BottomSheetDialogFragment {

    private DialogAddCardBinding binding;
    private HomeViewModel viewModel;

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

        binding.btnSubmit.setOnClickListener(v -> submit());

        // Auto focus card number field
        binding.etCardNo.requestFocus();
        binding.etCardNo.postDelayed(() -> {
            if (getContext() != null) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(binding.etCardNo, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 150);
    }

    private void submit() {
        String cardNo = binding.etCardNo.getText().toString().trim();
        String cardName = binding.etCardName.getText().toString().trim();

        if (cardNo.isEmpty()) {
            NotificationToast.showWarning(requireActivity(), "請輸入卡號");
            return;
        }

        binding.btnSubmit.setVisibility(View.GONE);
        binding.pbLoading.setVisibility(View.VISIBLE);
        binding.etCardNo.setEnabled(false);
        binding.etCardName.setEnabled(false);

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
                binding.etCardNo.setEnabled(true);
                binding.etCardName.setEnabled(true);
                NotificationToast.showError(requireActivity(), message != null ? message : "新增失敗");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
