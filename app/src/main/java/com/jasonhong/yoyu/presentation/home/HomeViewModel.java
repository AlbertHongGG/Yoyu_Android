package com.jasonhong.yoyu.presentation.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jasonhong.yoyu.core.base.Resource;
import com.jasonhong.yoyu.data.repository.CardRepositoryImpl;
import com.jasonhong.yoyu.domain.model.CardEntity;
import com.jasonhong.yoyu.domain.repository.CardRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final CardRepository cardRepository;
    private final MutableLiveData<Resource<List<CardEntity>>> cardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<CardEntity>> addCardLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDraggingLiveData = new MutableLiveData<>(false);

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.cardRepository = new CardRepositoryImpl(application);
        loadCards();
    }

    public LiveData<Resource<List<CardEntity>>> getCardsLiveData() {
        return cardsLiveData;
    }

    public LiveData<Resource<CardEntity>> getAddCardLiveData() {
        return addCardLiveData;
    }

    public LiveData<Boolean> getIsDraggingLiveData() {
        return isDraggingLiveData;
    }

    public void setDragging(boolean isDragging) {
        isDraggingLiveData.postValue(isDragging);
    }

    public void loadCards() {
        cardsLiveData.postValue(Resource.loading());
        cardRepository.loadCards()
                .thenAccept(localCards -> {
                    cardsLiveData.postValue(Resource.success(localCards));
                    // Silently refresh balances from network if there are cards
                    if (localCards != null && !localCards.isEmpty()) {
                        cardRepository.refreshCardsBalance(localCards)
                                .thenAccept(updatedCards -> cardsLiveData.postValue(Resource.success(updatedCards)))
                                .exceptionally(throwable -> null);
                    }
                })
                .exceptionally(throwable -> {
                    String msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
                    cardsLiveData.postValue(Resource.error(msg != null ? msg : "載入失敗", new ArrayList<>()));
                    return null;
                });
    }

    public void refresh() {
        cardRepository.loadCards()
                .thenCompose(cardRepository::refreshCardsBalance)
                .thenAccept(updatedCards -> cardsLiveData.postValue(Resource.success(updatedCards)))
                .exceptionally(throwable -> {
                    String msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
                    cardsLiveData.postValue(Resource.error(msg != null ? msg : "更新失敗", null));
                    return null;
                });
    }

    public void addCard(String cardNo, String cardName) {
        addCardLiveData.postValue(Resource.loading());
        cardRepository.addCard(cardNo, cardName)
                .thenAccept(newCard -> {
                    addCardLiveData.postValue(Resource.success(newCard));
                    // Refresh cards list
                    cardRepository.loadCards().thenAccept(list -> cardsLiveData.postValue(Resource.success(list)));
                })
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                    String msg = cause.getMessage() != null ? cause.getMessage() : "新增失敗";
                    addCardLiveData.postValue(Resource.error(msg));
                    return null;
                });
    }

    public void deleteCard(String cardNo) {
        cardRepository.removeCard(cardNo)
                .thenCompose(v -> cardRepository.loadCards())
                .thenAccept(remainingCards -> cardsLiveData.postValue(Resource.success(remainingCards)))
                .exceptionally(throwable -> null);
    }

    public void updateCardFace(String cardNo, String newFaceUrl) {
        cardRepository.loadCards()
                .thenAccept(cards -> {
                    for (CardEntity c : cards) {
                        if (c.getCardNo().equals(cardNo)) {
                            CardEntity updated = c.copyWithCardFaceUrl(newFaceUrl);
                            cardRepository.updateCard(updated).join();
                            break;
                        }
                    }
                    cardRepository.loadCards().thenAccept(list -> cardsLiveData.postValue(Resource.success(list)));
                });
    }
}
