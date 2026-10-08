package com.jasonhong.yoyu.presentation.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jasonhong.yoyu.core.base.BaseViewModel;
import com.jasonhong.yoyu.core.base.Resource;
import com.jasonhong.yoyu.data.repository.DetailRepositoryImpl;
import com.jasonhong.yoyu.domain.model.AnalysisDataType;
import com.jasonhong.yoyu.domain.model.CategoryUIModel;
import com.jasonhong.yoyu.domain.model.RetailTransaction;
import com.jasonhong.yoyu.domain.model.TransactionAnalysis;
import com.jasonhong.yoyu.domain.model.TransitTransaction;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;
import com.jasonhong.yoyu.domain.repository.DetailRepository;
import com.jasonhong.yoyu.domain.service.AutoTopUpCalculator;
import com.jasonhong.yoyu.domain.service.ExpenseCalculator;
import com.jasonhong.yoyu.domain.service.TopUpCalculator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DetailViewModel extends BaseViewModel {

    private final DetailRepository detailRepository;
    private final ExpenseCalculator expenseCalculator = new ExpenseCalculator();
    private final TopUpCalculator topUpCalculator = new TopUpCalculator();
    private final AutoTopUpCalculator autoTopUpCalculator = new AutoTopUpCalculator();

    private String cardNo;

    private final MutableLiveData<Date> startDate = new MutableLiveData<>();
    private final MutableLiveData<Date> endDate = new MutableLiveData<>();
    private final MutableLiveData<String> partnerFilter = new MutableLiveData<>(null);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    private final MutableLiveData<Resource<List<YoyuTransaction>>> rawTransactionsResource = new MutableLiveData<>();
    private final MutableLiveData<List<YoyuTransaction>> filteredTransactions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> availablePartners = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Resource<List<TransactionAnalysis>>> analysisResource = new MutableLiveData<>();
    private final MutableLiveData<AnalysisDataType> currentAnalysisType = new MutableLiveData<>(AnalysisDataType.EXPENSE);
    private final MutableLiveData<Integer> totalExpense = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalTopUp = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalAutoTopUp = new MutableLiveData<>(0);
    private final MutableLiveData<List<CategoryUIModel>> currentCategories = new MutableLiveData<>(new ArrayList<>());

    public DetailViewModel() {
        this(new DetailRepositoryImpl());
    }

    public DetailViewModel(DetailRepository detailRepository) {
        this.detailRepository = detailRepository;
    }

    public void init(String cardNo) {
        if (this.cardNo != null && this.cardNo.equals(cardNo)) {
            return;
        }
        this.cardNo = cardNo;

        // Default: 3 months ago to today
        Calendar calendar = Calendar.getInstance();
        Date eDate = calendar.getTime();
        calendar.add(Calendar.MONTH, -3);
        Date sDate = calendar.getTime();

        this.startDate.setValue(sDate);
        this.endDate.setValue(eDate);

        loadData();
    }

    public void loadData() {
        if (cardNo == null || startDate.getValue() == null || endDate.getValue() == null) {
            return;
        }

        rawTransactionsResource.setValue(Resource.loading());
        analysisResource.setValue(Resource.loading());

        Date sDate = startDate.getValue();
        Date eDate = endDate.getValue();

        // Load transactions
        detailRepository.getTransactions(cardNo, sDate, eDate)
                .thenAccept(transactions -> {
                    postTransactionsResult(Resource.success(transactions));
                })
                .exceptionally(throwable -> {
                    String msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
                    postTransactionsResult(Resource.error(msg, null));
                    return null;
                });

        // Load analysis
        detailRepository.getAnalysis(cardNo, sDate, eDate)
                .thenAccept(analyses -> {
                    postAnalysisResult(Resource.success(analyses));
                })
                .exceptionally(throwable -> {
                    String msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
                    postAnalysisResult(Resource.error(msg, null));
                    return null;
                });
    }

    private void postTransactionsResult(Resource<List<YoyuTransaction>> res) {
        runOnMainThread(() -> {
            rawTransactionsResource.setValue(res);
            if (res.getStatus() == Resource.Status.SUCCESS && res.getData() != null) {
                // Extract distinct partner names
                Set<String> partnersSet = new HashSet<>();
                for (YoyuTransaction tx : res.getData()) {
                    if (tx instanceof TransitTransaction) {
                        partnersSet.add(((TransitTransaction) tx).getPartnerName());
                    } else if (tx instanceof RetailTransaction) {
                        partnersSet.add(((RetailTransaction) tx).getPartnerName());
                    }
                }
                availablePartners.setValue(new ArrayList<>(partnersSet));
            } else {
                availablePartners.setValue(new ArrayList<>());
            }
            applyLocalFilter();
        });
    }

    private void postAnalysisResult(Resource<List<TransactionAnalysis>> res) {
        runOnMainThread(() -> {
            analysisResource.setValue(res);
            if (res.getStatus() == Resource.Status.SUCCESS && res.getData() != null) {
                List<TransactionAnalysis> data = res.getData();
                totalExpense.setValue(expenseCalculator.calculateTotal(data));
                totalTopUp.setValue(topUpCalculator.calculateTotal(data));
                totalAutoTopUp.setValue(autoTopUpCalculator.calculateTotal(data));
            } else {
                totalExpense.setValue(0);
                totalTopUp.setValue(0);
                totalAutoTopUp.setValue(0);
            }
            updateCategoryList();
        });
    }

    public void setDateRange(Date sDate, Date eDate) {
        this.startDate.setValue(sDate);
        this.endDate.setValue(eDate);
        loadData();
    }

    public void setPartnerFilter(String partner) {
        this.partnerFilter.setValue(partner);
        applyLocalFilter();
    }

    public void setSearchQuery(String query) {
        this.searchQuery.setValue(query != null ? query : "");
        applyLocalFilter();
    }

    public void setAnalysisType(AnalysisDataType type) {
        this.currentAnalysisType.setValue(type);
        updateCategoryList();
    }

    private void applyLocalFilter() {
        Resource<List<YoyuTransaction>> res = rawTransactionsResource.getValue();
        if (res == null || res.getData() == null) {
            filteredTransactions.setValue(new ArrayList<>());
            return;
        }

        List<YoyuTransaction> raw = res.getData();
        String partner = partnerFilter.getValue();
        String query = searchQuery.getValue() != null ? searchQuery.getValue().trim().toLowerCase() : "";

        List<YoyuTransaction> result = new ArrayList<>();
        for (YoyuTransaction tx : raw) {
            // Filter by partner
            if (partner != null && !partner.isEmpty()) {
                String txPartner = tx instanceof TransitTransaction
                        ? ((TransitTransaction) tx).getPartnerName()
                        : ((RetailTransaction) tx).getPartnerName();
                if (!partner.equals(txPartner)) {
                    continue;
                }
            }

            // Filter by search query
            if (!query.isEmpty()) {
                boolean match = false;
                if (tx instanceof TransitTransaction) {
                    TransitTransaction t = (TransitTransaction) tx;
                    match = t.getPartnerName().toLowerCase().contains(query)
                            || t.getInLocation().toLowerCase().contains(query)
                            || t.getOutLocation().toLowerCase().contains(query);
                } else if (tx instanceof RetailTransaction) {
                    RetailTransaction r = (RetailTransaction) tx;
                    match = r.getPartnerName().toLowerCase().contains(query)
                            || r.getLocation().toLowerCase().contains(query)
                            || r.getDescription().toLowerCase().contains(query);
                }
                if (!match) {
                    continue;
                }
            }

            result.add(tx);
        }

        filteredTransactions.setValue(result);
    }

    private void updateCategoryList() {
        Resource<List<TransactionAnalysis>> res = analysisResource.getValue();
        if (res == null || res.getData() == null) {
            currentCategories.setValue(new ArrayList<>());
            return;
        }

        List<TransactionAnalysis> data = res.getData();
        AnalysisDataType type = currentAnalysisType.getValue();
        if (type == null) type = AnalysisDataType.EXPENSE;

        List<CategoryUIModel> categories;
        switch (type) {
            case TOP_UP:
                categories = topUpCalculator.extractCategories(data);
                break;
            case AUTO_TOP_UP:
                categories = autoTopUpCalculator.extractCategories(data);
                break;
            case EXPENSE:
            default:
                categories = expenseCalculator.extractCategories(data);
                break;
        }

        currentCategories.setValue(categories);
    }

    // Getters for LiveData
    public LiveData<Date> getStartDate() { return startDate; }
    public LiveData<Date> getEndDate() { return endDate; }
    public LiveData<String> getPartnerFilter() { return partnerFilter; }
    public LiveData<String> getSearchQuery() { return searchQuery; }
    public LiveData<Resource<List<YoyuTransaction>>> getRawTransactionsResource() { return rawTransactionsResource; }
    public LiveData<List<YoyuTransaction>> getFilteredTransactions() { return filteredTransactions; }
    public LiveData<List<String>> getAvailablePartners() { return availablePartners; }
    public LiveData<Resource<List<TransactionAnalysis>>> getAnalysisResource() { return analysisResource; }
    public LiveData<AnalysisDataType> getCurrentAnalysisType() { return currentAnalysisType; }
    public LiveData<Integer> getTotalExpense() { return totalExpense; }
    public LiveData<Integer> getTotalTopUp() { return totalTopUp; }
    public LiveData<Integer> getTotalAutoTopUp() { return totalAutoTopUp; }
    public LiveData<List<CategoryUIModel>> getCurrentCategories() { return currentCategories; }
}
