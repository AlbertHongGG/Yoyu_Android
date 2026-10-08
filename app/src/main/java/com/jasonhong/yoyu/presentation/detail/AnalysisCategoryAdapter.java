package com.jasonhong.yoyu.presentation.detail;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.databinding.ItemAnalysisCategoryBinding;
import com.jasonhong.yoyu.domain.model.AnalysisDataType;
import com.jasonhong.yoyu.domain.model.CategoryUIModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalysisCategoryAdapter extends RecyclerView.Adapter<AnalysisCategoryAdapter.CategoryViewHolder> {

    private final List<CategoryUIModel> categories = new ArrayList<>();
    private int totalAmount = 0;
    private AnalysisDataType currentType = AnalysisDataType.EXPENSE;
    private final NumberFormat currencyFormat;

    public AnalysisCategoryAdapter() {
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.TAIWAN);
        this.currencyFormat.setMaximumFractionDigits(0);
        this.currencyFormat.setMinimumFractionDigits(0);
    }

    public void submitData(List<CategoryUIModel> list, int total, AnalysisDataType type) {
        this.categories.clear();
        if (list != null) {
            this.categories.addAll(list);
        }
        this.totalAmount = total;
        this.currentType = type;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAnalysisCategoryBinding binding = ItemAnalysisCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemAnalysisCategoryBinding binding;

        CategoryViewHolder(ItemAnalysisCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CategoryUIModel model) {
            binding.tvCategoryName.setText(model.getScopeName());
            binding.tvCategoryCount.setText(model.getCount() + " 次");
            binding.tvCategoryAmount.setText(currencyFormat.format(model.getAmount()));
            binding.ivCategoryIcon.setImageResource(model.getIconResId());

            int colorRes;
            switch (currentType) {
                case EXPENSE:
                    colorRes = R.color.expense_red;
                    break;
                case TOP_UP:
                    colorRes = R.color.top_up_blue;
                    break;
                case AUTO_TOP_UP:
                default:
                    colorRes = R.color.auto_top_up_orange;
                    break;
            }

            int color = ContextCompat.getColor(itemView.getContext(), colorRes);
            ImageViewCompat.setImageTintList(binding.ivCategoryIcon, ColorStateList.valueOf(color));
            binding.layoutIconContainer.setBackgroundTintList(ColorStateList.valueOf(androidx.core.graphics.ColorUtils.setAlphaComponent(color, 38)));

            int progress = totalAmount > 0 ? (int) Math.round((model.getAmount() * 100.0) / totalAmount) : 0;
            binding.pbCategoryProgress.setProgress(progress);
            binding.pbCategoryProgress.setProgressTintList(ColorStateList.valueOf(color));
        }
    }
}
