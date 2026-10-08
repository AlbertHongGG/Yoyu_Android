package com.jasonhong.yoyu.presentation.cardface;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.jasonhong.yoyu.core.widgets.skeleton.SkeletonPulseDrawable;
import com.jasonhong.yoyu.databinding.ItemCardFaceBinding;
import com.jasonhong.yoyu.databinding.ItemCardFaceSkeletonBinding;

import java.util.ArrayList;
import java.util.List;

public class CardFaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CARD = 0;
    private static final int TYPE_SKELETON = 1;

    public interface OnCardFaceClickListener {
        void onCardFaceClick(String url);
    }

    private final List<String> urls = new ArrayList<>();
    private final String selectedUrl;
    private final OnCardFaceClickListener listener;
    private boolean hasNext = true;

    public CardFaceAdapter(String selectedUrl, OnCardFaceClickListener listener) {
        this.selectedUrl = selectedUrl;
        this.listener = listener;
    }

    public void setHasNext(boolean hasNext) {
        if (this.hasNext != hasNext) {
            this.hasNext = hasNext;
            notifyDataSetChanged();
        }
    }

    public void addUrls(List<String> newUrls, boolean hasNext) {
        if (newUrls != null && !newUrls.isEmpty()) {
            urls.addAll(newUrls);
        }
        this.hasNext = hasNext;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < urls.size()) {
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
            ((CardFaceViewHolder) holder).bind(urls.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (urls.isEmpty()) {
            return hasNext ? 15 : 0; // Initial 15 skeleton cards (5 rows of 3)
        }
        return urls.size() + (hasNext ? 6 : 0); // Extra 6 skeleton cards at end matching Flutter
    }

    class CardFaceViewHolder extends RecyclerView.ViewHolder {
        private final ItemCardFaceBinding binding;

        CardFaceViewHolder(ItemCardFaceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String url) {
            boolean isSelected = url.equals(selectedUrl);
            binding.layoutSelectedOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            SkeletonPulseDrawable placeholder = new SkeletonPulseDrawable(itemView.getContext(), 16f);

            Glide.with(itemView.getContext())
                    .load(url)
                    .transform(new Rotate(90), new CenterCrop())
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .placeholder(placeholder)
                    .error(new ColorDrawable(Color.parseColor("#33888888")))
                    .into(binding.ivFace);

            binding.cardFaceContainer.setOnClickListener(v -> {
                if (listener != null) listener.onCardFaceClick(url);
            });
        }
    }

    static class SkeletonViewHolder extends RecyclerView.ViewHolder {
        private final SkeletonPulseDrawable skeletonDrawable;

        SkeletonViewHolder(ItemCardFaceSkeletonBinding binding) {
            super(binding.getRoot());
            skeletonDrawable = new SkeletonPulseDrawable(itemView.getContext(), 16f);
            binding.viewSkeleton.setBackground(skeletonDrawable);
        }
    }
}
