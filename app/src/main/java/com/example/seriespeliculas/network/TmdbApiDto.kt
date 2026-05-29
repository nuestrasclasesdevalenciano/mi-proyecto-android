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
    @SerializedName("profile_path") val profilePath: String? = null,
    val title: String?,
    val name: String?,
    val overview: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
    @SerializedName("known_for") val knownFor: List<TmdbMultiResultDto>? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double? = null
)

data class TmdbMovieDetailDto(
    val id: Long,
    val title: String,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    val runtime: Int?,
    @SerializedName("vote_average") val voteAverage: Double?,
    val credits: TmdbCreditsDto?,
    val genres: List<TmdbGenreDto>?,
    @SerializedName("watch/providers") val watchProviders: TmdbWatchProvidersResponseDto?,
    val videos: TmdbVideosResponseDto?
)

data class TmdbTvDetailDto(
    val id: Long,
    val name: String,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("episode_run_time") val episodeRunTime: List<Int>?,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int?,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int?,
    @SerializedName("vote_average") val voteAverage: Double?,
    val seasons: List<TmdbSeasonDto>?,
    val credits: TmdbCreditsDto?,
    val genres: List<TmdbGenreDto>?,
    @SerializedName("watch/providers") val watchProviders: TmdbWatchProvidersResponseDto?,
    val videos: TmdbVideosResponseDto?
)

data class TmdbSeasonDto(
    val id: Long,
    val name: String,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("episode_count") val episodeCount: Int,
    @SerializedName("air_date") val airDate: String?
)

data class TmdbSeasonDetailDto(
    val id: Long,
    val name: String,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("season_number") val seasonNumber: Int,
    val episodes: List<TmdbEpisodeDto>?
)

data class TmdbEpisodeDto(
    val id: Long,
    val name: String,
    val overview: String?,
    @SerializedName("episode_number") val episodeNumber: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("still_path") val stillPath: String?,
    @SerializedName("air_date") val airDate: String?,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("vote_average") val voteAverage: Double?,
    val crew: List<TmdbCrewDto>?,
    val guestStars: List<TmdbCastDto>?
)

data class TmdbVideosResponseDto(
    val results: List<TmdbVideoDto>
)

data class TmdbVideoDto(
    val key: String,
    val site: String,
    val type: String
)

data class TmdbWatchProvidersResponseDto(
    val results: Map<String, TmdbCountryProvidersDto>
)

data class TmdbCountryProvidersDto(
    @SerializedName("flatrate") val streaming: List<TmdbProviderDto>? = null,
    val buy: List<TmdbProviderDto>? = null,
    val rent: List<TmdbProviderDto>? = null
)

data class TmdbProviderDto(
    @SerializedName("provider_id") val id: Int,
    @SerializedName("provider_name") val name: String,
    @SerializedName("logo_path") val logoPath: String?
)

data class TmdbCreditsDto(
    val cast: List<TmdbCastDto>,
    val crew: List<TmdbCrewDto>? = null
)

data class TmdbCastDto(
    val id: Long,
    val name: String,
    val character: String?,
    @SerializedName("profile_path") val profilePath: String?
)

data class TmdbCrewDto(
    val name: String,
    val job: String,
    @SerializedName("profile_path") val profilePath: String?
)

data class TmdbGenreDto(
    val id: Int,
    val name: String
)

data class TmdbPersonDetailDto(
    val id: Long,
    val name: String,
    val biography: String?,
    @SerializedName("profile_path") val profilePath: String?,
    @SerializedName("place_of_birth") val placeOfBirth: String?,
    val birthday: String?,
    @SerializedName("combined_credits") val combinedCredits: TmdbCombinedCreditsDto?
)

data class TmdbCombinedCreditsDto(
    val cast: List<TmdbMultiResultDto>
)
