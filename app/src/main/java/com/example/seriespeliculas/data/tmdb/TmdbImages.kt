package com.example.seriespeliculas.data.tmdb

object TmdbImages {
    private const val BASE_URL = "https://image.tmdb.org/t/p/"

    fun urlPoster(path: String?, ancho: String = "w185"): String? {
        if (path.isNullOrBlank()) return null
        val cleanPath = path.trim().let { if (it.startsWith("/")) it else "/$it" }
        return "$BASE_URL$ancho$cleanPath"
    }

    fun urlBackdrop(path: String?, ancho: String = "w780"): String? {
        if (path.isNullOrBlank()) return null
        val cleanPath = path.trim().let { if (it.startsWith("/")) it else "/$it" }
        return "$BASE_URL$ancho$cleanPath"
    }

    fun urlProfile(path: String?, ancho: String = "w185"): String? {
        if (path.isNullOrBlank()) return null
        val cleanPath = path.trim().let { if (it.startsWith("/")) it else "/$it" }
        return "$BASE_URL$ancho$cleanPath"
    }
}
