package com.example.seriespeliculas.network

import com.google.gson.annotations.SerializedName

data class TmdbMultiSearchResponseDto(
    val results: List<TmdbMultiResultDto> = emptyList(),
)

data class TmdbMultiResultDto(
    val id: Long,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    val title: String?,
    val name: String?,
    val overview: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
)

data class TmdbMovieDetailDto(
    val id: Long,
    val title: String,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    val credits: TmdbCreditsDto?,
    val genres: List<TmdbGenreDto>?
)

data class TmdbTvDetailDto(
    val id: Long,
    val name: String,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    val credits: TmdbCreditsDto?,
    val genres: List<TmdbGenreDto>?
)

data class TmdbCreditsDto(
    val cast: List<TmdbCastDto>
)

data class TmdbCastDto(
    val name: String,
    val character: String?,
    @SerializedName("profile_path") val profilePath: String?
)

data class TmdbGenreDto(
    val id: Int,
    val name: String
)
