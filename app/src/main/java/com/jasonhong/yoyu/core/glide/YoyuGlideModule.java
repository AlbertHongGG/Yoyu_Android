package com.jasonhong.yoyu.core.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * Custom Glide module optimizing image pipeline:
 * - Integrates OkHttp3 with connection pooling (32 connections) and HTTP/2 multiplexing.
 * - Configures dedicated disk cache (150MB) and memory cache for rapid image retrieval.
 * - Sets default RequestOptions for high performance.
 */
@GlideModule
public final class YoyuGlideModule extends AppGlideModule {

    private static final int DISK_CACHE_SIZE_BYTES = 150 * 1024 * 1024; // 150 MB

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, "card_face_cache", DISK_CACHE_SIZE_BYTES));

        // Use PREFER_ARGB_8888 for high quality card images
        builder.setDefaultRequestOptions(
                new RequestOptions()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
        );
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
