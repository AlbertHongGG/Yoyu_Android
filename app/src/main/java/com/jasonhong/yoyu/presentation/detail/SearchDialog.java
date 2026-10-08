package com.jasonhong.yoyu.presentation.detail;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jasonhong.yoyu.databinding.DialogSearchBinding;

public class SearchDialog extends DialogFragment {

    public interface SearchCallback {
        void onSearch(String query);
        void onClear();
    }

    private DialogSearchBinding binding;
    private String initialQuery = "";
    private SearchCallback callback;

    public static SearchDialog newInstance(String initialQuery, SearchCallback callback) {
        SearchDialog dialog = new SearchDialog();
        dialog.initialQuery = initialQuery != null ? initialQuery : "";
        dialog.callback = callback;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.etSearchQuery.setText(initialQuery);
        binding.etSearchQuery.setSelection(initialQuery.length());
        binding.btnClearSearch.setVisibility(initialQuery.isEmpty() ? View.GONE : View.VISIBLE);

        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSubmit();
                return true;
            }
            return false;
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearchQuery.setText("");
            if (callback != null) {
                callback.onClear();
            }
        });

        // Request focus and show keyboard
        binding.etSearchQuery.requestFocus();
        binding.etSearchQuery.postDelayed(() -> {
            if (getContext() != null) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(binding.etSearchQuery, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 100);
    }

    private void performSubmit() {
        String query = binding.etSearchQuery.getText().toString().trim();
        if (callback != null) {
            callback.onSearch(query);
        }
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.TOP);
            window.setDimAmount(0.35f);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.TOP);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
