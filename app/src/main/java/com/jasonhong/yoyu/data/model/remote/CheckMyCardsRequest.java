package com.jasonhong.yoyu.data.model.remote;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public class CheckMyCardsRequest {
    @SerializedName("cardNos")
    private final List<String> cardNos;

    public CheckMyCardsRequest(List<String> cardNos) {
        this.cardNos = cardNos;
    }

    public CheckMyCardsRequest(String cardNo) {
        this.cardNos = Collections.singletonList(cardNo);
    }

    public List<String> getCardNos() {
        return cardNos;
    }
}
