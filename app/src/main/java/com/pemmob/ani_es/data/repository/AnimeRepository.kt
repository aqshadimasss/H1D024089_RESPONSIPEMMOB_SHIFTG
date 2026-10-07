package com.pemmob.ani_es.data.repository

import com.pemmob.ani_es.data.model.Anime
import com.pemmob.ani_es.data.model.Genre
import com.pemmob.ani_es.data.remote.ApiClient
import com.pemmob.ani_es.data.remote.AnimeApiService
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository data anime dengan penanganan fallback otomatis:
 * Tenrai API (primer) -> Jikan API (cadangan bila offline / HTTP 5xx).
 * Error dipetakan ke pesan ramah berbahasa Indonesia sesuai PRD.
 */
class AnimeRepository(
    private val primary: AnimeApiService = ApiClient.tenrai,
    private val fallback: AnimeApiService = ApiClient.jikan
) {

    /**
     * Menjalankan pemanggilan API dengan fallback Tenrai -> Jikan jika terjadi IOException atau server 5xx.
     */
    private suspend fun <T> executeWithFallback(
        apiCall: suspend (AnimeApiService) -> T
    ): Result<T> {
        return try {
            val result = apiCall(primary)
            Result.success(result)
        } catch (e: Exception) {
            val shouldFallback = e is IOException || (e is HttpException && e.code() in 500..599)
            if (shouldFallback) {
                try {
                    val fallbackResult = apiCall(fallback)
                    Result.success(fallbackResult)
                } catch (fallbackError: Exception) {
                    Result.failure(mapException(fallbackError))
                }
            } else {
                Result.failure(mapException(e))
            }
        }
    }

    /**
     * Memetakan Throwable teknis ke Exception dengan pesan berbahasa Indonesia yang ramah pengguna.
     */
    private fun mapException(throwable: Throwable): Exception {
        val friendlyMessage = when (throwable) {
            is IOException -> "Tidak ada koneksi internet. Periksa jaringanmu."
            is HttpException -> when (throwable.code()) {
                404 -> "Data anime tidak ditemukan."
                429 -> "Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi."
                in 500..599 -> "Server sedang bermasalah. Coba lagi nanti."
                else -> "Terjadi kesalahan: HTTP ${throwable.code()}"
            }
            else -> "Terjadi kesalahan: ${throwable.localizedMessage ?: "tidak diketahui"}"
        }
        return Exception(friendlyMessage, throwable)
    }

    /**
     * Mencari anime berdasarkan query teks dan/atau genre.
     * Jika query kosong dan tanpa genre, tampilkan anime terpopuler berdasarkan jumlah members (FR-7).
     */
    suspend fun searchAnime(
        query: String? = null,
        genreId: Int? = null,
        page: Int = 1
    ): Result<List<Anime>> {
        val cleanQuery = query?.trim()?.ifBlank { null }
        val genreParam = genreId?.toString()

        return executeWithFallback { service ->
            val response = if (cleanQuery == null && genreParam == null) {
                service.getAnimeList(
                    query = null,
                    genres = null,
                    page = page,
                    sfw = true,
                    orderBy = "members",
                    sort = "desc"
                )
            } else if (cleanQuery == null) {
                service.getAnimeList(
                    query = null,
                    genres = genreParam,
                    page = page,
                    sfw = true,
                    orderBy = "members",
                    sort = "desc"
                )
            } else {
                service.getAnimeList(
                    query = cleanQuery,
                    genres = genreParam,
                    page = page,
                    sfw = true
                )
            }
            response.data
        }
    }

    /**
     * Mengambil data detail lengkap sebuah anime berdasarkan mal_id.
     */
    suspend fun getAnimeDetail(malId: Int): Result<Anime> {
        return executeWithFallback { service ->
            val response = service.getAnimeDetail(id = malId)
            response.data
        }
    }

    /**
     * Mengambil daftar genre utama anime.
     */
    suspend fun getGenres(): Result<List<Genre>> {
        return executeWithFallback { service ->
            val response = service.getGenres(filter = "genres")
            response.data
        }
    }
}
