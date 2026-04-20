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
        @Query("append_to_response") append: String = "credits",
        @Query("language") language: String = "es-ES",
    ): TmdbMovieDetailDto

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") id: Long,
        @Query("append_to_response") append: String = "credits",
        @Query("language") language: String = "es-ES",
    ): TmdbTvDetailDto
}
