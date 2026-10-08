package com.jasonhong.yoyu.data.api;

import com.jasonhong.yoyu.data.model.remote.CheckMyCardsRequest;
import com.jasonhong.yoyu.data.model.remote.CheckMyCardsResponse;
import com.jasonhong.yoyu.data.model.remote.GetInquireCatalogRequest;
import com.jasonhong.yoyu.data.model.remote.GetInquireCatalogResponse;
import com.jasonhong.yoyu.data.model.remote.GetInquireDetailRequest;
import com.jasonhong.yoyu.data.model.remote.GetInquireDetailResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IPassApiService {

    @POST("CheckMyCards")
    Call<CheckMyCardsResponse> checkMyCards(@Body CheckMyCardsRequest request);

    @POST("GetInquireDetail")
    Call<GetInquireDetailResponse> getInquireDetail(@Body GetInquireDetailRequest request);

    @POST("GetInquireCatalog")
    Call<GetInquireCatalogResponse> getInquireCatalog(@Body GetInquireCatalogRequest request);
}
