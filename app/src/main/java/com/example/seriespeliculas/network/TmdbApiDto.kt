package com.example.seriespeliculas.network

import com.google.gson.annotations.SerializedName

data class TmdbMultiSearchResponseDto(
    val results: List<TmdbMultiResultDto> = emptyList(),
)

data class TmdbMultiResultDto(
    val id: Long,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("poster_path") val posterPath: String?,
    val title: String?,
    val name: String?,
    val overview: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
)
