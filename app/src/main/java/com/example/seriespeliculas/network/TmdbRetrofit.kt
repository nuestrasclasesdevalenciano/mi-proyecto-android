package com.example.seriespeliculas.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TmdbRetrofit {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    fun createApi(apiKey: String): TmdbApi {
        val clientBuilder = OkHttpClient.Builder()
        if (apiKey.isNotBlank()) {
            clientBuilder.addInterceptor { chain ->
                val original = chain.request()
                val url = original.url.newBuilder()
                    .addQueryParameter("api_key", apiKey)
                    .build()
                val request = original.newBuilder()
                    .url(url)
                    .build()
                chain.proceed(request)
            }
        }
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }
}
