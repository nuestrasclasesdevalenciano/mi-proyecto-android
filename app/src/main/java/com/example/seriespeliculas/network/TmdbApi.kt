package com.example.seriespeliculas.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("language") language: String = "es-ES",
    ): TmdbMultiSearchResponseDto

    @GET("trending/all/day")
    suspend fun getTrending(
        @Query("language") language: String = "es-ES",
    ): TmdbMultiSearchResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") id: Long,
        @Query("append_to_response") append: String = "credits,watch/providers,videos",
        @Query("language") language: String = "es-ES",
    ): TmdbMovieDetailDto

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") id: Long,
        @Query("append_to_response") append: String = "credits,watch/providers,videos",
        @Query("language") language: String = "es-ES",
    ): TmdbTvDetailDto

    @GET("person/{person_id}")
    suspend fun getPersonDetails(
        @Path("person_id") id: Long,
        @Query("append_to_response") append: String = "combined_credits",
        @Query("language") language: String = "es-ES",
    ): TmdbPersonDetailDto

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") id: Long,
        @Query("language") language: String = "es-ES",
    ): TmdbMultiSearchResponseDto

    @GET("tv/{tv_id}/similar")
    suspend fun getSimilarTv(
        @Path("tv_id") id: Long,
        @Query("language") language: String = "es-ES",
    ): TmdbMultiSearchResponseDto

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("language") language: String = "es-ES",
        @Query("region") region: String = "ES",
    ): TmdbMultiSearchResponseDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("language") language: String = "es-ES",
        @Query("with_genres") genreIds: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): TmdbMultiSearchResponseDto

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("language") language: String = "es-ES",
        @Query("with_genres") genreIds: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): TmdbMultiSearchResponseDto

    @GET("movie/{movie_id}/recommendations")
    suspend fun getMovieRecommendations(
        @Path("movie_id") id: Long,
        @Query("language") language: String = "es-ES"
    ): TmdbMultiSearchResponseDto

    @GET("tv/{tv_id}/recommendations")
    suspend fun getTvRecommendations(
        @Path("tv_id") id: Long,
        @Query("language") language: String = "es-ES"
    ): TmdbMultiSearchResponseDto

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getTvSeasonDetails(
        @Path("tv_id") id: Long,
        @Path("season_number") seasonNumber: Int,
        @Query("language") language: String = "es-ES"
    ): TmdbSeasonDetailDto
}
