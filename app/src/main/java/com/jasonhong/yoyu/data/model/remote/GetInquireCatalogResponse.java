package com.jasonhong.yoyu.data.model.remote;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class GetInquireCatalogResponse {
    @SerializedName("rtnCode")
    private String rtnCode;

    @SerializedName("rtnMsg")
    private String rtnMsg;

    @SerializedName("TranCatalogs")
    private List<CatalogDto> tranCatalogs;

    public String getRtnCode() {
        return rtnCode;
    }

    public String getRtnMsg() {
        return rtnMsg;
    }

    public List<CatalogDto> getTranCatalogs() {
        return tranCatalogs != null ? tranCatalogs : new ArrayList<>();
    }

    public static class CatalogDto {
        @SerializedName("SCOPENAME")
        private String scopeName;

        @SerializedName("PTCNT")
        private int ptCnt;

        @SerializedName("PTAMT")
        private int ptAmt;

        @SerializedName("MTCNT")
        private int mtCnt;

        @SerializedName("MTAMT")
        private int mtAmt;

        @SerializedName("APTCNT")
        private int aptCnt;

        @SerializedName("APTAMT")
        private int aptAmt;

        public String getScopeName() {
            return scopeName != null ? scopeName : "";
        }

        public int getPtCnt() {
            return ptCnt;
        }

        public int getPtAmt() {
            return ptAmt;
        }

        public int getMtCnt() {
            return mtCnt;
        }

        public int getMtAmt() {
            return mtAmt;
        }

        public int getAptCnt() {
            return aptCnt;
        }

        public int getAptAmt() {
            return aptAmt;
        }
    }
}
