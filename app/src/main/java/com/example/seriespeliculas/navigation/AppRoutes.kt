package com.example.seriespeliculas.navigation

object AppRoutes {
    const val Listas = "listas"
    const val Buscar = "buscar"
    const val Estadisticas = "estadisticas"
    const val Detalle = "detalle/{serieId}"
    const val DetalleTmdb = "detalle_tmdb/{id}/{type}"

    fun detalle(serieId: Long): String = "detalle/$serieId"
    fun detalleTmdb(id: Long, type: String): String = "detalle_tmdb/$id/$type"
}
