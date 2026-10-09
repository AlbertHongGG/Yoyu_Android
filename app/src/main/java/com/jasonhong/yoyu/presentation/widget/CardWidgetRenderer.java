package com.jasonhong.yoyu.presentation.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.core.navigation.AppLaunchPayload;
import com.jasonhong.yoyu.domain.model.CardEntity;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Dedicated Renderer constructing RemoteViews for the 4x2 Home Screen Widget:
 * - Full-bleed card cover with 90° rotation and CenterCrop.
 * - Safe downsampling to 960x600 px to prevent Binder TransactionTooLargeException.
 * - Three independent action zones (Cycle card on body, open App on name/ID, refresh on balance).
 */
public class CardWidgetRenderer {

    private final NumberFormat currencyFormat;

    public CardWidgetRenderer() {
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.TAIWAN);
        this.currencyFormat.setMaximumFractionDigits(0);
        this.currencyFormat.setMinimumFractionDigits(0);
    }

    @NonNull
    public RemoteViews render(@NonNull Context context,
                              int appWidgetId,
                              @Nullable CardEntity card,
                              int cardIndex,
                              int totalCards) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_card_4x2);

        if (card == null) {
            renderEmptyState(context, views, appWidgetId);
            return views;
        }

        // 1. Text Information
        views.setTextViewText(R.id.tvWidgetCardName, card.getCardName());
        views.setTextViewText(R.id.tvWidgetCardNo, card.getCardNo());
        views.setTextViewText(R.id.tvWidgetBalance, currencyFormat.format(card.getLastTranSum()));

        // 2. Multi-card Indicator (e.g. "1/3")
        if (totalCards > 1) {
            views.setViewVisibility(R.id.tvWidgetIndicator, View.VISIBLE);
            views.setTextViewText(R.id.tvWidgetIndicator, (cardIndex + 1) + " / " + totalCards);
        } else {
            views.setViewVisibility(R.id.tvWidgetIndicator, View.GONE);
        }

        // 3. Load full-bleed rotated card cover via Glide
        loadCoverBitmap(context, views, card.getCardFaceUrl());

        // 4. PendingIntents for Touch Partitioning
        bindInteractions(context, views, appWidgetId, card, totalCards);

        return views;
    }

    private void renderEmptyState(@NonNull Context context, @NonNull RemoteViews views, int appWidgetId) {
        views.setTextViewText(R.id.tvWidgetCardName, "尚未綁定卡片");
        views.setTextViewText(R.id.tvWidgetCardNo, "點擊開啟 App 新增");
        views.setTextViewText(R.id.tvWidgetBalance, "$0");
        views.setViewVisibility(R.id.tvWidgetIndicator, View.GONE);

        PendingIntent pi = AppLaunchPayload.createWidgetEmptyLaunchPendingIntent(context, appWidgetId);
        views.setOnClickPendingIntent(R.id.widgetRoot, pi);
    }

    private void loadCoverBitmap(@NonNull Context context, @NonNull RemoteViews views, @Nullable String url) {
        if (url == null || url.trim().isEmpty()) return;

        try {
            int cornerRadiusPx = Math.round(20 * context.getResources().getDisplayMetrics().density);
            Bitmap bitmap = Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(url)
                    .transform(new CenterCrop(), new RoundedCorners(cornerRadiusPx))
                    .override(640, 400)
                    .submit()
                    .get(3500, TimeUnit.MILLISECONDS);

            if (bitmap != null) {
                views.setImageViewBitmap(R.id.ivWidgetCardFace, bitmap);
            }
        } catch (Exception ignored) {
            // Graceful fallback: layout displays default dark gradient background
        }
    }

    private void bindInteractions(@NonNull Context context,
                                  @NonNull RemoteViews views,
                                  int appWidgetId,
                                  @NonNull CardEntity card,
                                  int totalCards) {
        // Zone A: Card Body Tap -> Cycle to next card directly on desktop (or open app if only 1 card)
        if (totalCards > 1) {
            Intent cycleIntent = new Intent(context, CardAppWidgetProvider.class);
            cycleIntent.setAction(CardAppWidgetProvider.ACTION_CYCLE_CARD);
            cycleIntent.putExtra(CardAppWidgetProvider.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent cyclePi = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    cycleIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widgetRoot, cyclePi);
            views.setOnClickPendingIntent(R.id.ivWidgetCardFace, cyclePi);
            views.setOnClickPendingIntent(R.id.viewWidgetScrim, cyclePi);
        } else {
            PendingIntent launchPi = AppLaunchPayload.createWidgetCardLaunchPendingIntent(
                    context,
                    appWidgetId,
                    card.getCardNo()
            );
            views.setOnClickPendingIntent(R.id.widgetRoot, launchPi);
            views.setOnClickPendingIntent(R.id.ivWidgetCardFace, launchPi);
            views.setOnClickPendingIntent(R.id.viewWidgetScrim, launchPi);
        }

        // Zone B: Bottom-Left Card Info Tap (Name & ID) -> Open App & Focus on this Card
        PendingIntent cardInfoPi = AppLaunchPayload.createWidgetCardLaunchPendingIntent(
                context,
                appWidgetId * 100 + 1,
                card.getCardNo()
        );
        views.setOnClickPendingIntent(R.id.layoutCardInfo, cardInfoPi);

        // Zone C: Bottom-Right Balance Tap -> Trigger manual balance refresh
        Intent refreshIntent = new Intent(context, CardAppWidgetProvider.class);
        refreshIntent.setAction(CardAppWidgetProvider.ACTION_REFRESH_BALANCE);
        refreshIntent.putExtra(CardAppWidgetProvider.EXTRA_APPWIDGET_ID, appWidgetId);
        refreshIntent.putExtra(CardAppWidgetProvider.EXTRA_TARGET_CARD_NO, card.getCardNo());
        PendingIntent refreshPi = PendingIntent.getBroadcast(
                context,
                appWidgetId * 100 + 2,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.tvWidgetBalance, refreshPi);
    }
}
