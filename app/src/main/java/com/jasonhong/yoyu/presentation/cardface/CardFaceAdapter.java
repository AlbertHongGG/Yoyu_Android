package com.jasonhong.yoyu.presentation.cardface;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.core.widgets.skeleton.SkeletonPulseDrawable;
import com.jasonhong.yoyu.databinding.ItemCardFaceBinding;
import com.jasonhong.yoyu.databinding.ItemCardFaceSkeletonBinding;
import com.jasonhong.yoyu.domain.model.CardFaceItem;

import java.util.ArrayList;
import java.util.List;

public class CardFaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CARD = 0;
    private static final int TYPE_SKELETON = 1;

    public interface OnCardFaceClickListener {
        void onCardFaceClick(CardFaceItem item);
        void onFavoriteToggle(CardFaceItem item, int position);
        void onCopyId(CardFaceItem item);
    }

    private final List<CardFaceItem> items = new ArrayList<>();
    private final String selectedUrl;
    private final OnCardFaceClickListener listener;
    private boolean hasNext = true;

    public CardFaceAdapter(String selectedUrl, OnCardFaceClickListener listener) {
        this.selectedUrl = selectedUrl != null ? selectedUrl : "";
        this.listener = listener;
    }

    public void setItems(List<CardFaceItem> newItems, boolean hasNext) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        this.hasNext = hasNext;
        notifyDataSetChanged();
    }

    public void addItems(List<CardFaceItem> newItems, boolean hasNext) {
        if (newItems != null && !newItems.isEmpty()) {
            this.items.addAll(newItems);
        }
        this.hasNext = hasNext;
        notifyDataSetChanged();
    }

    public void notifyFavoriteChanged(int position, boolean isFavorite) {
        if (position >= 0 && position < items.size()) {
            items.get(position).setFavorite(isFavorite);
            notifyItemChanged(position, "FAVORITE_PAYLOAD");
        }
    }

    public List<CardFaceItem> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < items.size()) {
            return TYPE_CARD;
        }
        return TYPE_SKELETON;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_CARD) {
            ItemCardFaceBinding binding = ItemCardFaceBinding.inflate(inflater, parent, false);
            return new CardFaceViewHolder(binding);
        } else {
            ItemCardFaceSkeletonBinding binding = ItemCardFaceSkeletonBinding.inflate(inflater, parent, false);
            return new SkeletonViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CardFaceViewHolder) {
            ((CardFaceViewHolder) holder).bind(items.get(position));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && holder instanceof CardFaceViewHolder) {
            ((CardFaceViewHolder) holder).updateFavoriteUI(items.get(position));
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        if (items.isEmpty()) {
            return hasNext ? 15 : 0; // Initial 15 skeleton cards (5 rows of 3)
        }
        return items.size() + (hasNext ? 6 : 0); // Extra 6 skeleton cards at end
    }

    class CardFaceViewHolder extends RecyclerView.ViewHolder {
        private final ItemCardFaceBinding binding;

        CardFaceViewHolder(ItemCardFaceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CardFaceItem item) {
            Context context = itemView.getContext();
            boolean isSelected = item.getUrl().equals(selectedUrl);
            binding.layoutSelectedOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            binding.tvCardFaceId.setText(item.getFormattedId());
            updateFavoriteUI(item);

            SkeletonPulseDrawable placeholder = new SkeletonPulseDrawable(context, 16f);

            Glide.with(context)
                    .load(item.getUrl())
                    .transform(new Rotate(90), new CenterCrop())
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .placeholder(placeholder)
                    .error(new ColorDrawable(Color.parseColor("#33888888")))
                    .into(binding.ivFace);

            View.OnClickListener cardClickListener = v -> {
                if (listener != null) listener.onCardFaceClick(item);
            };
            binding.cardFaceContainer.setOnClickListener(cardClickListener);
            binding.ivFace.setOnClickListener(cardClickListener);

            binding.btnFavorite.setOnClickListener(v -> {
                binding.btnFavorite.animate()
                        .scaleX(1.3f)
                        .scaleY(1.3f)
                        .setDuration(120)
                        .withEndAction(() -> binding.btnFavorite.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start())
                        .start();
                if (listener != null) {
                    listener.onFavoriteToggle(item, getBindingAdapterPosition());
                }
            });

            binding.tvCardFaceId.setOnClickListener(v -> {
                if (listener != null) listener.onCopyId(item);
            });
        }

        void updateFavoriteUI(CardFaceItem item) {
            Context context = itemView.getContext();
            if (item.isFavorite()) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                binding.btnFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.expense_red)));
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                binding.btnFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_hint_light)));
            }
        }
    }

    static class SkeletonViewHolder extends RecyclerView.ViewHolder {
        SkeletonViewHolder(ItemCardFaceSkeletonBinding binding) {
            super(binding.getRoot());
            SkeletonPulseDrawable pulse = new SkeletonPulseDrawable(itemView.getContext(), 16f);
            binding.viewSkeleton.setBackground(pulse);
        }
    }
}
