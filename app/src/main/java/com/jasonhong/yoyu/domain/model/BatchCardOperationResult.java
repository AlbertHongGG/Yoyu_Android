package com.jasonhong.yoyu.domain.model;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * Immutable value object reporting the detailed outcome of a batch card addition operation.
 */
public final class BatchCardOperationResult {

    @NonNull
    private final List<CardEntity> addedCards;
    @NonNull
    private final List<String> duplicateCardNos;
    @NonNull
    private final List<String> failedCardNos;
    private final int totalRequested;

    public BatchCardOperationResult(@NonNull List<CardEntity> addedCards,
                                    @NonNull List<String> duplicateCardNos,
                                    @NonNull List<String> failedCardNos,
                                    int totalRequested) {
        this.addedCards = Collections.unmodifiableList(addedCards != null ? addedCards : Collections.emptyList());
        this.duplicateCardNos = Collections.unmodifiableList(duplicateCardNos != null ? duplicateCardNos : Collections.emptyList());
        this.failedCardNos = Collections.unmodifiableList(failedCardNos != null ? failedCardNos : Collections.emptyList());
        this.totalRequested = totalRequested;
    }

    @NonNull
    public List<CardEntity> getAddedCards() {
        return addedCards;
    }

    @NonNull
    public List<String> getDuplicateCardNos() {
        return duplicateCardNos;
    }

    @NonNull
    public List<String> getFailedCardNos() {
        return failedCardNos;
    }

    public int getTotalRequested() {
        return totalRequested;
    }

    public int getSuccessCount() {
        return addedCards.size();
    }

    public int getDuplicateCount() {
        return duplicateCardNos.size();
    }

    public int getFailedCount() {
        return failedCardNos.size();
    }

    public boolean isAllSuccess() {
        return addedCards.size() == totalRequested;
    }

    public boolean hasAnySuccess() {
        return !addedCards.isEmpty();
    }
}
