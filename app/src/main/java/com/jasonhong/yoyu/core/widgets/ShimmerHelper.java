package com.jasonhong.yoyu.core.widgets;

import android.content.Context;
import android.content.res.Configuration;

import androidx.core.content.ContextCompat;

import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.jasonhong.yoyu.R;

public final class ShimmerHelper {

    private ShimmerHelper() {}

    public static ShimmerDrawable createMetallicShimmer(Context context) {
        boolean isDark = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int baseColor = ContextCompat.getColor(context, isDark ? R.color.shimmer_base_dark : R.color.shimmer_base_light);
        int highlightColor = ContextCompat.getColor(context, isDark ? R.color.shimmer_highlight_dark : R.color.shimmer_highlight_light);

        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(baseColor)
                .setHighlightColor(highlightColor)
                .setDuration(1200)
                .setBaseAlpha(1.0f)
                .setHighlightAlpha(0.65f)
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setTilt(20f)
                .setAutoStart(true)
                .build();

        ShimmerDrawable drawable = new ShimmerDrawable();
        drawable.setShimmer(shimmer);
        return drawable;
    }
}
