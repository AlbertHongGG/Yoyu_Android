package com.jasonhong.yoyu.domain.service;

import com.jasonhong.yoyu.domain.model.CategoryUIModel;
import com.jasonhong.yoyu.domain.model.TransactionAnalysis;
import java.util.List;

public interface AnalysisCalculator {

    int calculateTotal(List<TransactionAnalysis> data);

    List<CategoryUIModel> extractCategories(List<TransactionAnalysis> data);
}
