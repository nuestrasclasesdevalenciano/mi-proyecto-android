package com.example.seriespeliculas.data.tmdb

object TmdbImages {
    fun urlPoster(path: String?, ancho: String = "w185"): String? {
        if (path.isNullOrBlank()) return null
        return "https://image.tmdb.org/t/p/$ancho$path"
    }
}
