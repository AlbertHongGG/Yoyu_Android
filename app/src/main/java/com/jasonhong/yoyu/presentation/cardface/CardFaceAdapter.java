package com.jasonhong.yoyu.presentation.cardface;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.databinding.ItemCardFaceBinding;
import com.jasonhong.yoyu.domain.model.CardFaceItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for card face selection grid:
 * - Implements ListPreloader.PreloadModelProvider for zero-latency lookahead prefetching.
 * - Optimized with DiskCacheStrategy.ALL and lightweight placeholders.
 * - Handles favorite toggle with payload updates for flicker-free interaction.
 */
public class CardFaceAdapter extends RecyclerView.Adapter<CardFaceAdapter.CardFaceViewHolder>
        implements ListPreloader.PreloadModelProvider<CardFaceItem> {

    public interface OnCardFaceClickListener {
        void onCardFaceClick(CardFaceItem item);
        void onFavoriteToggle(CardFaceItem item, int position);
        void onCopyId(CardFaceItem item);
    }

    private final Context context;
    private final List<CardFaceItem> items = new ArrayList<>();
    private final String selectedUrl;
    private final OnCardFaceClickListener listener;
    private final ColorDrawable placeholderDrawable = new ColorDrawable(Color.parseColor("#14000000"));
    private final ColorDrawable errorDrawable = new ColorDrawable(Color.parseColor("#22888888"));

    private final ViewPreloadSizeProvider<CardFaceItem> sizeProvider;

    public CardFaceAdapter(Context context, String selectedUrl, OnCardFaceClickListener listener,
                           ViewPreloadSizeProvider<CardFaceItem> sizeProvider) {
        this.context = context;
        this.selectedUrl = selectedUrl != null ? selectedUrl : "";
        this.listener = listener;
        this.sizeProvider = sizeProvider;
    }

    public void setItems(List<CardFaceItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
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

    @NonNull
    @Override
    public List<CardFaceItem> getPreloadItems(int position) {
        if (position >= 0 && position < items.size()) {
            return Collections.singletonList(items.get(position));
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull CardFaceItem item) {
        return Glide.with(context)
                .load(item.getUrl())
                .transform(new Rotate(90), new CenterCrop())
                .diskCacheStrategy(DiskCacheStrategy.ALL);
    }

    @NonNull
    @Override
    public CardFaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardFaceBinding binding = ItemCardFaceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        if (sizeProvider != null) {
            sizeProvider.setView(binding.ivFace);
        }
        return new CardFaceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardFaceViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull CardFaceViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            holder.updateFavoriteUI(items.get(position));
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CardFaceViewHolder extends RecyclerView.ViewHolder {
        private final ItemCardFaceBinding binding;

        CardFaceViewHolder(ItemCardFaceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CardFaceItem item) {
            Context ctx = itemView.getContext();
            boolean isSelected = item.getUrl().equals(selectedUrl);
            binding.layoutSelectedOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            binding.tvCardFaceId.setText(item.getFormattedId());
            updateFavoriteUI(item);

            Glide.with(ctx)
                    .load(item.getUrl())
                    .transform(new Rotate(90), new CenterCrop())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade(120))
                    .placeholder(placeholderDrawable)
                    .error(errorDrawable)
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
            Context ctx = itemView.getContext();
            if (item.isFavorite()) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                binding.btnFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.expense_red)));
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                binding.btnFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.text_hint_light)));
            }
        }
    }
}
