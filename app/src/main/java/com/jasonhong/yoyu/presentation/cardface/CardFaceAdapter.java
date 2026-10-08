package com.jasonhong.yoyu.presentation.cardface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.databinding.ItemCardFaceBinding;

import java.util.ArrayList;
import java.util.List;

public class CardFaceAdapter extends RecyclerView.Adapter<CardFaceAdapter.CardFaceViewHolder> {

    public interface OnCardFaceClickListener {
        void onCardFaceClick(String url);
    }

    private final List<String> urls = new ArrayList<>();
    private final String selectedUrl;
    private final OnCardFaceClickListener listener;

    public CardFaceAdapter(String selectedUrl, OnCardFaceClickListener listener) {
        this.selectedUrl = selectedUrl;
        this.listener = listener;
    }

    public void addUrls(List<String> newUrls) {
        if (newUrls == null || newUrls.isEmpty()) return;
        int startPos = urls.size();
        urls.addAll(newUrls);
        notifyItemRangeInserted(startPos, newUrls.size());
    }

    @NonNull
    @Override
    public CardFaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardFaceBinding binding = ItemCardFaceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CardFaceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardFaceViewHolder holder, int position) {
        holder.bind(urls.get(position));
    }

    @Override
    public int getItemCount() {
        return urls.size();
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

            // Rotate 90 degrees to show landscape card images as portrait!
            int radius = (int) (16 * itemView.getResources().getDisplayMetrics().density + 0.5f);
            Glide.with(itemView.getContext())
                    .load(url)
                    .transform(new Rotate(90), new CenterCrop(), new RoundedCorners(radius))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.ivFace);

            binding.cardFaceContainer.setOnClickListener(v -> {
                if (listener != null) listener.onCardFaceClick(url);
            });
        }
    }
}
