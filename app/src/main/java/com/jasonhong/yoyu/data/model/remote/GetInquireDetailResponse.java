package com.jasonhong.yoyu.data.model.remote;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class GetInquireDetailResponse {
    @SerializedName("rtnCode")
    private String rtnCode;

    @SerializedName("rtnMsg")
    private String rtnMsg;

    @SerializedName("TranDetails")
    private List<RawTransactionDto> tranDetails;

    public String getRtnCode() {
        return rtnCode;
    }

    public String getRtnMsg() {
        return rtnMsg;
    }

    public List<RawTransactionDto> getTranDetails() {
        return tranDetails != null ? tranDetails : new ArrayList<>();
    }

    public static class RawTransactionDto {
        @SerializedName("TraceNo")
        private String traceNo;

        @SerializedName("TransactionDate")
        private long transactionDate; // seconds timestamp

        @SerializedName("xtype")
        private String xtype;

        @SerializedName("PartnerName")
        private String partnerName;

        @SerializedName("ElectronicValue")
        private double electronicValue;

        @SerializedName("LocationName")
        private String locationName;

        @SerializedName("DataSource")
        private String dataSource;

        @SerializedName("AMT")
        private String amt;

        @SerializedName("InquireIcon")
        private String inquireIcon;

        public String getTraceNo() {
            return traceNo != null ? traceNo : "";
        }

        public long getTransactionDate() {
            return transactionDate;
        }

        public String getXtype() {
            return xtype != null ? xtype : "";
        }

        public String getPartnerName() {
            return partnerName != null ? partnerName : "";
        }

        public double getElectronicValue() {
            return electronicValue;
        }

        public String getLocationName() {
            return locationName != null ? locationName : "";
        }

        public String getDataSource() {
            return dataSource != null ? dataSource : "";
        }

        public String getAmt() {
            return amt != null ? amt : "0";
        }

        public String getInquireIcon() {
            return inquireIcon != null ? inquireIcon : "";
        }
    }
}
