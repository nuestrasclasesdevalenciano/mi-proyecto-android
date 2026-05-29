package com.example.seriespeliculas.data.tmdb

import com.example.seriespeliculas.network.TmdbApi
import retrofit2.HttpException

class TmdbRepository(
    private val api: TmdbApi,
    private val apiKeyConfigurada: Boolean,
) {
    suspend fun buscarMulti(consulta: String, lang: String = "es-ES"): List<TmdbSearchItem> {
        val q = consulta.trim()
        if (!apiKeyConfigurada || q.isEmpty()) return emptyList()

        val response = try {
            api.searchMulti(query = q, language = lang)
        } catch (e: HttpException) {
            handleHttpException(e)
        }

        return response.results.flatMap { it.toDomainList() }.distinctBy { it.id }
    }

    suspend fun obtenerTendencias(lang: String = "es-ES"): List<TmdbSearchItem> {
        if (!apiKeyConfigurada) return emptyList()
        val response = try {
            api.getTrending(language = lang)
        } catch (e: HttpException) {
            handleHttpException(e)
        }
        return response.results.flatMap { it.toDomainList() }.distinctBy { it.id }
    }

    suspend fun obtenerDetallePelicula(id: Long, lang: String = "es-ES") = try {
        api.getMovieDetails(id, language = lang)
    } catch (e: HttpException) {
        handleHttpException(e)
    }

    suspend fun obtenerDetalleSerie(id: Long, lang: String = "es-ES") = try {
        api.getTvDetails(id, language = lang)
    } catch (e: HttpException) {
        handleHttpException(e)
    }

    suspend fun obtenerDetallePersona(id: Long, lang: String = "es-ES") = try {
        api.getPersonDetails(id, language = lang)
    } catch (e: HttpException) {
        handleHttpException(e)
    }

    suspend fun obtenerSimilaresPelicula(id: Long, lang: String = "es-ES") = try {
        api.getSimilarMovies(id, language = lang).results.mapNotNull { it.toDomain() }
    } catch (e: HttpException) {
        handleHttpException(e)
    }

    suspend fun obtenerSimilaresSerie(id: Long, lang: String = "es-ES") = try {
        api.getSimilarTv(id, language = lang).results.mapNotNull { it.toDomain() }
    } catch (e: HttpException) {
        handleHttpException(e)
    }

    suspend fun obtenerDetalleTemporada(id: Long, seasonNumber: Int, lang: String = "es-ES") = try {
        api.getTvSeasonDetails(id, seasonNumber, language = lang)
    } catch (e: HttpException) {
        handleHttpException(e)
    }

    suspend fun obtenerRecomendaciones(id: Long, type: String, lang: String = "es-ES") = try {
        if (type == "movie") {
            api.getMovieRecommendations(id, language = lang).results.mapNotNull { it.toDomain() }
        } else {
            api.getTvRecommendations(id, language = lang).results.mapNotNull { it.toDomain() }
        }
    } catch (e: HttpException) {
        handleHttpException(e)
    }

    suspend fun descubrirPorGeneros(generos: String?, type: String, lang: String = "es-ES") = try {
        if (type == "movie") {
            api.discoverMovies(language = lang, genreIds = generos).results.mapNotNull { it.toDomain() }
        } else {
            api.discoverTv(language = lang, genreIds = generos).results.mapNotNull { it.toDomain() }
        }
    } catch (e: HttpException) {
        handleHttpException(e)
    }

    suspend fun obtenerProximos(lang: String = "es-ES"): List<TmdbSearchItem> {
        if (!apiKeyConfigurada) return emptyList()
        val response = try {
            api.getUpcomingMovies(language = lang)
        } catch (e: HttpException) {
            handleHttpException(e)
        }
        return response.results.mapNotNull { it.toDomain() }
    }

    private fun handleHttpException(e: HttpException): Nothing {
        val mensaje = when (e.code()) {
            401 -> "La clave de TMDB no es válida."
            429 -> "Demasiadas peticiones. Prueba dentro de un momento."
            in 500..599 -> "TMDB no responde. Inténtalo más tarde."
            else -> "Error TMDB (${e.code()})."
        }
        throw IllegalStateException(mensaje, e)
    }

    private fun com.example.seriespeliculas.network.TmdbMultiResultDto.toDomainList(): List<TmdbSearchItem> {
        if (mediaType == "person") {
            return knownFor?.mapNotNull { it.toDomain() } ?: emptyList()
        }
        val domain = toDomain()
        return if (domain != null) listOf(domain) else emptyList()
    }

    private fun com.example.seriespeliculas.network.TmdbMultiResultDto.toDomain(): TmdbSearchItem? {
        val type = mediaType ?: return null
        if (type != "movie" && type != "tv") return null
        val titulo = title ?: name ?: return null
        return TmdbSearchItem(
            id = id,
            titulo = titulo,
            posterPath = posterPath,
            overview = overview,
            mediaType = type,
            generoIds = genreIds ?: emptyList(),
            fechaLanzamiento = if (type == "movie") releaseDate else firstAirDate,
            puntuacion = voteAverage
        )
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

    fun reverseMapearGenero(nombre: String): Int? {
        val mapa = mapOf(
            "Acción" to 28, "Aventura" to 12, "Animación" to 16, "Comedia" to 35,
            "Crimen" to 80, "Documental" to 99, "Drama" to 18, "Familia" to 10751,
            "Fantasía" to 14, "Historia" to 36, "Terror" to 27, "Música" to 10402,
            "Misterio" to 9648, "Romance" to 10749, "Ciencia ficción" to 878,
            "Película de TV" to 10770, "Suspense" to 53, "Bélica" to 10752,
            "Western" to 37
        )
        return mapa[nombre]
    }
}
