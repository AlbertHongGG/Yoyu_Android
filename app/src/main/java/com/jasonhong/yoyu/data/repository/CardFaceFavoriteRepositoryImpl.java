package com.jasonhong.yoyu.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.jasonhong.yoyu.domain.repository.CardFaceFavoriteRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CardFaceFavoriteRepositoryImpl implements CardFaceFavoriteRepository {

    private static final String PREF_NAME = "card_face_favorites";
    private static final String KEY_FAVORITE_IDS = "favorite_ids";

    private final SharedPreferences prefs;
    private final Set<Integer> cache = new HashSet<>();
    private final Object lock = new Object();

    public CardFaceFavoriteRepositoryImpl(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadCache();
    }

    private void loadCache() {
        synchronized (lock) {
            Set<String> savedStrings = prefs.getStringSet(KEY_FAVORITE_IDS, Collections.emptySet());
            cache.clear();
            if (savedStrings != null) {
                for (String s : savedStrings) {
                    try {
                        cache.add(Integer.parseInt(s));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    private void persist() {
        synchronized (lock) {
            Set<String> stringSet = new HashSet<>(cache.size());
            for (Integer id : cache) {
                stringSet.add(String.valueOf(id));
            }
            prefs.edit().putStringSet(KEY_FAVORITE_IDS, stringSet).apply();
        }
    }

    @Override
    public boolean isFavorite(int id) {
        synchronized (lock) {
            return cache.contains(id);
        }
    }

    @Override
    public void setFavorite(int id, boolean isFavorite) {
        synchronized (lock) {
            if (isFavorite) {
                cache.add(id);
            } else {
                cache.remove(id);
            }
            persist();
        }
    }

    @Override
    public boolean toggleFavorite(int id) {
        synchronized (lock) {
            boolean newState;
            if (cache.contains(id)) {
                cache.remove(id);
                newState = false;
            } else {
                cache.add(id);
                newState = true;
            }
            persist();
            return newState;
        }
    }

    @Override
    public Set<Integer> getFavoriteIds() {
        synchronized (lock) {
            return new HashSet<>(cache);
        }
    }
}
