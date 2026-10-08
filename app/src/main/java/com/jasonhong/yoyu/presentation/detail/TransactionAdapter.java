package com.jasonhong.yoyu.presentation.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.core.widgets.skeleton.SkeletonPulseDrawable;
import com.jasonhong.yoyu.databinding.ItemTransactionRetailBinding;
import com.jasonhong.yoyu.databinding.ItemTransactionSkeletonBinding;
import com.jasonhong.yoyu.databinding.ItemTransactionTransitBinding;
import com.jasonhong.yoyu.domain.model.RetailTransaction;
import com.jasonhong.yoyu.domain.model.TransitTransaction;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TRANSIT = 1;
    private static final int TYPE_RETAIL = 2;
    private static final int TYPE_SKELETON = 3;
    private static final int SKELETON_COUNT = 4;

    private final List<YoyuTransaction> transactions = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final NumberFormat currencyFormat;
    private boolean isLoading = true;

    public TransactionAdapter() {
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.TAIWAN);
        this.currencyFormat.setMaximumFractionDigits(0);
        this.currencyFormat.setMinimumFractionDigits(0);
    }

    public void setLoading(boolean loading) {
        if (this.isLoading != loading) {
            this.isLoading = loading;
            notifyDataSetChanged();
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void submitList(List<YoyuTransaction> list) {
        transactions.clear();
        if (list != null) {
            transactions.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoading) {
            return TYPE_SKELETON;
        }
        if (transactions.get(position) instanceof TransitTransaction) {
            return TYPE_TRANSIT;
        } else {
            return TYPE_RETAIL;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SKELETON) {
            ItemTransactionSkeletonBinding binding = ItemTransactionSkeletonBinding.inflate(inflater, parent, false);
            return new SkeletonViewHolder(binding);
        } else if (viewType == TYPE_TRANSIT) {
            ItemTransactionTransitBinding binding = ItemTransactionTransitBinding.inflate(inflater, parent, false);
            return new TransitViewHolder(binding);
        } else {
            ItemTransactionRetailBinding binding = ItemTransactionRetailBinding.inflate(inflater, parent, false);
            return new RetailViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TransitViewHolder) {
            ((TransitViewHolder) holder).bind((TransitTransaction) transactions.get(position));
        } else if (holder instanceof RetailViewHolder) {
            ((RetailViewHolder) holder).bind((RetailTransaction) transactions.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (isLoading) {
            return SKELETON_COUNT;
        }
        return transactions.size();
    }

    class TransitViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionTransitBinding binding;

        TransitViewHolder(ItemTransactionTransitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TransitTransaction tx) {
            binding.tvDate.setText(dateFormat.format(tx.getInTime()));
            binding.tvPartnerName.setText(tx.getPartnerName());
            binding.tvBalance.setText("餘 " + currencyFormat.format(tx.getBalance()));

            int amt = tx.getAmount();
            String amtStr = (amt > 0 ? "+" : "") + amt;
            binding.tvAmount.setText(amtStr);

            boolean isDark = (itemView.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            int colorRes = amt < 0 ? R.color.tx_expense_red : (amt > 0 ? R.color.tx_income_green : (isDark ? R.color.text_primary_dark : R.color.tx_zero_black));
            binding.tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));

            binding.tvInLocation.setText(tx.getInLocation());
            binding.tvOutLocation.setText(tx.getOutLocation());

            binding.tvInTime.setText(timeFormat.format(tx.getInTime()));
            binding.tvOutTime.setText(timeFormat.format(tx.getOutTime()));
        }
    }

    class RetailViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionRetailBinding binding;

        RetailViewHolder(ItemTransactionRetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RetailTransaction tx) {
            binding.tvDate.setText(dateFormat.format(tx.getTime()));
            binding.tvPartnerName.setText(tx.getPartnerName());
            binding.tvBalance.setText("餘 " + currencyFormat.format(tx.getBalance()));

            int amt = tx.getAmount();
            String amtStr = (amt > 0 ? "+" : "") + amt;
            binding.tvAmount.setText(amtStr);

            boolean isDark = (itemView.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            int colorRes = amt < 0 ? R.color.tx_expense_red : (amt > 0 ? R.color.tx_income_green : (isDark ? R.color.text_primary_dark : R.color.tx_zero_black));
            binding.tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));

            String desc = (tx.getLocation() + " " + tx.getDescription()).trim();
            binding.tvDescription.setText(desc);
            binding.tvTime.setText(timeFormat.format(tx.getTime()));
        }
    }

    static class SkeletonViewHolder extends RecyclerView.ViewHolder {
        SkeletonViewHolder(ItemTransactionSkeletonBinding binding) {
            super(binding.getRoot());
            Context context = itemView.getContext();
            binding.viewSkeletonDate.setBackground(new SkeletonPulseDrawable(context, 4f));
            binding.viewSkeletonPartner.setBackground(new SkeletonPulseDrawable(context, 4f));
            binding.viewSkeletonBalance.setBackground(new SkeletonPulseDrawable(context, 4f));
            binding.viewSkeletonAmount.setBackground(new SkeletonPulseDrawable(context, 6f));
            binding.viewSkeletonTopDot.setBackground(new SkeletonPulseDrawable(context, 4f));
            binding.viewSkeletonBottomDot.setBackground(new SkeletonPulseDrawable(context, 4f));
            binding.viewSkeletonInLocation.setBackground(new SkeletonPulseDrawable(context, 4f));
            binding.viewSkeletonOutLocation.setBackground(new SkeletonPulseDrawable(context, 4f));
            binding.viewSkeletonInTime.setBackground(new SkeletonPulseDrawable(context, 4f));
            binding.viewSkeletonOutTime.setBackground(new SkeletonPulseDrawable(context, 4f));
        }
    }
}
