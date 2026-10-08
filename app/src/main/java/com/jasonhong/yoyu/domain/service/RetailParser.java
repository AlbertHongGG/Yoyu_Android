package com.jasonhong.yoyu.domain.service;

import com.jasonhong.yoyu.data.model.remote.GetInquireDetailResponse.RawTransactionDto;
import com.jasonhong.yoyu.domain.model.RetailTransaction;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RetailParser implements TransactionParser {

    @Override
    public List<YoyuTransaction> parse(List<RawTransactionDto> rawTransactions) {
        List<YoyuTransaction> result = new ArrayList<>();
        if (rawTransactions == null || rawTransactions.isEmpty()) {
            return result;
        }

        for (RawTransactionDto raw : rawTransactions) {
            int amount = parseAmount(raw.getAmt());
            Date time = new Date(raw.getTransactionDate() * 1000L);

            result.add(new RetailTransaction(
                    raw.getTraceNo(),
                    raw.getPartnerName(),
                    amount,
                    raw.getElectronicValue(),
                    time,
                    raw.getLocationName(),
                    raw.getXtype()
            ));
        }

        return result;
    }

    private int parseAmount(String amtStr) {
        if (amtStr == null) return 0;
        String clean = amtStr.trim();
        if (clean.isEmpty()) return 0;
        try {
            return Integer.parseInt(clean);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
