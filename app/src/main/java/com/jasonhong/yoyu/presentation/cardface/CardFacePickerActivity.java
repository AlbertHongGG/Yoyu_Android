package com.jasonhong.yoyu.presentation.cardface;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jasonhong.yoyu.core.base.BaseActivity;
import com.jasonhong.yoyu.core.constants.AppConstants;
import com.jasonhong.yoyu.data.repository.CardRepositoryImpl;
import com.jasonhong.yoyu.databinding.ActivityCardFacePickerBinding;
import com.jasonhong.yoyu.domain.model.CardEntity;
import com.jasonhong.yoyu.domain.repository.CardRepository;

import java.util.ArrayList;
import java.util.List;

public class CardFacePickerActivity extends BaseActivity<ActivityCardFacePickerBinding> implements CardFaceAdapter.OnCardFaceClickListener {

    private static final int PAGE_SIZE = 15;

    private CardEntity card;
    private CardRepository cardRepository;
    private CardFaceAdapter adapter;
    private GridLayoutManager layoutManager;

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasNext = true;

    @Override
    protected ActivityCardFacePickerBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCardFacePickerBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        card = (CardEntity) getIntent().getSerializableExtra("card");
        if (card == null) {
            finish();
            return;
        }

        cardRepository = new CardRepositoryImpl(this);

        binding.btnBack.setOnClickListener(v -> finish());

        layoutManager = new GridLayoutManager(this, 3);
        binding.rvCardFaces.setLayoutManager(layoutManager);

        adapter = new CardFaceAdapter(card.getCardFaceUrl(), this);
        binding.rvCardFaces.setAdapter(adapter);

        binding.rvCardFaces.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && !isLoading && hasNext) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 6) {
                        loadMore();
                    }
                }
            }
        });

        loadMore();
    }

    private void loadMore() {
        if (isLoading || !hasNext) return;

        isLoading = true;

        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, CardFaceConstants.ALLOWED_IMAGE_IDS.length);

        List<String> batchUrls = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            int id = CardFaceConstants.ALLOWED_IMAGE_IDS[i];
            batchUrls.add(AppConstants.CARD_FACE_CDN_BASE + id + ".webp");
        }

        currentPage++;
        hasNext = endIndex < CardFaceConstants.ALLOWED_IMAGE_IDS.length;

        // Post to adapter
        binding.rvCardFaces.post(() -> {
            adapter.addUrls(batchUrls, hasNext);
            isLoading = false;
        });
    }

    @Override
    public void onCardFaceClick(String url) {
        CardEntity updated = card.copyWithCardFaceUrl(url);
        cardRepository.updateCard(updated).thenAccept(v -> {
            runOnUiThread(this::finish);
        });
    }
}
