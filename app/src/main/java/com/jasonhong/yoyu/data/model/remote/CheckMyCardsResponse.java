package com.jasonhong.yoyu.data.model.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CheckMyCardsResponse {
    @SerializedName("rtnCode")
    private String rtnCode;

    @SerializedName("rtnMsg")
    private String rtnMsg;

    @SerializedName("errCode")
    private String errCode;

    @SerializedName("errMsg")
    private String errMsg;

    @SerializedName("cardNos")
    private List<CardDataDto> cardNos;

    public String getRtnCode() {
        return rtnCode;
    }

    public String getRtnMsg() {
        return rtnMsg;
    }

    public String getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public List<CardDataDto> getCardNos() {
        return cardNos;
    }

    public static class CardDataDto {
        @SerializedName("cardNo")
        private String cardNo;

        @SerializedName("LastTranSum")
        private double lastTranSum;

        @SerializedName("LastTranDate")
        private String lastTranDate;

        @SerializedName("cardFaceID")
        private String cardFaceId;

        @SerializedName("cardImageUrl")
        private String cardImageUrl;

        @SerializedName("isRegister")
        private boolean isRegister;

        @SerializedName("errCode")
        private String errCode;

        @SerializedName("errMsg")
        private String errMsg;

        public String getCardNo() {
            return cardNo != null ? cardNo : "";
        }

        public double getLastTranSum() {
            return lastTranSum;
        }

        public String getLastTranDate() {
            return lastTranDate;
        }

        public String getCardFaceId() {
            return cardFaceId;
        }

        public String getCardImageUrl() {
            return cardImageUrl;
        }

        public boolean isRegister() {
            return isRegister;
        }

        public String getErrCode() {
            return errCode;
        }

        public String getErrMsg() {
            return errMsg;
        }
    }
}
