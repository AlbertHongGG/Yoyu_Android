package com.jasonhong.yoyu.domain.model;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Objects;

/**
 * Pure domain card entity where card cover is modeled strictly as an integer cardFaceId,
 * referencing CardFaceCatalog as the single source of truth.
 */
public class CardEntity implements Serializable {

    @SerializedName("cardNo")
    private final String cardNo;

    @SerializedName("cardName")
    private final String cardName;

    @SerializedName("cardFaceId")
    private final int cardFaceId;

    // Gracefully deserializes legacy records where cardFaceUrl was saved as a string
    @SerializedName("cardFaceUrl")
    private final String legacyCardFaceUrl;

    @SerializedName("lastTranSum")
    private final double lastTranSum;

    @SerializedName("isRegister")
    private final boolean isRegister;

    public CardEntity(
            @NonNull String cardNo,
            String cardName,
            int cardFaceId,
            double lastTranSum,
            boolean isRegister
    ) {
        this.cardNo = Objects.requireNonNull(cardNo, "cardNo must not be null");
        this.cardName = (cardName != null && !cardName.trim().isEmpty()) ? cardName.trim() : "我的卡片";
        this.cardFaceId = CardFaceCatalog.normalize(cardFaceId);
        this.legacyCardFaceUrl = null;
        this.lastTranSum = lastTranSum;
        this.isRegister = isRegister;
    }

    public CardEntity(
            @NonNull String cardNo,
            String cardName,
            String cardFaceUrlOrId,
            double lastTranSum,
            boolean isRegister
    ) {
        this.cardNo = Objects.requireNonNull(cardNo, "cardNo must not be null");
        this.cardName = (cardName != null && !cardName.trim().isEmpty()) ? cardName.trim() : "我的卡片";
        this.cardFaceId = CardFaceCatalog.resolveFromUrl(cardFaceUrlOrId);
        this.legacyCardFaceUrl = null;
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

    public int getCardFaceId() {
        if (cardFaceId != 0 && CardFaceCatalog.isValid(cardFaceId)) {
            return cardFaceId;
        }
        if (legacyCardFaceUrl != null && !legacyCardFaceUrl.trim().isEmpty()) {
            return CardFaceCatalog.resolveFromUrl(legacyCardFaceUrl);
        }
        return CardFaceCatalog.DEFAULT_FACE_ID;
    }

    @NonNull
    public String getCardFaceUrl() {
        return CardFaceCatalog.getUrl(getCardFaceId());
    }

    public boolean isDefaultFace() {
        return getCardFaceId() == CardFaceCatalog.DEFAULT_FACE_ID;
    }

    public double getLastTranSum() {
        return lastTranSum;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public CardEntity copyWithCardName(String newCardName) {
        return new CardEntity(this.cardNo, newCardName, getCardFaceId(), this.lastTranSum, this.isRegister);
    }

    public CardEntity copyWithCardFaceId(int newCardFaceId) {
        return new CardEntity(this.cardNo, this.cardName, newCardFaceId, this.lastTranSum, this.isRegister);
    }

    public CardEntity copyWithBalance(double newBalance) {
        return new CardEntity(this.cardNo, this.cardName, getCardFaceId(), newBalance, this.isRegister);
    }

    public CardEntity copyWith(String newCardName, int newCardFaceId, double newBalance, boolean newIsRegister) {
        return new CardEntity(
                this.cardNo,
                newCardName != null ? newCardName : this.cardName,
                newCardFaceId,
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
                getCardFaceId() == that.getCardFaceId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNo, cardName, getCardFaceId(), lastTranSum, isRegister);
    }

    @NonNull
    @Override
    public String toString() {
        return "CardEntity{" +
                "cardNo='" + cardNo + '\'' +
                ", cardName='" + cardName + '\'' +
                ", cardFaceId=" + getCardFaceId() +
                ", lastTranSum=" + lastTranSum +
                ", isRegister=" + isRegister +
                '}';
    }
}
