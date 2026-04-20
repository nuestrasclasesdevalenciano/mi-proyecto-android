package com.example.seriespeliculas.data.tmdb

object TmdbImages {
    fun urlPoster(path: String?, ancho: String = "w185"): String? {
        if (path.isNullOrBlank()) return null
        return "https://image.tmdb.org/t/p/$ancho$path"
    }

    fun urlBackdrop(path: String?, ancho: String = "w780"): String? {
        if (path.isNullOrBlank()) return null
        return "https://image.tmdb.org/t/p/$ancho$path"
    }

    fun urlProfile(path: String?, ancho: String = "w185"): String? {
        if (path.isNullOrBlank()) return null
        return "https://image.tmdb.org/t/p/$ancho$path"
    }
}
