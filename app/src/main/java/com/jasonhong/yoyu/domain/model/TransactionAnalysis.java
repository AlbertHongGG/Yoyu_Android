package com.jasonhong.yoyu.domain.model;

import java.io.Serializable;

public class TransactionAnalysis implements Serializable {

    private final String scopeName;
    private final int ptCnt;
    private final int ptAmt;
    private final int mtCnt;
    private final int mtAmt;
    private final int aptCnt;
    private final int aptAmt;

    public TransactionAnalysis(
            String scopeName,
            int ptCnt,
            int ptAmt,
            int mtCnt,
            int mtAmt,
            int aptCnt,
            int aptAmt
    ) {
        this.scopeName = scopeName != null ? scopeName : "";
        this.ptCnt = ptCnt;
        this.ptAmt = ptAmt;
        this.mtCnt = mtCnt;
        this.mtAmt = mtAmt;
        this.aptCnt = aptCnt;
        this.aptAmt = aptAmt;
    }

    public String getScopeName() {
        return scopeName;
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
