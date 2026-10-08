package com.jasonhong.yoyu.presentation.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.core.base.BaseActivity;
import com.jasonhong.yoyu.databinding.ActivityMainBinding;
import com.jasonhong.yoyu.domain.model.CardEntity;
import com.jasonhong.yoyu.presentation.cardface.CardFacePickerActivity;
import com.jasonhong.yoyu.presentation.detail.CardDetailActivity;
import com.jasonhong.yoyu.presentation.home.drag.CardDragCoordinator;
import com.jasonhong.yoyu.presentation.home.drag.TrashActionTarget;

import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding> implements CardAdapter.OnCardClickListener {

    private HomeViewModel viewModel;
    private CardAdapter adapter;
    private CardDragCoordinator dragCoordinator;
    private TrashActionTarget trashActionTarget;

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

        // Setup Drag Target (Trash Action)
        int defaultColor = ContextCompat.getColor(this, R.color.text_secondary_light);
        trashActionTarget = new TrashActionTarget(
                binding.fabActionTarget,
                binding.ivFabIcon,
                defaultColor,
                card -> {
                    viewModel.deleteCard(card.getCardNo());
                    showSuccess("已刪除卡片");
                }
        );

        // Setup Drag Coordinator
        dragCoordinator = new CardDragCoordinator(
                binding.dragOverlayView,
                trashActionTarget,
                binding.rvCards
        );

        binding.fabActionTarget.setOnClickListener(v -> {
            if (dragCoordinator != null && dragCoordinator.isDragging()) return;
            AddCardBottomSheetDialog dialog = AddCardBottomSheetDialog.newInstance();
            dialog.show(getSupportFragmentManager(), "AddCardBottomSheetDialog");
        });
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (dragCoordinator != null && dragCoordinator.isDragging()) {
            dragCoordinator.onTouchEvent(ev);
            return true;
        }
        return super.dispatchTouchEvent(ev);
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
    public void onCardDragStart(CardEntity card, View itemView, float touchX, float touchY) {
        if (dragCoordinator != null) {
            dragCoordinator.startDrag(itemView, card, touchX, touchY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadCards();
    }
}
