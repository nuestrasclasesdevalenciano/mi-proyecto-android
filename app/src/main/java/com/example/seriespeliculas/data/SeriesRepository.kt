package com.example.seriespeliculas.data

import com.example.seriespeliculas.data.local.SerieDao
import com.example.seriespeliculas.data.local.SerieEntity
import com.example.seriespeliculas.data.model.ListaTipo
import com.example.seriespeliculas.data.tmdb.TmdbSearchItem
import kotlinx.coroutines.flow.Flow

class SeriesRepository(private val serieDao: SerieDao) {

    fun observePorLista(lista: ListaTipo): Flow<List<SerieEntity>> =
        serieDao.observePorLista(lista.name)

    fun observeAll(): Flow<List<SerieEntity>> = serieDao.observeAll()

    fun observeById(id: Long): Flow<SerieEntity?> = serieDao.observeById(id)

    suspend fun getById(id: Long): SerieEntity? = serieDao.getById(id)

    suspend fun añadir(
        titulo: String,
        lista: ListaTipo,
        tmdbId: Long? = null,
        posterPath: String? = null,
        mediaType: String? = null,
        genero: String? = null,
        fechaLanzamiento: String? = null,
        duracion: String? = null,
        totalTemporadas: Int? = null,
        totalCapitulos: Int? = null
    ) {
        val limpio = titulo.trim()
        if (limpio.isEmpty()) return
        serieDao.insert(
            SerieEntity(
                titulo = limpio,
                lista = lista.name,
                tmdbId = tmdbId,
                posterPath = posterPath,
                mediaType = mediaType,
                genero = genero,
                fechaLanzamiento = fechaLanzamiento,
                duracion = duracion,
                totalTemporadas = totalTemporadas,
                totalCapitulos = totalCapitulos
            ),
        )
    }

    suspend fun añadirDesdeTmdb(item: TmdbSearchItem, lista: ListaTipo, genero: String? = null) {
        añadir(
            titulo = item.titulo,
            lista = lista,
            tmdbId = item.id,
            posterPath = item.posterPath,
            mediaType = item.mediaType,
            genero = genero,
            fechaLanzamiento = item.fechaLanzamiento,
            duracion = item.duracion
        )
    }

    suspend fun eliminar(serie: SerieEntity) {
        serieDao.delete(serie)
    }

    suspend fun actualizar(serie: SerieEntity) {
        serieDao.update(serie)
    }

    suspend fun limpiarTodo() {
        serieDao.deleteAll()
    }
}
