package com.jasonhong.yoyu.presentation.home;

import android.content.ClipData;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.facebook.shimmer.ShimmerDrawable;
import com.jasonhong.yoyu.core.widgets.ShimmerHelper;
import com.jasonhong.yoyu.databinding.ItemCardBinding;
import com.jasonhong.yoyu.domain.model.CardEntity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    public interface OnCardClickListener {
        void onCardClick(CardEntity card);
        void onChangeCoverClick(CardEntity card);
        void onCardDragStart(CardEntity card);
    }

    private final List<CardEntity> cards = new ArrayList<>();
    private final OnCardClickListener listener;
    private final NumberFormat currencyFormat;

    public CardAdapter(OnCardClickListener listener) {
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.TAIWAN);
        this.currencyFormat.setMaximumFractionDigits(0);
        this.currencyFormat.setMinimumFractionDigits(0);
    }

    public void submitList(List<CardEntity> newCards) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return cards.size();
            }

            @Override
            public int getNewListSize() {
                return newCards != null ? newCards.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return cards.get(oldItemPosition).getCardNo().equals(newCards.get(newItemPosition).getCardNo());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return cards.get(oldItemPosition).equals(newCards.get(newItemPosition));
            }
        });

        cards.clear();
        if (newCards != null) {
            cards.addAll(newCards);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(cards.get(position));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        private final ItemCardBinding binding;

        CardViewHolder(ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CardEntity card) {
            binding.tvCardName.setText(card.getCardName());
            binding.tvCardNo.setText(formatCardNo(card.getCardNo()));
            binding.tvCardBalance.setText(currencyFormat.format(card.getLastTranSum()));

            binding.tagRegistered.setVisibility(card.isRegister() ? View.VISIBLE : View.GONE);

            // Load Card Face with metallic shimmer
            ShimmerDrawable shimmer = ShimmerHelper.createMetallicShimmer(itemView.getContext());
            Glide.with(itemView.getContext())
                    .load(card.getCardFaceUrl())
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .centerCrop()
                    .placeholder(shimmer)
                    .error(new ColorDrawable(android.graphics.Color.parseColor("#33888888")))
                    .into(binding.ivCardFace);

            binding.cardContainer.setOnClickListener(v -> {
                if (listener != null) listener.onCardClick(card);
            });

            binding.btnChangeCover.setOnClickListener(v -> {
                if (listener != null) listener.onChangeCoverClick(card);
            });

            // Long press to drag and drop
            binding.cardContainer.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onCardDragStart(card);
                }
                ClipData data = ClipData.newPlainText("cardNo", card.getCardNo());
                View.DragShadowBuilder shadowBuilder = new CardDragShadowBuilder(itemView);
                itemView.setAlpha(0.2f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    v.startDragAndDrop(data, shadowBuilder, card, 0);
                } else {
                    v.startDrag(data, shadowBuilder, card, 0);
                }
                return true;
            });
        }

        private String formatCardNo(String rawNo) {
            if (rawNo == null) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rawNo.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    sb.append(" ");
                }
                sb.append(rawNo.charAt(i));
            }
            return sb.toString();
        }
    }

    public static class CardDragShadowBuilder extends View.DragShadowBuilder {
        public CardDragShadowBuilder(View view) {
            super(view);
        }

        @Override
        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            View view = getView();
            if (view == null) return;
            int width = (int) (view.getWidth() * 1.05f);
            int height = (int) (view.getHeight() * 1.05f);
            outShadowSize.set(width, height);
            outShadowTouchPoint.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            View view = getView();
            if (view == null) return;
            canvas.save();
            canvas.scale(1.05f, 1.05f);
            view.setAlpha(0.85f);
            view.draw(canvas);
            view.setAlpha(0.2f);
            canvas.restore();
        }
    }
}
