package com.example.seriespeliculas.data.model

enum class ListaTipo {
    POR_VER,
    VISTAS,
    REVER;

    fun etiqueta(): String = when (this) {
        POR_VER -> "Por ver"
        VISTAS -> "Vistas"
        REVER -> "Rever"
    }
}
