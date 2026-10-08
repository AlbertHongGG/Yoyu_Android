package com.jasonhong.yoyu.presentation.home;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.core.base.BaseActivity;
import com.jasonhong.yoyu.databinding.ActivityMainBinding;
import com.jasonhong.yoyu.domain.model.CardEntity;
import com.jasonhong.yoyu.presentation.cardface.CardFacePickerActivity;
import com.jasonhong.yoyu.presentation.detail.CardDetailActivity;

import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding> implements CardAdapter.OnCardClickListener {

    private HomeViewModel viewModel;
    private CardAdapter adapter;

    @Override
    protected ActivityMainBinding inflateBinding(LayoutInflater inflater) {
        return ActivityMainBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        adapter = new CardAdapter(this);
        binding.rvCards.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCards.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        binding.fabActionTarget.setOnClickListener(v -> {
            Boolean isDragging = viewModel.getIsDraggingLiveData().getValue();
            if (Boolean.TRUE.equals(isDragging)) return;
            AddCardBottomSheetDialog dialog = AddCardBottomSheetDialog.newInstance();
            dialog.show(getSupportFragmentManager(), "AddCardBottomSheetDialog");
        });

        setupSmartDragTarget();
    }

    @Override
    protected void initData() {
        viewModel.getCardsLiveData().observe(this, resource -> {
            if (resource == null) return;
            binding.swipeRefresh.setRefreshing(resource.isLoading());

            List<CardEntity> cards = resource.data;
            if (cards == null || cards.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.rvCards.setVisibility(View.GONE);
                adapter.submitList(null);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvCards.setVisibility(View.VISIBLE);
                adapter.submitList(cards);
            }

            if (resource.isError() && resource.message != null) {
                showError(resource.message);
            }
        });
    }

    private void setupSmartDragTarget() {
        int defaultColor = ContextCompat.getColor(this, R.color.text_secondary_light);

        binding.fabActionTarget.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    viewModel.setDragging(true);
                    binding.ivFabIcon.setImageResource(R.drawable.ic_delete);
                    ImageViewCompat.setImageTintList(binding.ivFabIcon, ColorStateList.valueOf(defaultColor));
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    animateFabSize(dpToPx(56), dpToPx(64));
                    binding.fabActionTarget.setBackgroundResource(R.drawable.bg_fab_hover);
                    binding.ivFabIcon.setImageResource(R.drawable.ic_delete_forever);
                    ImageViewCompat.setImageTintList(binding.ivFabIcon, ColorStateList.valueOf(0xFFFFFFFF));
                    v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    animateFabSize(dpToPx(64), dpToPx(56));
                    binding.fabActionTarget.setBackgroundResource(R.drawable.bg_fab_default);
                    binding.ivFabIcon.setImageResource(R.drawable.ic_delete);
                    ImageViewCompat.setImageTintList(binding.ivFabIcon, ColorStateList.valueOf(defaultColor));
                    return true;

                case DragEvent.ACTION_DROP:
                    String cardNoToDelete = null;
                    Object localState = event.getLocalState();
                    if (localState instanceof CardEntity) {
                        cardNoToDelete = ((CardEntity) localState).getCardNo();
                    } else if (event.getClipData() != null && event.getClipData().getItemCount() > 0) {
                        CharSequence text = event.getClipData().getItemAt(0).getText();
                        if (text != null) cardNoToDelete = text.toString();
                    }
                    if (cardNoToDelete != null) {
                        v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                        viewModel.deleteCard(cardNoToDelete);
                        showSuccess("已刪除卡片");
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    viewModel.setDragging(false);
                    restoreCardsAlpha();
                    animateFabSize(binding.fabActionTarget.getWidth(), dpToPx(56));
                    binding.fabActionTarget.setBackgroundResource(R.drawable.bg_fab_default);
                    binding.ivFabIcon.setImageResource(R.drawable.ic_add);
                    ImageViewCompat.setImageTintList(binding.ivFabIcon, ColorStateList.valueOf(defaultColor));
                    return true;

                default:
                    return false;
            }
        });
    }

    private void restoreCardsAlpha() {
        for (int i = 0; i < binding.rvCards.getChildCount(); i++) {
            View child = binding.rvCards.getChildAt(i);
            if (child != null) {
                child.animate().alpha(1.0f).setDuration(150).start();
            }
        }
    }

    private void animateFabSize(int fromSize, int toSize) {
        if (fromSize == toSize) return;
        ValueAnimator anim = ValueAnimator.ofInt(fromSize, toSize);
        anim.setDuration(150);
        anim.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = binding.fabActionTarget.getLayoutParams();
            lp.width = val;
            lp.height = val;
            binding.fabActionTarget.setLayoutParams(lp);
        });
        anim.start();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onCardClick(CardEntity card) {
        Intent intent = new Intent(this, CardDetailActivity.class);
        intent.putExtra("card", card);
        startActivity(intent);
    }

    @Override
    public void onChangeCoverClick(CardEntity card) {
        Intent intent = new Intent(this, CardFacePickerActivity.class);
        intent.putExtra("card", card);
        startActivity(intent);
    }

    @Override
    public void onCardDragStart(CardEntity card) {
        // Drag started
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadCards();
    }
}
