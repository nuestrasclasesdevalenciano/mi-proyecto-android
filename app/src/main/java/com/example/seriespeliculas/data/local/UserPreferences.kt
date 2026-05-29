package com.example.seriespeliculas.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "tema_oscuro"
        private const val KEY_LANGUAGE = "idioma"
    }

    /**
     * null = System
     * true = Dark
     * false = Light
     */
    var temaOscuro: Boolean?
        get() = if (prefs.contains(KEY_THEME)) prefs.getBoolean(KEY_THEME, false) else null
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_THEME).apply()
            } else {
                prefs.edit().putBoolean(KEY_THEME, value).apply()
            }
        }

    var idioma: String
        get() = prefs.getString(KEY_LANGUAGE, "es-ES") ?: "es-ES"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()
}
