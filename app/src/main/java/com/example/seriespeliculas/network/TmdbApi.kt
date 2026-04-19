package com.example.seriespeliculas.network

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {
    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("language") language: String = "es-ES",
    ): TmdbMultiSearchResponseDto
}
