package com.example.seriespeliculas.data.model

enum class ListaTipo {
    POR_VER,
    VIENDO,
    VISTAS,
    REVER;

    fun etiqueta(): String = when (this) {
        POR_VER -> "Por ver"
        VIENDO -> "Viendo"
        VISTAS -> "Vistas"
        REVER -> "Rever"
    }
}
