package com.jasonhong.yoyu.domain.repository;

import com.jasonhong.yoyu.domain.model.TransactionAnalysis;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DetailRepository {

    CompletableFuture<List<YoyuTransaction>> getTransactions(String cardNo, Date sDate, Date eDate);

    CompletableFuture<List<TransactionAnalysis>> getAnalysis(String cardNo, Date sDate, Date eDate);
}
