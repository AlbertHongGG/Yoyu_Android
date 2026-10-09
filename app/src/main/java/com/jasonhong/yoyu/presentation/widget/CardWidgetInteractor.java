package com.jasonhong.yoyu.presentation.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.jasonhong.yoyu.data.repository.CardRepositoryImpl;
import com.jasonhong.yoyu.domain.model.CardEntity;
import com.jasonhong.yoyu.domain.repository.CardRepository;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Domain Interactor coordinating widget state transitions, multi-card cycling,
 * background rendering, and repository synchronization.
 */
public class CardWidgetInteractor {

    private static volatile CardWidgetInteractor instance;

    public static CardWidgetInteractor getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (CardWidgetInteractor.class) {
                if (instance == null) {
                    instance = new CardWidgetInteractor(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final Context appContext;
    private final CardRepository cardRepository;
    private final CardWidgetStore widgetStore;
    private final CardWidgetRenderer renderer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private CardWidgetInteractor(@NonNull Context appContext) {
        this.appContext = appContext;
        this.cardRepository = new CardRepositoryImpl(appContext);
        this.widgetStore = new CardWidgetStore(appContext);
        this.renderer = new CardWidgetRenderer();
    }

    public CardWidgetStore getStore() {
        return widgetStore;
    }

    public void updateAllWidgets() {
        executor.execute(() -> {
            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            ComponentName component = new ComponentName(appContext, CardAppWidgetProvider.class);
            int[] appWidgetIds = manager.getAppWidgetIds(component);
            if (appWidgetIds == null || appWidgetIds.length == 0) return;

            List<CardEntity> cards = getCardsSafe();
            for (int id : appWidgetIds) {
                renderWidgetInternal(manager, id, cards);
            }
        });
    }

    public void updateWidgets(int[] appWidgetIds) {
        if (appWidgetIds == null || appWidgetIds.length == 0) return;
        executor.execute(() -> {
            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            List<CardEntity> cards = getCardsSafe();
            for (int id : appWidgetIds) {
                renderWidgetInternal(manager, id, cards);
            }
        });
    }

    public void updateWidget(int appWidgetId) {
        executor.execute(() -> {
            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            List<CardEntity> cards = getCardsSafe();
            renderWidgetInternal(manager, appWidgetId, cards);
        });
    }

    public void cycleNextCard(int appWidgetId) {
        executor.execute(() -> {
            triggerHaptic(20);
            List<CardEntity> cards = getCardsSafe();
            if (cards.isEmpty()) return;

            String currentBoundNo = widgetStore.getBoundCardNo(appWidgetId);
            int currentIndex = findCardIndex(cards, currentBoundNo);
            int nextIndex = (currentIndex + 1) % cards.size();

            CardEntity nextCard = cards.get(nextIndex);
            widgetStore.bindCardNo(appWidgetId, nextCard.getCardNo());

            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            renderWidgetInternal(manager, appWidgetId, cards);
        });
    }

    public void refreshCardBalance(int appWidgetId, String cardNo) {
        executor.execute(() -> {
            triggerHaptic(25);
            List<CardEntity> cards = getCardsSafe();
            if (cards.isEmpty()) return;

            try {
                // Silently refresh balance from network
                List<CardEntity> refreshed = cardRepository.refreshCardsBalance(cards).get();
                if (refreshed != null && !refreshed.isEmpty()) {
                    cards = refreshed;
                }
            } catch (Exception ignored) {}

            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            renderWidgetInternal(manager, appWidgetId, cards);
        });
    }

    public void onUserPresent() {
        executor.execute(() -> {
            AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
            ComponentName component = new ComponentName(appContext, CardAppWidgetProvider.class);
            int[] appWidgetIds = manager.getAppWidgetIds(component);
            if (appWidgetIds == null || appWidgetIds.length == 0) return;

            // 1. Instant Cache-First render (0ms latency for user unlocking screen)
            List<CardEntity> cards = getCardsSafe();
            for (int id : appWidgetIds) {
                renderWidgetInternal(manager, id, cards);
            }

            // 2. Debounced background balance check (at most once every 10 minutes)
            long now = System.currentTimeMillis();
            long lastRefresh = widgetStore.getLastRefreshTimestamp();
            if (now - lastRefresh > 10 * 60 * 1000L && !cards.isEmpty()) {
                widgetStore.setLastRefreshTimestamp(now);
                try {
                    List<CardEntity> refreshed = cardRepository.refreshCardsBalance(cards).get();
                    if (refreshed != null && !refreshed.isEmpty()) {
                        for (int id : appWidgetIds) {
                            renderWidgetInternal(manager, id, refreshed);
                        }
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    private void renderWidgetInternal(AppWidgetManager manager, int appWidgetId, List<CardEntity> cards) {
        CardEntity targetCard = null;
        int targetIndex = 0;

        if (!cards.isEmpty()) {
            String boundNo = widgetStore.getBoundCardNo(appWidgetId);
            targetIndex = findCardIndex(cards, boundNo);
            if (targetIndex < 0) {
                targetIndex = 0;
            }
            targetCard = cards.get(targetIndex);
            widgetStore.bindCardNo(appWidgetId, targetCard.getCardNo());
        }

        RemoteViews views = renderer.render(appContext, appWidgetId, targetCard, targetIndex, cards.size());
        manager.updateAppWidget(appWidgetId, views);
    }

    private List<CardEntity> getCardsSafe() {
        try {
            List<CardEntity> cards = cardRepository.loadCards().get();
            return cards != null ? cards : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private int findCardIndex(List<CardEntity> cards, String cardNo) {
        if (cardNo == null || cards == null) return 0;
        for (int i = 0; i < cards.size(); i++) {
            if (cardNo.equals(cards.get(i).getCardNo())) {
                return i;
            }
        }
        return 0;
    }

    private void triggerHaptic(int ms) {
        Vibrator vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        }
    }
}
