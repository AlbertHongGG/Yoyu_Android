package com.jasonhong.yoyu.data.repository;

import android.content.Context;

import com.jasonhong.yoyu.core.constants.AppConstants;
import com.jasonhong.yoyu.core.network.ApiException;
import com.jasonhong.yoyu.core.network.RetrofitClient;
import com.jasonhong.yoyu.data.api.IPassApiService;
import com.jasonhong.yoyu.data.local.LocalCardStorage;
import com.jasonhong.yoyu.data.model.remote.CheckMyCardsRequest;
import com.jasonhong.yoyu.data.model.remote.CheckMyCardsResponse;
import com.jasonhong.yoyu.domain.model.CardEntity;
import com.jasonhong.yoyu.domain.repository.CardRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class CardRepositoryImpl implements CardRepository {

    private final IPassApiService apiService;
    private final LocalCardStorage storage;
    private final ExecutorService executor;

    public CardRepositoryImpl(Context context) {
        this.apiService = RetrofitClient.getApiService();
        this.storage = new LocalCardStorage(context);
        this.executor = Executors.newFixedThreadPool(4);
    }

    public CardRepositoryImpl(IPassApiService apiService, LocalCardStorage storage, ExecutorService executor) {
        this.apiService = apiService;
        this.storage = storage;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<CardEntity> checkCard(String cardNo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response<CheckMyCardsResponse> response = apiService.checkMyCards(new CheckMyCardsRequest(cardNo)).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    throw new ApiException("伺服器回應錯誤 (狀態碼: " + response.code() + ")");
                }

                CheckMyCardsResponse body = response.body();
                if (!"0".equals(body.getRtnCode())) {
                    String msg = body.getRtnMsg() != null && !body.getRtnMsg().isEmpty() ? body.getRtnMsg() : "驗證失敗";
                    throw new ApiException(msg);
                }

                List<CheckMyCardsResponse.CardDataDto> cardList = body.getCardNos();
                if (cardList == null || cardList.isEmpty()) {
                    throw new ApiException("查無卡片資料");
                }

                CheckMyCardsResponse.CardDataDto dto = cardList.get(0);
                if (!"0".equals(dto.getErrCode())) {
                    String msg = dto.getErrMsg() != null && !dto.getErrMsg().isEmpty() ? dto.getErrMsg() : "驗證失敗";
                    throw new ApiException(msg);
                }

                return new CardEntity(
                        dto.getCardNo(),
                        "我的卡片",
                        AppConstants.DEFAULT_CARD_FACE_URL,
                        dto.getLastTranSum(),
                        dto.isRegister()
                );
            } catch (IOException e) {
                throw new RuntimeException(new ApiException("連線逾時或網路錯誤，請檢查網路狀態"));
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<CardEntity>> loadCards() {
        return CompletableFuture.supplyAsync(storage::getCards, executor);
    }

    @Override
    public CompletableFuture<Void> saveCards(List<CardEntity> cards) {
        return CompletableFuture.runAsync(() -> storage.saveCards(cards), executor);
    }

    @Override
    public CompletableFuture<List<CardEntity>> refreshCardsBalance(List<CardEntity> existingCards) {
        return CompletableFuture.supplyAsync(() -> {
            if (existingCards == null || existingCards.isEmpty()) {
                return new ArrayList<>();
            }

            List<CardEntity> updatedList = new ArrayList<>();
            for (CardEntity card : existingCards) {
                try {
                    CardEntity freshCard = checkCard(card.getCardNo()).join();
                    updatedList.add(freshCard.copyWith(
                            card.getCardName(),
                            card.getCardFaceUrl(),
                            freshCard.getLastTranSum(),
                            freshCard.isRegister()
                    ));
                } catch (Exception e) {
                    // Fallback to existing card on single-card network failure
                    updatedList.add(card);
                }
            }

            storage.saveCards(updatedList);
            return updatedList;
        }, executor);
    }

    @Override
    public CompletableFuture<CardEntity> addCard(String cardNo, String cardName) {
        return CompletableFuture.supplyAsync(() -> {
            List<CardEntity> current = storage.getCards();
            for (CardEntity c : current) {
                if (c.getCardNo().equals(cardNo)) {
                    throw new RuntimeException(new ApiException("此卡片已存在"));
                }
            }

            CardEntity newCard = checkCard(cardNo).join();
            String name = (cardName != null && !cardName.trim().isEmpty()) ? cardName.trim() : "我的卡片";
            CardEntity finalCard = newCard.copyWithCardName(name);

            current.add(finalCard);
            storage.saveCards(current);
            return finalCard;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> updateCard(CardEntity card) {
        return CompletableFuture.runAsync(() -> storage.updateCard(card), executor);
    }

    @Override
    public CompletableFuture<Void> removeCard(String cardNo) {
        return CompletableFuture.runAsync(() -> storage.deleteCard(cardNo), executor);
    }
}
