package com.pemmob.ani_es.data.model

import com.google.gson.annotations.SerializedName

/**
 * Respons pembungkus daftar anime dari Tenrai / Jikan API.
 */
data class AnimeListResponse(
    @SerializedName("pagination")
    val pagination: Pagination? = null,

    @SerializedName("data")
    val data: List<Anime> = emptyList()
)

/**
 * Respons pembungkus detail anime dari Tenrai / Jikan API.
 */
data class AnimeDetailResponse(
    @SerializedName("data")
    val data: Anime
)

/**
 * Respons pembungkus daftar genre anime.
 */
data class GenreListResponse(
    @SerializedName("data")
    val data: List<Genre> = emptyList()
)

/**
 * Metadata pagination dari API.
 */
data class Pagination(
    @SerializedName("current_page")
    val currentPage: Int? = null,

    @SerializedName("has_next_page")
    val hasNextPage: Boolean? = null,

    @SerializedName("last_visible_page")
    val lastVisiblePage: Int? = null
)
