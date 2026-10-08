package com.jasonhong.yoyu.domain.model;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import com.jasonhong.yoyu.core.constants.AppConstants;
import java.io.Serializable;
import java.util.Objects;

public class CardEntity implements Serializable {

    @SerializedName("cardNo")
    private final String cardNo;

    @SerializedName("cardName")
    private final String cardName;

    @SerializedName("cardFaceUrl")
    private final String cardFaceUrl;

    @SerializedName("lastTranSum")
    private final double lastTranSum;

    @SerializedName("isRegister")
    private final boolean isRegister;

    public CardEntity(
            @NonNull String cardNo,
            String cardName,
            String cardFaceUrl,
            double lastTranSum,
            boolean isRegister
    ) {
        this.cardNo = Objects.requireNonNull(cardNo, "cardNo must not be null");
        this.cardName = (cardName != null && !cardName.trim().isEmpty()) ? cardName.trim() : "我的卡片";
        this.cardFaceUrl = (cardFaceUrl != null && !cardFaceUrl.trim().isEmpty()) ? cardFaceUrl.trim() : AppConstants.DEFAULT_CARD_FACE_URL;
        this.lastTranSum = lastTranSum;
        this.isRegister = isRegister;
    }

    @NonNull
    public String getCardNo() {
        return cardNo;
    }

    @NonNull
    public String getCardName() {
        return cardName;
    }

    @NonNull
    public String getCardFaceUrl() {
        return cardFaceUrl;
    }

    public double getLastTranSum() {
        return lastTranSum;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public CardEntity copyWithCardName(String newCardName) {
        return new CardEntity(this.cardNo, newCardName, this.cardFaceUrl, this.lastTranSum, this.isRegister);
    }

    public CardEntity copyWithCardFaceUrl(String newCardFaceUrl) {
        return new CardEntity(this.cardNo, this.cardName, newCardFaceUrl, this.lastTranSum, this.isRegister);
    }

    public CardEntity copyWithBalance(double newBalance) {
        return new CardEntity(this.cardNo, this.cardName, this.cardFaceUrl, newBalance, this.isRegister);
    }

    public CardEntity copyWith(String newCardName, String newCardFaceUrl, double newBalance, boolean newIsRegister) {
        return new CardEntity(
                this.cardNo,
                newCardName != null ? newCardName : this.cardName,
                newCardFaceUrl != null ? newCardFaceUrl : this.cardFaceUrl,
                newBalance,
                newIsRegister
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardEntity that = (CardEntity) o;
        return Double.compare(that.lastTranSum, lastTranSum) == 0 &&
                isRegister == that.isRegister &&
                cardNo.equals(that.cardNo) &&
                cardName.equals(that.cardName) &&
                cardFaceUrl.equals(that.cardFaceUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNo, cardName, cardFaceUrl, lastTranSum, isRegister);
    }

    @NonNull
    @Override
    public String toString() {
        return "CardEntity{" +
                "cardNo='" + cardNo + '\'' +
                ", cardName='" + cardName + '\'' +
                ", cardFaceUrl='" + cardFaceUrl + '\'' +
                ", lastTranSum=" + lastTranSum +
                ", isRegister=" + isRegister +
                '}';
    }
}
