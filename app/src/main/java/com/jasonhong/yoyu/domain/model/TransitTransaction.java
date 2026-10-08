package com.jasonhong.yoyu.domain.model;

import androidx.annotation.NonNull;
import java.util.Date;

public class TransitTransaction extends YoyuTransaction {

    private final Date inTime;
    private final Date outTime;
    private final String inLocation;
    private final String outLocation;

    public TransitTransaction(
            @NonNull String traceNo,
            @NonNull String partnerName,
            int amount,
            double balance,
            @NonNull Date inTime,
            @NonNull Date outTime,
            @NonNull String inLocation,
            @NonNull String outLocation
    ) {
        super(traceNo, partnerName, amount, balance);
        this.inTime = inTime;
        this.outTime = outTime;
        this.inLocation = inLocation;
        this.outLocation = outLocation;
    }

    @NonNull
    public Date getInTime() {
        return inTime;
    }

    @NonNull
    public Date getOutTime() {
        return outTime;
    }

    @NonNull
    public String getInLocation() {
        return inLocation;
    }

    @NonNull
    public String getOutLocation() {
        return outLocation;
    }

    @NonNull
    @Override
    public Date getPrimaryTime() {
        return outTime;
    }
}
