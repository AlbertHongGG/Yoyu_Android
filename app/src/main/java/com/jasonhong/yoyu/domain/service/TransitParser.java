package com.jasonhong.yoyu.domain.service;

import com.jasonhong.yoyu.data.model.remote.GetInquireDetailResponse.RawTransactionDto;
import com.jasonhong.yoyu.domain.model.TransitTransaction;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransitParser implements TransactionParser {

    @Override
    public List<YoyuTransaction> parse(List<RawTransactionDto> rawTransactions) {
        List<YoyuTransaction> result = new ArrayList<>();
        if (rawTransactions == null || rawTransactions.isEmpty()) {
            return result;
        }

        List<RawTransactionDto> processList = new ArrayList<>(rawTransactions);
        // Sort descending by date (newest first)
        processList.sort((a, b) -> Long.compare(b.getTransactionDate(), a.getTransactionDate()));

        Set<String> processedTraceNos = new HashSet<>();

        for (int i = 0; i < processList.size(); i++) {
            RawTransactionDto current = processList.get(i);
            if (processedTraceNos.contains(current.getTraceNo())) {
                continue;
            }

            String xtype = current.getXtype();
            if ("出站".equals(xtype) || "段次下車".equals(xtype)) {
                RawTransactionDto inRecord = null;
                // Search backwards in time (forward in the descending array)
                for (int j = i + 1; j < processList.size(); j++) {
                    RawTransactionDto potentialIn = processList.get(j);
                    if (!processedTraceNos.contains(potentialIn.getTraceNo())) {
                        String inXtype = potentialIn.getXtype();
                        if ("進站".equals(inXtype) || "段次上車".equals(inXtype)) {
                            inRecord = potentialIn;
                            processedTraceNos.add(potentialIn.getTraceNo());
                            break;
                        }
                    }
                }

                processedTraceNos.add(current.getTraceNo());

                int outAmount = parseAmount(current.getAmt());
                int inAmount = inRecord != null ? parseAmount(inRecord.getAmt()) : 0;
                int totalAmount = outAmount + inAmount;

                Date outTime = new Date(current.getTransactionDate() * 1000L);
                Date inTime = inRecord != null ? new Date(inRecord.getTransactionDate() * 1000L) : outTime;

                result.add(new TransitTransaction(
                        current.getTraceNo(),
                        current.getPartnerName(),
                        totalAmount,
                        current.getElectronicValue(),
                        inTime,
                        outTime,
                        inRecord != null ? inRecord.getLocationName() : "",
                        current.getLocationName()
                ));
            }
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
