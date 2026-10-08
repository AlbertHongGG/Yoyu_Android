package com.jasonhong.yoyu.data.model.remote;

import com.google.gson.annotations.SerializedName;

public class GetInquireDetailRequest {
    @SerializedName("sDate")
    private final String sDate;

    @SerializedName("eDate")
    private final String eDate;

    @SerializedName("cardNo")
    private final String cardNo;

    public GetInquireDetailRequest(String sDate, String eDate, String cardNo) {
        this.sDate = sDate;
        this.eDate = eDate;
        this.cardNo = cardNo;
    }

    public String getsDate() {
        return sDate;
    }

    public String geteDate() {
        return eDate;
    }

    public String getCardNo() {
        return cardNo;
    }
}
