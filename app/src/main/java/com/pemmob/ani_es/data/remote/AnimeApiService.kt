package com.pemmob.ani_es.data.remote

import com.pemmob.ani_es.data.model.AnimeDetailResponse
import com.pemmob.ani_es.data.model.AnimeListResponse
import com.pemmob.ani_es.data.model.GenreListResponse
import com.pemmob.ani_es.util.AppConstants
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit untuk komunikasi dengan REST API Tenrai dan Jikan.
 * Seluruh path ditulis tanpa awalan '/' agar menyatu dengan base URL secara presisi.
 */
interface AnimeApiService {

    /**
     * Mengambil daftar anime berdasarkan kata kunci pencarian, filter genre, atau urutan popularitas.
     */
    @GET("anime")
    suspend fun getAnimeList(
        @Query("q") query: String? = null,
        @Query("genres") genres: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = AppConstants.PAGE_SIZE,
        @Query("sfw") sfw: Boolean = true,
        @Query("order_by") orderBy: String? = null,
        @Query("sort") sort: String? = null
    ): AnimeListResponse

    /**
     * Mengambil detail lengkap anime berdasarkan ID MyAnimeList (mal_id).
     */
    @GET("anime/{id}")
    suspend fun getAnimeDetail(
        @Path("id") id: Int
    ): AnimeDetailResponse

    /**
     * Mengambil daftar genre anime utama (filter=genres membatasi ke genre utama saja).
     */
    @GET("genres/anime")
    suspend fun getGenres(
        @Query("filter") filter: String = "genres"
    ): GenreListResponse
}
