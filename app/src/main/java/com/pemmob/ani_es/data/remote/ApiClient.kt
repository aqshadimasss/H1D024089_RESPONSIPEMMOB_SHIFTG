package com.pemmob.ani_es.data.remote

import com.pemmob.ani_es.util.AppConstants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton factory penyedia instance Retrofit untuk Tenrai (primer) dan Jikan (fallback).
 * Dibuat secara lazy agar hanya diinisialisasi saat pertama kali dibutuhkan.
 */
object ApiClient {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Ani-es/1.0 (Android Mobile Assignment)")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private fun buildService(baseUrl: String): AnimeApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnimeApiService::class.java)
    }

    /**
     * Instance Retrofit utama (Tenrai API v1).
     */
    val tenrai: AnimeApiService by lazy {
        buildService(AppConstants.BASE_URL_TENRAI)
    }

    /**
     * Instance Retrofit cadangan (Jikan API v4).
     */
    val jikan: AnimeApiService by lazy {
        buildService(AppConstants.BASE_URL_JIKAN)
    }
}
