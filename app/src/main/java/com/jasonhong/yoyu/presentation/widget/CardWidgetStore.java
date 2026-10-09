package com.jasonhong.yoyu.presentation.widget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Isolated repository managing persistent state for Home Screen Widgets:
 * - Maps each unique appWidgetId to its currently bound cardNo.
 * - Tracks last refresh timestamps for debounced network synchronization.
 * - Cleans up preferences when widgets are deleted.
 */
public class CardWidgetStore {

    private static final String PREF_NAME = "card_widget_prefs";
    private static final String KEY_CARD_PREFIX = "bound_card_";
    private static final String KEY_LAST_REFRESH = "last_refresh_time";

    private final SharedPreferences prefs;

    public CardWidgetStore(@NonNull Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    public String getBoundCardNo(int appWidgetId) {
        return prefs.getString(KEY_CARD_PREFIX + appWidgetId, null);
    }

    public void bindCardNo(int appWidgetId, @NonNull String cardNo) {
        prefs.edit().putString(KEY_CARD_PREFIX + appWidgetId, cardNo).apply();
    }

    public void removeWidget(int appWidgetId) {
        prefs.edit().remove(KEY_CARD_PREFIX + appWidgetId).apply();
    }

    public long getLastRefreshTimestamp() {
        return prefs.getLong(KEY_LAST_REFRESH, 0L);
    }

    public void setLastRefreshTimestamp(long timestamp) {
        prefs.edit().putLong(KEY_LAST_REFRESH, timestamp).apply();
    }
}
