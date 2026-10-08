package com.jasonhong.yoyu.domain.service;

import com.jasonhong.yoyu.data.model.remote.GetInquireDetailResponse.RawTransactionDto;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionAggregator {

    private final TransactionParser transitParser;
    private final TransactionParser retailParser;
    private final TransactionClassifier classifier;

    public TransactionAggregator() {
        this(new TransitParser(), new RetailParser(), new TransactionClassifier());
    }

    public TransactionAggregator(TransactionParser transitParser, TransactionParser retailParser) {
        this(transitParser, retailParser, new TransactionClassifier());
    }

    public TransactionAggregator(TransactionParser transitParser, TransactionParser retailParser, TransactionClassifier classifier) {
        this.transitParser = transitParser;
        this.retailParser = retailParser;
        this.classifier = classifier;
    }

    public List<YoyuTransaction> aggregate(List<RawTransactionDto> rawTransactions) {
        if (rawTransactions == null || rawTransactions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<RawTransactionDto>> grouped = new HashMap<>();

        // Group by DataSource
        for (RawTransactionDto raw : rawTransactions) {
            String ds = raw.getDataSource();
            if (!grouped.containsKey(ds)) {
                grouped.put(ds, new ArrayList<>());
            }
            grouped.get(ds).add(raw);
        }

        List<YoyuTransaction> allTransactions = new ArrayList<>();

        // Parse each group
        for (Map.Entry<String, List<RawTransactionDto>> entry : grouped.entrySet()) {
            String dataSource = entry.getKey();
            List<RawTransactionDto> list = entry.getValue();

            if (classifier.isTransitGroup(dataSource, list)) {
                allTransactions.addAll(transitParser.parse(list));
            } else {
                allTransactions.addAll(retailParser.parse(list));
            }
        }

        // Sort all by time descending (newest first)
        allTransactions.sort((a, b) -> b.getPrimaryTime().compareTo(a.getPrimaryTime()));

        return allTransactions;
    }
}
