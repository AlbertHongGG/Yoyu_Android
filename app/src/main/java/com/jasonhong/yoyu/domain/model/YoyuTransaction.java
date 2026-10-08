package com.jasonhong.yoyu.domain.model;

import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.Date;

public abstract class YoyuTransaction implements Serializable {

    private final String traceNo;
    private final String partnerName;
    private final int amount;
    private final double balance;

    public YoyuTransaction(
            @NonNull String traceNo,
            @NonNull String partnerName,
            int amount,
            double balance
    ) {
        this.traceNo = traceNo;
        this.partnerName = partnerName;
        this.amount = amount;
        this.balance = balance;
    }

    @NonNull
    public String getTraceNo() {
        return traceNo;
    }

    @NonNull
    public String getPartnerName() {
        return partnerName;
    }

    public int getAmount() {
        return amount;
    }

    public double getBalance() {
        return balance;
    }

    @NonNull
    public abstract Date getPrimaryTime();

    public boolean isTransit() {
        return this instanceof TransitTransaction;
    }

    public boolean isRetail() {
        return this instanceof RetailTransaction;
    }
}
