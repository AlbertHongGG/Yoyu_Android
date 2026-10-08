package com.jasonhong.yoyu.presentation.detail;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.databinding.DialogPartnerFilterBinding;
import com.jasonhong.yoyu.databinding.ItemPartnerFilterBinding;
import com.jasonhong.yoyu.domain.service.ScopeIconMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PartnerFilterBottomSheetDialog extends BottomSheetDialogFragment {

    public interface OnPartnerSelectedListener {
        void onPartnerSelected(String partner);
    }

    private DialogPartnerFilterBinding binding;
    private List<String> partners = new ArrayList<>();
    private String selectedPartner = null;
    private OnPartnerSelectedListener listener;

    public static PartnerFilterBottomSheetDialog newInstance(List<String> partners, String selectedPartner, OnPartnerSelectedListener listener) {
        PartnerFilterBottomSheetDialog dialog = new PartnerFilterBottomSheetDialog();
        dialog.partners = partners != null ? partners : new ArrayList<>();
        dialog.selectedPartner = selectedPartner;
        dialog.listener = listener;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogPartnerFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<OptionItem> options = new ArrayList<>();
        // "全部" is always first
        options.add(new OptionItem(null, getString(R.string.filter_all), R.drawable.ic_all_inclusive));
        for (String p : partners) {
            options.add(new OptionItem(p, p, R.drawable.ic_directions_bus_filled_rounded));
        }

        PartnerAdapter adapter = new PartnerAdapter(options, selectedPartner, item -> {
            if (listener != null) {
                listener.onPartnerSelected(item.value);
            }
            dismiss();
        });

        binding.rvPartnerOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPartnerOptions.setAdapter(adapter);
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class OptionItem {
        final String value;
        final String title;
        final int iconRes;

        OptionItem(String value, String title, int iconRes) {
            this.value = value;
            this.title = title;
            this.iconRes = iconRes;
        }
    }

    private static class PartnerAdapter extends RecyclerView.Adapter<PartnerAdapter.ViewHolder> {
        private final List<OptionItem> items;
        private final String selectedValue;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(OptionItem item);
        }

        PartnerAdapter(List<OptionItem> items, String selectedValue, OnItemClickListener listener) {
            this.items = items;
            this.selectedValue = selectedValue;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPartnerFilterBinding binding = ItemPartnerFilterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OptionItem item = items.get(position);
            holder.binding.tvPartnerName.setText(item.title);
            holder.binding.ivPartnerIcon.setImageResource(item.iconRes);

            boolean isSelected = Objects.equals(selectedValue, item.value);
            holder.binding.ivPartnerSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemPartnerFilterBinding binding;

            ViewHolder(ItemPartnerFilterBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
