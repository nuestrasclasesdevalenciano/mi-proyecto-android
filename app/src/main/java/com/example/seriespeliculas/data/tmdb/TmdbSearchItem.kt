package com.example.seriespeliculas.data.tmdb

data class TmdbSearchItem(
    val id: Long,
    val titulo: String,
    val posterPath: String?,
    val overview: String?,
    /** "movie" o "tv". */
    val mediaType: String,
    val generoIds: List<Int> = emptyList(),
) {
    fun etiquetaTipo(): String = when (mediaType) {
        "movie" -> "Película"
        "tv" -> "Serie"
        else -> mediaType
    }
}
