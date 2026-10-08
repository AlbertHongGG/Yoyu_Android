package com.jasonhong.yoyu.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jasonhong.yoyu.domain.model.CardEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalCardStorage {

    private static final String PREF_NAME = "yoyu_prefs";
    private static final String KEY_SAVED_CARDS = "saved_cards";

    private final SharedPreferences prefs;
    private final Gson gson;

    public LocalCardStorage(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public synchronized List<CardEntity> getCards() {
        String json = prefs.getString(KEY_SAVED_CARDS, null);
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<ArrayList<CardEntity>>() {}.getType();
            List<CardEntity> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized void saveCards(List<CardEntity> cards) {
        if (cards == null) {
            cards = Collections.emptyList();
        }
        String json = gson.toJson(cards);
        prefs.edit().putString(KEY_SAVED_CARDS, json).apply();
    }

    public synchronized void addCard(CardEntity card) {
        List<CardEntity> current = getCards();
        // check if duplicate
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getCardNo().equals(card.getCardNo())) {
                current.set(i, card);
                saveCards(current);
                return;
            }
        }
        current.add(card);
        saveCards(current);
    }

    public synchronized void updateCard(CardEntity card) {
        List<CardEntity> current = getCards();
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getCardNo().equals(card.getCardNo())) {
                current.set(i, card);
                saveCards(current);
                return;
            }
        }
    }

    public synchronized void deleteCard(String cardNo) {
        List<CardEntity> current = getCards();
        boolean changed = current.removeIf(c -> c.getCardNo().equals(cardNo));
        if (changed) {
            saveCards(current);
        }
    }
}
