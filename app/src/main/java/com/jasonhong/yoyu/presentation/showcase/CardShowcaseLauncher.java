package com.jasonhong.yoyu.presentation.showcase;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jasonhong.yoyu.domain.model.CardFaceItem;
import com.jasonhong.yoyu.presentation.showcase.model.CardShowcaseItem;

/**
 * Clean facade launcher for opening the 3D Card Showcase Viewer.
 */
public final class CardShowcaseLauncher {

    private CardShowcaseLauncher() {}

    public static void show(@NonNull Activity activity,
                            @Nullable View sourceView,
                            @NonNull CardFaceItem item,
                            @NonNull CardShowcaseDialog.OnShowcaseActionListener listener) {
        Rect bounds = null;
        if (sourceView != null) {
            int[] loc = new int[2];
            sourceView.getLocationOnScreen(loc);
            bounds = new Rect(loc[0], loc[1], loc[0] + sourceView.getWidth(), loc[1] + sourceView.getHeight());
        }

        CardShowcaseItem showcaseItem = new CardShowcaseItem(
                item.getId(),
                item.getUrl(),
                item.isFavorite(),
                bounds
        );

        CardShowcaseDialog dialog = new CardShowcaseDialog(activity, showcaseItem, listener);
        dialog.show();
    }
}
