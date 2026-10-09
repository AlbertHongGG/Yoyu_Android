package com.jasonhong.yoyu.data.repository;

import android.content.Context;

import com.jasonhong.yoyu.core.constants.AppConstants;
import com.jasonhong.yoyu.core.network.ApiException;
import com.jasonhong.yoyu.core.network.RetrofitClient;
import com.jasonhong.yoyu.data.api.IPassApiService;
import com.jasonhong.yoyu.data.local.LocalCardStorage;
import com.jasonhong.yoyu.data.model.remote.CheckMyCardsRequest;
import com.jasonhong.yoyu.data.model.remote.CheckMyCardsResponse;
import com.jasonhong.yoyu.domain.model.BatchCardOperationResult;
import com.jasonhong.yoyu.domain.model.BatchCardSequenceGenerator;
import com.jasonhong.yoyu.domain.model.CardEntity;
import com.jasonhong.yoyu.domain.repository.BatchProgressListener;
import com.jasonhong.yoyu.domain.repository.CardRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public CompletableFuture<BatchCardOperationResult> batchAddCards(
            String startCardNo,
            int range,
            BatchProgressListener progressListener
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> sequence = BatchCardSequenceGenerator.generate(startCardNo, range);
            int total = sequence.size();

            if (progressListener != null) {
                progressListener.onProgress(0, total, "檢查既有卡片資料庫...");
            }

            List<CardEntity> current = storage.getCards();
            Set<String> existingNumbers = new HashSet<>();
            for (CardEntity c : current) {
                existingNumbers.add(c.getCardNo());
            }

            List<String> duplicateCardNos = new ArrayList<>();
            List<String> toQueryCardNos = new ArrayList<>();
            for (String no : sequence) {
                if (existingNumbers.contains(no)) {
                    duplicateCardNos.add(no);
                } else {
                    toQueryCardNos.add(no);
                }
            }

            int processedSoFar = duplicateCardNos.size();
            if (progressListener != null) {
                progressListener.onProgress(processedSoFar, total, "正在進行遠端驗證 (" + processedSoFar + "/" + total + ")");
            }

            List<CardEntity> addedCards = new ArrayList<>();
            List<String> failedCardNos = new ArrayList<>();

            final int CHUNK_SIZE = 25;
            for (int i = 0; i < toQueryCardNos.size(); i += CHUNK_SIZE) {
                int end = Math.min(i + CHUNK_SIZE, toQueryCardNos.size());
                List<String> chunk = toQueryCardNos.subList(i, end);

                try {
                    Response<CheckMyCardsResponse> response = apiService.checkMyCards(new CheckMyCardsRequest(chunk)).execute();
                    if (!response.isSuccessful() || response.body() == null) {
                        failedCardNos.addAll(chunk);
                    } else {
                        CheckMyCardsResponse body = response.body();
                        if (!"0".equals(body.getRtnCode())) {
                            failedCardNos.addAll(chunk);
                        } else {
                            List<CheckMyCardsResponse.CardDataDto> dtos = body.getCardNos();
                            Map<String, CheckMyCardsResponse.CardDataDto> dtoMap = new HashMap<>();
                            if (dtos != null) {
                                for (CheckMyCardsResponse.CardDataDto dto : dtos) {
                                    if (dto != null && dto.getCardNo() != null) {
                                        dtoMap.put(dto.getCardNo(), dto);
                                    }
                                }
                            }

                            for (String cardNo : chunk) {
                                CheckMyCardsResponse.CardDataDto dto = dtoMap.get(cardNo);
                                if (dto != null && "0".equals(dto.getErrCode())) {
                                    String faceUrl = (dto.getCardImageUrl() != null && !dto.getCardImageUrl().isEmpty())
                                            ? dto.getCardImageUrl()
                                            : AppConstants.DEFAULT_CARD_FACE_URL;
                                    CardEntity entity = new CardEntity(
                                            dto.getCardNo(),
                                            "我的卡片",
                                            faceUrl,
                                            dto.getLastTranSum(),
                                            dto.isRegister()
                                    );
                                    addedCards.add(entity);
                                } else {
                                    failedCardNos.add(cardNo);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    failedCardNos.addAll(chunk);
                }

                processedSoFar += chunk.size();
                if (progressListener != null) {
                    progressListener.onProgress(processedSoFar, total, "正在進行遠端驗證 (" + processedSoFar + "/" + total + ")");
                }
            }

            if (!addedCards.isEmpty()) {
                current.addAll(addedCards);
                storage.saveCards(current);
            }

            if (progressListener != null) {
                progressListener.onProgress(total, total, "批量新增完成");
            }

            return new BatchCardOperationResult(addedCards, duplicateCardNos, failedCardNos, total);
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
