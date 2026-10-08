package com.jasonhong.yoyu.domain.repository;

import java.util.Set;

public interface CardFaceFavoriteRepository {
    boolean isFavorite(int id);
    void setFavorite(int id, boolean isFavorite);
    boolean toggleFavorite(int id);
    Set<Integer> getFavoriteIds();
}
