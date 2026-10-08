package com.jasonhong.yoyu.presentation.showcase.model;

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Domain model representing a card displayed in the showcase viewer.
 */
public class CardShowcaseItem implements Serializable {

    private final int id;
    private final String url;
    private final String formattedId;
    private boolean isFavorite;
    @Nullable
    private final Rect sourceBounds;

    public CardShowcaseItem(int id, @NonNull String url, boolean isFavorite, @Nullable Rect sourceBounds) {
        this.id = id;
        this.url = url;
        this.formattedId = "#" + id;
        this.isFavorite = isFavorite;
        this.sourceBounds = sourceBounds != null ? new Rect(sourceBounds) : null;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public String getFormattedId() {
        return formattedId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Nullable
    public Rect getSourceBounds() {
        return sourceBounds;
    }
}
