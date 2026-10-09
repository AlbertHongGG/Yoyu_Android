package com.jasonhong.yoyu.presentation.home;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import com.jasonhong.yoyu.presentation.widget.CardAppWidgetProvider;

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

    public interface AddCardCallback {
        void onSuccess(CardEntity card);
        void onError(String message);
    }

    public interface BatchCallback {
        void onProgress(int processed, int total, String message);
        void onSuccess(com.jasonhong.yoyu.domain.model.BatchCardOperationResult result);
        void onError(String message);
    }

    private final CardRepository cardRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Resource<List<CardEntity>>> cardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDraggingLiveData = new MutableLiveData<>(false);

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.cardRepository = new CardRepositoryImpl(application);
        loadCards();
    }

    public LiveData<Resource<List<CardEntity>>> getCardsLiveData() {
        return cardsLiveData;
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
                                .thenAccept(updatedCards -> {
                                    cardsLiveData.postValue(Resource.success(updatedCards));
                                    notifyWidgetDataChanged();
                                })
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
                .thenAccept(updatedCards -> {
                    cardsLiveData.postValue(Resource.success(updatedCards));
                    notifyWidgetDataChanged();
                })
                .exceptionally(throwable -> {
                    String msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
                    cardsLiveData.postValue(Resource.error(msg != null ? msg : "更新失敗", null));
                    return null;
                });
    }

    public void addCard(String cardNo, String cardName, AddCardCallback callback) {
        cardRepository.addCard(cardNo, cardName)
                .thenAccept(newCard -> {
                    cardRepository.loadCards().thenAccept(list -> cardsLiveData.postValue(Resource.success(list)));
                    notifyWidgetDataChanged();
                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(newCard));
                    }
                })
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                    String msg = cause.getMessage() != null ? cause.getMessage() : "新增失敗";
                    if (callback != null) {
                        mainHandler.post(() -> callback.onError(msg));
                    }
                    return null;
                });
    }

    public void batchAddCards(String startCardNo, int range, BatchCallback callback) {
        cardRepository.batchAddCards(startCardNo, range, (processed, total, message) -> {
            if (callback != null) {
                mainHandler.post(() -> callback.onProgress(processed, total, message));
            }
        }).thenAccept(result -> {
            cardRepository.loadCards().thenAccept(list -> cardsLiveData.postValue(Resource.success(list)));
            if (result.hasAnySuccess()) {
                notifyWidgetDataChanged();
            }
            if (callback != null) {
                mainHandler.post(() -> callback.onSuccess(result));
            }
        }).exceptionally(throwable -> {
            Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
            String msg = cause.getMessage() != null ? cause.getMessage() : "批量新增失敗";
            if (callback != null) {
                mainHandler.post(() -> callback.onError(msg));
            }
            return null;
        });
    }

    public void deleteCard(String cardNo) {
        cardRepository.removeCard(cardNo)
                .thenCompose(v -> cardRepository.loadCards())
                .thenAccept(remainingCards -> {
                    cardsLiveData.postValue(Resource.success(remainingCards));
                    notifyWidgetDataChanged();
                })
                .exceptionally(throwable -> null);
    }

    public void updateCardFace(String cardNo, String newFaceUrl) {
        cardRepository.loadCards()
                .thenAccept(cards -> {
                    for (int i = 0; i < cards.size(); i++) {
                        if (cards.get(i).getCardNo().equals(cardNo)) {
                            cards.set(i, cards.get(i).copyWithCardFaceUrl(newFaceUrl));
                            break;
                        }
                    }
                    cardRepository.saveCards(cards).join();
                    cardsLiveData.postValue(Resource.success(cards));
                    notifyWidgetDataChanged();
                });
    }

    private void notifyWidgetDataChanged() {
        try {
            Intent intent = new Intent(CardAppWidgetProvider.ACTION_CARD_DATA_CHANGED);
            intent.setPackage(getApplication().getPackageName());
            getApplication().sendBroadcast(intent);
        } catch (Exception ignored) {}
    }
}
