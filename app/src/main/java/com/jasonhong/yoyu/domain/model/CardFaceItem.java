package com.jasonhong.yoyu.domain.model;

import java.io.Serializable;
import java.util.Objects;

public final class CardFaceItem implements Serializable, Comparable<CardFaceItem> {
    private final int id;
    private final String url;
    private boolean isFavorite;

    public CardFaceItem(int id, String url, boolean isFavorite) {
        this.id = id;
        this.url = url;
        this.isFavorite = isFavorite;
    }

    public int getId() {
        return id;
    }

    public String getFormattedId() {
        return "#" + id;
    }

    public String getUrl() {
        return url;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public CardFaceItem copyWithFavorite(boolean favorite) {
        return new CardFaceItem(id, url, favorite);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardFaceItem that = (CardFaceItem) o;
        return id == that.id && isFavorite == that.isFavorite && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, isFavorite);
    }

    @Override
    public int compareTo(CardFaceItem o) {
        if (o == null) return 1;
        return Integer.compare(this.id, o.id);
    }
}
