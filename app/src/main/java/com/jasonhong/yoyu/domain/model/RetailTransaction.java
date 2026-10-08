package com.jasonhong.yoyu.domain.model;

import androidx.annotation.NonNull;
import java.util.Date;

public class RetailTransaction extends YoyuTransaction {

    private final Date time;
    private final String location;
    private final String description;

    public RetailTransaction(
            @NonNull String traceNo,
            @NonNull String partnerName,
            int amount,
            double balance,
            @NonNull Date time,
            @NonNull String location,
            @NonNull String description
    ) {
        super(traceNo, partnerName, amount, balance);
        this.time = time;
        this.location = location;
        this.description = description;
    }

    @NonNull
    public Date getTime() {
        return time;
    }

    @NonNull
    public String getLocation() {
        return location;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    @Override
    public Date getPrimaryTime() {
        return time;
    }
}
