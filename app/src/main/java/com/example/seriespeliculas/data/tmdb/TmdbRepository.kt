package com.example.seriespeliculas.data.tmdb

import com.example.seriespeliculas.network.TmdbApi
import retrofit2.HttpException

class TmdbRepository(
    private val api: TmdbApi,
    private val apiKeyConfigurada: Boolean,
) {
    suspend fun buscarMulti(consulta: String): List<TmdbSearchItem> {
        val q = consulta.trim()
        if (!apiKeyConfigurada || q.isEmpty()) return emptyList()

        val response = try {
            api.searchMulti(query = q)
        } catch (e: HttpException) {
            val mensaje = when (e.code()) {
                401 -> "La clave de TMDB no es válida."
                429 -> "Demasiadas peticiones. Prueba dentro de un momento."
                in 500..599 -> "TMDB no responde. Inténtalo más tarde."
                else -> "Error TMDB (${e.code()})."
            }
            throw IllegalStateException(mensaje, e)
        }

        return response.results.mapNotNull { dto ->
            val type = dto.mediaType ?: return@mapNotNull null
            if (type != "movie" && type != "tv") return@mapNotNull null
            val titulo = dto.title ?: dto.name ?: return@mapNotNull null
            TmdbSearchItem(
                id = dto.id,
                titulo = titulo,
                posterPath = dto.posterPath,
                overview = dto.overview,
                mediaType = type,
                generoIds = dto.genreIds ?: emptyList(),
            )
        }
    }

    fun mapearGenero(ids: List<Int>): String? {
        val mapa = mapOf(
            28 to "Acción", 12 to "Aventura", 16 to "Animación", 35 to "Comedia",
            80 to "Crimen", 99 to "Documental", 18 to "Drama", 10751 to "Familia",
            14 to "Fantasía", 36 to "Historia", 27 to "Terror", 10402 to "Música",
            9648 to "Misterio", 10749 to "Romance", 878 to "Ciencia ficción",
            10770 to "Película de TV", 53 to "Suspense", 10752 to "Bélica",
            37 to "Western", 10759 to "Acción & Aventura", 10762 to "Kids",
            10763 to "News", 10764 to "Reality", 10765 to "Sci-Fi & Fantasy",
            10766 to "Soap", 10767 to "Talk", 10768 to "War & Politics"
        )
        return ids.mapNotNull { mapa[it] }.firstOrNull()
    }
}
