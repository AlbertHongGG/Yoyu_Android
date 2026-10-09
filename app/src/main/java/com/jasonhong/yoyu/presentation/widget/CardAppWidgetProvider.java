package com.jasonhong.yoyu.presentation.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * AppWidgetProvider handling lifecycle callbacks and system/custom broadcast events:
 * - APPWIDGET_UPDATE / onUpdate: Renders all active widgets with fresh cache data.
 * - ACTION_CYCLE_CARD: Cycles the specific appWidgetId to the next card.
 * - ACTION_REFRESH_BALANCE: Triggers a balance refresh for the card displayed on the widget.
 * - ACTION_CARD_DATA_CHANGED: Triggered by the app when cards or card covers are updated.
 * - ACTION_USER_PRESENT: Screen unlocked event; performs instant cache render + debounced sync.
 * - onDeleted: Removes persisted preference for the removed widget.
 */
public class CardAppWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_CYCLE_CARD = "com.jasonhong.yoyu.ACTION_CYCLE_CARD";
    public static final String ACTION_REFRESH_BALANCE = "com.jasonhong.yoyu.ACTION_REFRESH_BALANCE";
    public static final String ACTION_CARD_DATA_CHANGED = "com.jasonhong.yoyu.ACTION_CARD_DATA_CHANGED";

    public static final String EXTRA_APPWIDGET_ID = "appWidgetId";
    public static final String EXTRA_TARGET_CARD_NO = "targetCardNo";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (action == null) return;

        CardWidgetInteractor interactor = CardWidgetInteractor.getInstance(context);

        switch (action) {
            case ACTION_CYCLE_CARD: {
                int appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    interactor.cycleNextCard(appWidgetId);
                }
                break;
            }
            case ACTION_REFRESH_BALANCE: {
                int appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                String cardNo = intent.getStringExtra(EXTRA_TARGET_CARD_NO);
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    interactor.refreshCardBalance(appWidgetId, cardNo);
                }
                break;
            }
            case ACTION_CARD_DATA_CHANGED: {
                interactor.updateAllWidgets();
                break;
            }
            case Intent.ACTION_USER_PRESENT: {
                interactor.onUserPresent();
                break;
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            CardWidgetInteractor.getInstance(context).updateWidgets(appWidgetIds);
        } else {
            CardWidgetInteractor.getInstance(context).updateAllWidgets();
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        if (appWidgetIds != null) {
            CardWidgetStore store = CardWidgetInteractor.getInstance(context).getStore();
            for (int id : appWidgetIds) {
                store.removeWidget(id);
            }
        }
    }
}
