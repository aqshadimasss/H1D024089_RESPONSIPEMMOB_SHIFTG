package com.pemmob.ani_es.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model data utama untuk anime.
 * Seluruh field nullable ditangani dengan aman agar aplikasi tidak crash jika ada nilai null dari API.
 */
data class Anime(
    @SerializedName("mal_id")
    val malId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("title_english")
    val titleEnglish: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("episodes")
    val episodes: Int? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("score")
    val score: Double? = null,

    @SerializedName("synopsis")
    val synopsis: String? = null,

    @SerializedName("year")
    val year: Int? = null,

    @SerializedName("images")
    val images: AnimeImages? = null,

    @SerializedName("genres")
    val genres: List<Genre> = emptyList()
) {
    /**
     * URL poster utama (mengutamakan ukuran besar, fallback ke ukuran standar).
     */
    val posterUrl: String?
        get() = images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl

    /**
     * Tampilan skor: N/A jika null atau <= 0, jika ada format nilai 1 desimal.
     */
    val displayScore: String
        get() = if (score != null && score > 0.0) {
            String.format(java.util.Locale.US, "%.2f", score)
        } else {
            "N/A"
        }

    /**
     * Tampilan jumlah episode: "?" jika null.
     */
    val displayEpisodes: String
        get() = episodes?.let { "$it Ep" } ?: "? Ep"

    /**
     * Tampilan tipe penayangan (TV, Movie, OVA, dll.).
     */
    val displayType: String
        get() = type?.ifBlank { "-" } ?: "-"

    /**
     * Tampilan status penayangan.
     */
    val displayStatus: String
        get() = status?.ifBlank { "-" } ?: "-"

    /**
     * Tampilan tahun tayang.
     */
    val displayYear: String
        get() = if (year != null && year > 0) year.toString() else "-"

    /**
     * Tampilan sinopsis dengan teks cadangan jika kosong.
     */
    val displaySynopsis: String
        get() = synopsis?.takeIf { it.isNotBlank() } ?: "Sinopsis belum tersedia."
}

/**
 * Wadah objek gambar poster anime.
 */
data class AnimeImages(
    @SerializedName("jpg")
    val jpg: ImageVariants? = null,

    @SerializedName("webp")
    val webp: ImageVariants? = null
)

/**
 * Varian resolusi gambar poster.
 */
data class ImageVariants(
    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("small_image_url")
    val smallImageUrl: String? = null,

    @SerializedName("large_image_url")
    val largeImageUrl: String? = null
)

/**
 * Model genre anime.
 */
data class Genre(
    @SerializedName("mal_id")
    val malId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("count")
    val count: Int? = null
)
