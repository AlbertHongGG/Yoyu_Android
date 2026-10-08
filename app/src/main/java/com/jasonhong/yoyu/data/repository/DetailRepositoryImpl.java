package com.jasonhong.yoyu.data.repository;

import com.jasonhong.yoyu.core.network.ApiException;
import com.jasonhong.yoyu.core.network.RetrofitClient;
import com.jasonhong.yoyu.data.api.IPassApiService;
import com.jasonhong.yoyu.data.model.remote.GetInquireCatalogRequest;
import com.jasonhong.yoyu.data.model.remote.GetInquireCatalogResponse;
import com.jasonhong.yoyu.data.model.remote.GetInquireDetailRequest;
import com.jasonhong.yoyu.data.model.remote.GetInquireDetailResponse;
import com.jasonhong.yoyu.domain.model.TransactionAnalysis;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;
import com.jasonhong.yoyu.domain.repository.DetailRepository;
import com.jasonhong.yoyu.domain.service.TransactionAggregator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class DetailRepositoryImpl implements DetailRepository {

    private final IPassApiService apiService;
    private final TransactionAggregator aggregator;
    private final ExecutorService executor;

    public DetailRepositoryImpl() {
        this.apiService = RetrofitClient.getApiService();
        this.aggregator = new TransactionAggregator();
        this.executor = Executors.newFixedThreadPool(4);
    }

    public DetailRepositoryImpl(IPassApiService apiService, TransactionAggregator aggregator, ExecutorService executor) {
        this.apiService = apiService;
        this.aggregator = aggregator;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<List<YoyuTransaction>> getTransactions(String cardNo, Date sDate, Date eDate) {
        return CompletableFuture.supplyAsync(() -> {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String sDateStr = df.format(sDate);
            String eDateStr = df.format(eDate);

            try {
                Response<GetInquireDetailResponse> response = apiService.getInquireDetail(
                        new GetInquireDetailRequest(sDateStr, eDateStr, cardNo)
                ).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    throw new ApiException("伺服器回應錯誤 (狀態碼: " + response.code() + ")");
                }

                GetInquireDetailResponse body = response.body();
                if (!"0".equals(body.getRtnCode())) {
                    String msg = body.getRtnMsg() != null && !body.getRtnMsg().isEmpty() ? body.getRtnMsg() : "取得明細失敗";
                    throw new ApiException(msg);
                }

                return aggregator.aggregate(body.getTranDetails());
            } catch (IOException e) {
                throw new RuntimeException(new ApiException("連線逾時或網路錯誤，請檢查網路狀態"));
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<TransactionAnalysis>> getAnalysis(String cardNo, Date sDate, Date eDate) {
        return CompletableFuture.supplyAsync(() -> {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String sDateStr = df.format(sDate);
            String eDateStr = df.format(eDate);

            try {
                Response<GetInquireCatalogResponse> response = apiService.getInquireCatalog(
                        new GetInquireCatalogRequest(sDateStr, eDateStr, cardNo)
                ).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    throw new ApiException("伺服器回應錯誤 (狀態碼: " + response.code() + ")");
                }

                GetInquireCatalogResponse body = response.body();
                if (!"0".equals(body.getRtnCode())) {
                    String msg = body.getRtnMsg() != null && !body.getRtnMsg().isEmpty() ? body.getRtnMsg() : "取得分析失敗";
                    throw new ApiException(msg);
                }

                List<TransactionAnalysis> result = new ArrayList<>();
                for (GetInquireCatalogResponse.CatalogDto dto : body.getTranCatalogs()) {
                    result.add(new TransactionAnalysis(
                            dto.getScopeName(),
                            dto.getPtCnt(),
                            dto.getPtAmt(),
                            dto.getMtCnt(),
                            dto.getMtAmt(),
                            dto.getAptCnt(),
                            dto.getAptAmt()
                    ));
                }
                return result;
            } catch (IOException e) {
                throw new RuntimeException(new ApiException("連線逾時或網路錯誤，請檢查網路狀態"));
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
