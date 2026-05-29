package com.example.seriespeliculas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series")
data class SerieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    /** Coincide con [com.example.seriespeliculas.data.model.ListaTipo.name]. */
    val lista: String,
    val creadoEn: Long = System.currentTimeMillis(),
    val tmdbId: Long? = null,
    val posterPath: String? = null,
    /** "movie" o "tv" cuando viene de TMDB. */
    val mediaType: String? = null,
    val valoracion: Int = 0,
    val notas: String = "",
    val genero: String? = null,
    val temporadaActual: Int = 1,
    val capituloActual: Int = 1,
    val fechaLanzamiento: String? = null,
    val duracion: String? = null,
    val totalTemporadas: Int? = null,
    val totalCapitulos: Int? = null
)
