package com.jasonhong.yoyu.core.network;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class IPassHeadersInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
                .header("User-Agent", "ktor-client")
                .header("Accept", "application/json")
                .header("Accept-Encoding", "gzip")
                .header("Connection", "Keep-Alive")
                .header("Device-OS", "Android")
                .header("OS-Version", "28")
                .header("App-Version", "1.36.0")
                .header("Accept-Language", "zh-TW")
                .header("Content-Type", "application/json")
                .build();
        return chain.proceed(request);
    }
}
