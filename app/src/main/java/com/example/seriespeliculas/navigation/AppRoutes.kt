package com.example.seriespeliculas.navigation

object AppRoutes {
    const val Listas = "listas"
    const val Buscar = "buscar"
    const val Detalle = "detalle/{serieId}"

    fun detalle(serieId: Long): String = "detalle/$serieId"
}
