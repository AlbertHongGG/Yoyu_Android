package com.jasonhong.yoyu.presentation.cardface;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.core.base.BaseActivity;
import com.jasonhong.yoyu.core.constants.AppConstants;
import com.jasonhong.yoyu.data.repository.CardFaceFavoriteRepositoryImpl;
import com.jasonhong.yoyu.data.repository.CardRepositoryImpl;
import com.jasonhong.yoyu.databinding.ActivityCardFacePickerBinding;
import com.jasonhong.yoyu.domain.model.CardEntity;
import com.jasonhong.yoyu.domain.model.CardFaceItem;
import com.jasonhong.yoyu.domain.repository.CardFaceFavoriteRepository;
import com.jasonhong.yoyu.domain.repository.CardRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * CardFacePickerActivity:
 * - Natural virtualized data supply for full card catalog without artificial micro-batching.
 * - Integrates Glide RecyclerViewPreloader for zero-latency lookahead image preloading.
 * - Configured with RecyclerView view cache and fixed size optimizations.
 * - Supports seamless favorite toggling and exact pixel scroll position restoration.
 */
public class CardFacePickerActivity extends BaseActivity<ActivityCardFacePickerBinding>
        implements CardFaceAdapter.OnCardFaceClickListener {

    private CardEntity card;
    private CardRepository cardRepository;
    private CardFaceFavoriteRepository favoriteRepository;
    private CardFaceAdapter adapter;
    private GridLayoutManager layoutManager;
    private ViewPreloadSizeProvider<CardFaceItem> preloadSizeProvider;

    // Full catalog in "All" mode
    private final List<CardFaceItem> allItems = new ArrayList<>();

    // Preserved scroll position for "All" mode
    private int allScrollPos = 0;
    private int allScrollOffset = 0;

    // Mode state: false = All, true = Favorites
    private boolean isFavoriteMode = false;

    @Override
    protected ActivityCardFacePickerBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCardFacePickerBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        card = (CardEntity) getIntent().getSerializableExtra("card");
        if (card == null) {
            card = new CardEntity("77050067379", "我的卡片", AppConstants.DEFAULT_CARD_FACE_URL, 291.0, false);
        }

        cardRepository = new CardRepositoryImpl(this);
        favoriteRepository = new CardFaceFavoriteRepositoryImpl(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnToggleFavorites.setOnClickListener(v -> toggleFavoriteMode());

        layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setInitialPrefetchItemCount(9);
        binding.rvCardFaces.setLayoutManager(layoutManager);
        binding.rvCardFaces.setHasFixedSize(true);
        binding.rvCardFaces.setItemViewCacheSize(18);

        // Preload size provider observes actual view dimensions on layout
        preloadSizeProvider = new ViewPreloadSizeProvider<>();
        adapter = new CardFaceAdapter(this, card.getCardFaceUrl(), this, preloadSizeProvider);
        binding.rvCardFaces.setAdapter(adapter);

        // Preload ahead by 24 items in the scroll direction
        RecyclerViewPreloader<CardFaceItem> preloader = new RecyclerViewPreloader<>(
                Glide.with(this),
                adapter,
                preloadSizeProvider,
                24
        );
        binding.rvCardFaces.addOnScrollListener(preloader);

        loadAllCards();
    }

    private void loadAllCards() {
        allItems.clear();
        for (int id : CardFaceConstants.ALLOWED_IMAGE_IDS) {
            boolean isFav = favoriteRepository.isFavorite(id);
            allItems.add(new CardFaceItem(id, AppConstants.CARD_FACE_CDN_BASE + id + ".webp", isFav));
        }
        adapter.setItems(allItems);
    }

    private void toggleFavoriteMode() {
        triggerHaptic();
        isFavoriteMode = !isFavoriteMode;

        if (isFavoriteMode) {
            // Save current scroll position in "All" mode
            allScrollPos = layoutManager.findFirstVisibleItemPosition();
            View firstChild = layoutManager.getChildAt(0);
            allScrollOffset = (firstChild != null) ? firstChild.getTop() : 0;

            // Update AppBar UI to Favorites state
            binding.ivToggleFavorites.setImageResource(R.drawable.ic_favorite_filled);
            binding.ivToggleFavorites.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.expense_red)));
            binding.tvTitle.setText("我的最愛封面");

            loadFavoritesList();
            layoutManager.scrollToPositionWithOffset(0, 0);
        } else {
            // Restore AppBar UI to All state
            binding.ivToggleFavorites.setImageResource(R.drawable.ic_favorite_border);
            binding.ivToggleFavorites.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_primary_light)));
            binding.tvTitle.setText(R.string.choose_card_face);

            binding.layoutEmptyFavorites.setVisibility(View.GONE);
            binding.rvCardFaces.setVisibility(View.VISIBLE);

            // Re-sync favorite status in allItems in case it changed
            for (CardFaceItem item : allItems) {
                item.setFavorite(favoriteRepository.isFavorite(item.getId()));
            }
            adapter.setItems(allItems);

            // Seamlessly restore scroll position in "All" mode
            layoutManager.scrollToPositionWithOffset(allScrollPos, allScrollOffset);
        }
    }

    private void loadFavoritesList() {
        Set<Integer> favIds = favoriteRepository.getFavoriteIds();
        if (favIds.isEmpty()) {
            binding.layoutEmptyFavorites.setVisibility(View.VISIBLE);
            binding.rvCardFaces.setVisibility(View.GONE);
            adapter.setItems(Collections.emptyList());
        } else {
            binding.layoutEmptyFavorites.setVisibility(View.GONE);
            binding.rvCardFaces.setVisibility(View.VISIBLE);
            List<Integer> sortedIds = new ArrayList<>(favIds);
            Collections.sort(sortedIds);
            List<CardFaceItem> favItems = new ArrayList<>(sortedIds.size());
            for (Integer id : sortedIds) {
                favItems.add(new CardFaceItem(id, AppConstants.CARD_FACE_CDN_BASE + id + ".webp", true));
            }
            adapter.setItems(favItems);
        }
    }

    @Override
    public void onCardFaceClick(CardFaceItem item) {
        triggerHaptic();
        CardEntity updated = card.copyWithCardFaceUrl(item.getUrl());
        cardRepository.updateCard(updated).thenAccept(v -> {
            runOnUiThread(this::finish);
        });
    }

    @Override
    public void onCardFaceLongClick(View sourceView, CardFaceItem item, int position) {
        triggerHaptic(40);
        com.jasonhong.yoyu.presentation.showcase.CardShowcaseLauncher.show(
                this,
                sourceView,
                item,
                (showcaseItem, isFavorite) -> {
                    favoriteRepository.toggleFavorite(showcaseItem.getId());
                    item.setFavorite(isFavorite);
                    if (isFavoriteMode) {
                        loadFavoritesList();
                    } else {
                        adapter.notifyFavoriteChanged(position, isFavorite);
                    }
                }
        );
    }

    @Override
    public void onFavoriteToggle(CardFaceItem item, int position) {
        triggerHaptic();
        boolean newFav = favoriteRepository.toggleFavorite(item.getId());
        item.setFavorite(newFav);

        if (isFavoriteMode) {
            loadFavoritesList();
        } else {
            adapter.notifyFavoriteChanged(position, newFav);
        }
    }

    @Override
    public void onCopyId(CardFaceItem item) {
        triggerHaptic();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("CardFace ID", item.getFormattedId());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "已複製卡面 ID " + item.getFormattedId(), Toast.LENGTH_SHORT).show();
        }
    }

    private void triggerHaptic() {
        triggerHaptic(20);
    }

    private void triggerHaptic(int ms) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        }
    }
}
