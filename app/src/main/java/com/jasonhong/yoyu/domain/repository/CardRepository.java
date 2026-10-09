package com.jasonhong.yoyu.domain.repository;

import com.jasonhong.yoyu.domain.model.CardEntity;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CardRepository {

    CompletableFuture<CardEntity> checkCard(String cardNo);

    CompletableFuture<List<CardEntity>> loadCards();

    CompletableFuture<Void> saveCards(List<CardEntity> cards);

    CompletableFuture<List<CardEntity>> refreshCardsBalance(List<CardEntity> existingCards);

    CompletableFuture<CardEntity> addCard(String cardNo, String cardName);

    CompletableFuture<com.jasonhong.yoyu.domain.model.BatchCardOperationResult> batchAddCards(
            String startCardNo,
            int range,
            BatchProgressListener progressListener
    );

    CompletableFuture<Void> updateCard(CardEntity card);

    CompletableFuture<Void> removeCard(String cardNo);
}
