package com.jasonhong.yoyu.domain.service;

import com.jasonhong.yoyu.data.model.remote.GetInquireDetailResponse.RawTransactionDto;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;
import java.util.List;

public interface TransactionParser {
    List<YoyuTransaction> parse(List<RawTransactionDto> rawTransactions);
}
