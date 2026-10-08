package com.jasonhong.yoyu.domain.service;

import com.jasonhong.yoyu.domain.model.CategoryUIModel;
import com.jasonhong.yoyu.domain.model.TransactionAnalysis;

import java.util.ArrayList;
import java.util.List;

public class AutoTopUpCalculator implements AnalysisCalculator {

    @Override
    public int calculateTotal(List<TransactionAnalysis> data) {
        if (data == null) return 0;
        int sum = 0;
        for (TransactionAnalysis item : data) {
            sum += item.getAptAmt();
        }
        return sum;
    }

    @Override
    public List<CategoryUIModel> extractCategories(List<TransactionAnalysis> data) {
        List<CategoryUIModel> result = new ArrayList<>();
        if (data == null) return result;

        for (TransactionAnalysis item : data) {
            if (item.getAptAmt() > 0) {
                result.add(new CategoryUIModel(
                        item.getScopeName(),
                        item.getAptAmt(),
                        item.getAptCnt(),
                        ScopeIconMapper.getIconForScope(item.getScopeName())
                ));
            }
        }

        result.sort((a, b) -> Integer.compare(b.getAmount(), a.getAmount()));
        return result;
    }
}
