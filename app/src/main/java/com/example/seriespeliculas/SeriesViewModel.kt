package com.example.seriespeliculas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seriespeliculas.data.SeriesRepository
import com.example.seriespeliculas.data.local.SerieEntity
import com.example.seriespeliculas.data.model.ListaTipo
import com.example.seriespeliculas.data.tmdb.TmdbRepository
import com.example.seriespeliculas.data.tmdb.TmdbSearchItem
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BuscarUiState {
    data object Inicial : BuscarUiState
    data object Cargando : BuscarUiState
    data class Exito(val items: List<TmdbSearchItem>) : BuscarUiState
    data class Error(val mensaje: String) : BuscarUiState
}

sealed interface DetalleTmdbUiState {
    data object Cargando : DetalleTmdbUiState
    data class Exito(
        val id: Long,
        val titulo: String,
        val descripcion: String,
        val posterPath: String?,
        val backdropPath: String?,
        val reparto: List<com.example.seriespeliculas.network.TmdbCastDto>,
        val generos: List<String>,
        val mediaType: String,
        val director: String? = null,
        val plataformas: List<com.example.seriespeliculas.network.TmdbProviderDto> = emptyList(),
        val trailerUrl: String? = null,
        val similares: List<TmdbSearchItem> = emptyList(),
        val fechaLanzamiento: String? = null,
        val duracion: String? = null,
        val valoracion: Double? = null,
        val numTemporadas: Int? = null,
        val numEpisodios: Int? = null,
        val temporadas: List<com.example.seriespeliculas.network.TmdbSeasonDto>? = null
    ) : DetalleTmdbUiState
    data class Error(val mensaje: String) : DetalleTmdbUiState
}

sealed interface DetallePersonaUiState {
    data object Cargando : DetallePersonaUiState
    data class Exito(
        val id: Long,
        val nombre: String,
        val biografia: String?,
        val fotoPath: String?,
        val lugarNacimiento: String?,
        val fechaNacimiento: String?,
        val creditos: List<TmdbSearchItem>
    ) : DetallePersonaUiState
    data class Error(val mensaje: String) : DetallePersonaUiState
}

enum class OrdenTipo(val etiqueta: String) {
    FECHA_DESC("Más recientes"),
    FECHA_ASC("Más antiguos"),
    TITULO_ASC("Título (A-Z)"),
    VALORACION_DESC("Mejor valorados"),
    VALORACION_ASC("Peor valorados")
}

class SeriesViewModel(
    private val seriesRepository: SeriesRepository,
    private val tmdbRepository: TmdbRepository,
    private val userPreferences: com.example.seriespeliculas.data.local.UserPreferences,
    val tmdbHabilitado: Boolean,
) : ViewModel() {

    private val listaSeleccionadaInternal = MutableStateFlow(ListaTipo.POR_VER)
    val listaSeleccionada: StateFlow<ListaTipo> = listaSeleccionadaInternal.asStateFlow()

    private val consultaListaInternal = MutableStateFlow("")
    val consultaLista: StateFlow<String> = consultaListaInternal.asStateFlow()

    private val ordenSeleccionadoInternal = MutableStateFlow(OrdenTipo.FECHA_DESC)
    val ordenSeleccionado: StateFlow<OrdenTipo> = ordenSeleccionadoInternal.asStateFlow()

    val todasLasSeries: StateFlow<List<SerieEntity>> = seriesRepository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val generoSeleccionadoInternal = MutableStateFlow<String?>(null)
    val generoSeleccionado: StateFlow<String?> = generoSeleccionadoInternal.asStateFlow()

    val topGeneros: StateFlow<List<String>> = todasLasSeries.map { series ->
        series.filter { it.lista == ListaTipo.VISTAS.name }
            .mapNotNull { it.genero }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }
            .take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val recomendacionesGeneralesInternal = MutableStateFlow<List<TmdbSearchItem>>(emptyList())
    val recomendacionesGenerales = recomendacionesGeneralesInternal.asStateFlow()

    private val recomendacionesIAInternal = MutableStateFlow<List<TmdbSearchItem>>(emptyList())
    val recomendacionesIA = recomendacionesIAInternal.asStateFlow()

    private val mensajeIAInternal = MutableStateFlow("")
    val mensajeIA = mensajeIAInternal.asStateFlow()

    private val cargandoIAInternal = MutableStateFlow(false)
    val cargandoIA = cargandoIAInternal.asStateFlow()

    private var ultimaFavoritaUsada: Long? = null

    private val busquedasRecientesInternal = MutableStateFlow<List<String>>(emptyList())
    val busquedasRecientes = busquedasRecientesInternal.asStateFlow()

    val seriesEnLista: StateFlow<List<SerieEntity>> = combine(
        listaSeleccionadaInternal,
        consultaListaInternal,
        ordenSeleccionadoInternal,
        generoSeleccionadoInternal
    ) { lista, consulta, orden, genero ->
        val result = Triple(lista, consulta, orden)
        val finalResult = Quadruple(result.first, result.second, result.third, genero)
        finalResult
    }.flatMapLatest { (lista, consulta, orden, genero) ->
        seriesRepository.observePorLista(lista).map { series ->
            series.filter { it.titulo.contains(consulta, ignoreCase = true) }
                .filter { genero == null || it.genero == genero }
                .let { filtered ->
                    when (orden) {
                        OrdenTipo.FECHA_DESC -> filtered.sortedByDescending { it.creadoEn }
                        OrdenTipo.FECHA_ASC -> filtered.sortedBy { it.creadoEn }
                        OrdenTipo.TITULO_ASC -> filtered.sortedBy { it.titulo }
                        OrdenTipo.VALORACION_DESC -> filtered.sortedByDescending { it.valoracion }
                        OrdenTipo.VALORACION_ASC -> filtered.sortedBy { it.valoracion }
                    }
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun seleccionarGenero(genero: String?) {
        generoSeleccionadoInternal.value = genero
    }

    private val buscarStateInternal = MutableStateFlow<BuscarUiState>(BuscarUiState.Inicial)
    val buscarState: StateFlow<BuscarUiState> = buscarStateInternal.asStateFlow()

    private val tendenciasInternal = MutableStateFlow<List<TmdbSearchItem>>(emptyList())
    val tendencias: StateFlow<List<TmdbSearchItem>> = tendenciasInternal.asStateFlow()

    private val proximosInternal = MutableStateFlow<List<TmdbSearchItem>>(emptyList())
    val proximos: StateFlow<List<TmdbSearchItem>> = proximosInternal.asStateFlow()

    init {
        cargarTendencias()
        cargarProximos()
        generarRecomendacionesIA()
    }

    fun refrescarTendencias() {
        cargarTendencias()
        cargarProximos()
        generarRecomendacionesIA()
    }

    fun generarRecomendacionesIA() {
        if (!tmdbHabilitado) return
        viewModelScope.launch {
            cargandoIAInternal.value = true
            try {
                // Pequeña pausa para que se vea la animación de carga
                delay(600)
                val vistas = todasLasSeries.value.filter { it.lista == ListaTipo.VISTAS.name }
                val favoritas = vistas.filter { it.valoracion >= 4 }
                
                if (favoritas.isNotEmpty()) {
                    // Intentar elegir una favorita diferente a la última
                    val candidatas = if (favoritas.size > 1) {
                        favoritas.filter { it.tmdbId != ultimaFavoritaUsada }
                    } else favoritas
                    
                    val semilla = candidatas.random()
                    ultimaFavoritaUsada = semilla.tmdbId

                    val type = semilla.mediaType ?: "movie"
                    val items = tmdbRepository.obtenerRecomendaciones(semilla.tmdbId ?: 0L, type, idiomaInternal.value).shuffled()
                    
                    if (items.isNotEmpty()) {
                        mensajeIAInternal.value = "Basado en tu amor por \"${semilla.titulo}\", te sugiero..."
                        recomendacionesIAInternal.value = items.take(10)
                        return@launch
                    }
                }
                
                // Fallback a géneros si no hay favoritas o fallaron las recomendaciones
                val topG = topGeneros.value
                if (topG.isNotEmpty()) {
                    val genero = topG.random()
                    val generoId = tmdbRepository.reverseMapearGenero(genero)
                    val items = tmdbRepository.descubrirPorGeneros(generoId?.toString(), "movie", idiomaInternal.value).shuffled()
                    
                    mensajeIAInternal.value = "Como te gusta el cine de $genero, prueba con estas:"
                    recomendacionesIAInternal.value = items.take(10)
                } else {
                    // Fallback final: Tendencias
                    val items = tmdbRepository.obtenerTendencias(idiomaInternal.value).shuffled()
                    mensajeIAInternal.value = "¡Empieza a ver algo nuevo! Mira lo que es tendencia hoy:"
                    recomendacionesIAInternal.value = items.take(10)
                }
            } catch (e: Exception) {
                // Silently fail
            } finally {
                cargandoIAInternal.value = false
            }
        }
    }

    private val idiomaInternal = MutableStateFlow(userPreferences.idioma)
    val idioma = idiomaInternal.asStateFlow()

    private val temaOscuroInternal = MutableStateFlow(userPreferences.temaOscuro) // null = sistema
    val temaOscuro = temaOscuroInternal.asStateFlow()

    fun cambiarIdioma(nuevoIdioma: String) {
        userPreferences.idioma = nuevoIdioma
        idiomaInternal.value = nuevoIdioma
        cargarTendencias()
        cargarProximos()
    }

    fun cambiarTema(oscuro: Boolean?) {
        userPreferences.temaOscuro = oscuro
        temaOscuroInternal.value = oscuro
    }

    private fun cargarTendencias() {
        if (!tmdbHabilitado) return
        viewModelScope.launch {
            try {
                val items = tmdbRepository.obtenerTendencias(idiomaInternal.value)
                tendenciasInternal.value = items
            } catch (e: Exception) {
                // Silently fail for trending
            }
        }
    }

    private fun cargarProximos() {
        if (!tmdbHabilitado) return
        viewModelScope.launch {
            try {
                val items = tmdbRepository.obtenerProximos(idiomaInternal.value)
                proximosInternal.value = items
            } catch (e: Exception) {
                // Silently fail for upcoming
            }
        }
    }

    fun cargarRecomendacionesPorGenero(genero: String) {
        if (!tmdbHabilitado) return
        viewModelScope.launch {
            try {
                val items = tmdbRepository.buscarMulti(genero, idiomaInternal.value)
                recomendacionesGeneralesInternal.value = items.take(10)
            } catch (e: Exception) { /* Silently fail */ }
        }
    }

    private var buscarJob: Job? = null

    fun seleccionarLista(lista: ListaTipo) {
        listaSeleccionadaInternal.value = lista
    }

    fun buscarEnLista(consulta: String) {
        consultaListaInternal.value = consulta
    }

    fun cambiarOrden(orden: OrdenTipo) {
        ordenSeleccionadoInternal.value = orden
    }

    fun buscar(consulta: String) {
        buscarJob?.cancel()
        if (!tmdbHabilitado) {
            buscarStateInternal.value = BuscarUiState.Inicial
            return
        }
        val q = consulta.trim()
        if (q.length < 2) {
            buscarStateInternal.value = BuscarUiState.Inicial
            return
        }
        buscarJob = viewModelScope.launch {
            // Esperar un poco para no saturar la red y el procesador (debounce)
            delay(400)
            buscarStateInternal.value = BuscarUiState.Cargando
            try {
                val items = tmdbRepository.buscarMulti(q, idiomaInternal.value)
                buscarStateInternal.value = BuscarUiState.Exito(items)
                
                // Guardar en búsquedas recientes si hay resultados
                if (items.isNotEmpty()) {
                    val actual = busquedasRecientesInternal.value.toMutableList()
                    if (q !in actual) {
                        actual.add(0, q)
                        busquedasRecientesInternal.value = actual.take(5)
                    }
                }
            } catch (e: IllegalStateException) {
                buscarStateInternal.value = BuscarUiState.Error(e.message ?: "Error al buscar.")
            } catch (_: IOException) {
                buscarStateInternal.value = BuscarUiState.Error("No hay conexión o TMDB no responde.")
            } catch (e: Exception) {
                buscarStateInternal.value = BuscarUiState.Error(e.message ?: "Error al buscar.")
            }
        }
    }

    fun limpiarBusqueda() {
        buscarJob?.cancel()
        buscarStateInternal.value = BuscarUiState.Inicial
    }

    fun borrarHistorialBusqueda() {
        busquedasRecientesInternal.value = emptyList()
    }

    fun añadirTituloAListaActual(titulo: String) {
        viewModelScope.launch {
            // Intento de detección automática de género si TMDB está habilitado
            var generoAuto: String? = null
            if (tmdbHabilitado) {
                try {
                    val resultados = tmdbRepository.buscarMulti(titulo, idiomaInternal.value)
                    val match = resultados.find { it.titulo.equals(titulo, ignoreCase = true) } 
                        ?: resultados.firstOrNull()
                    
                    if (match != null) {
                        generoAuto = tmdbRepository.mapearGenero(match.generoIds)
                    }
                } catch (e: Exception) {
                    // Ignorar errores de red para la detección automática
                }
            }
            seriesRepository.añadir(titulo, listaSeleccionadaInternal.value, genero = generoAuto)
        }
    }

    fun añadirResultadoTmdb(item: TmdbSearchItem) {
        viewModelScope.launch {
            val genero = tmdbRepository.mapearGenero(item.generoIds)
            seriesRepository.añadirDesdeTmdb(item, listaSeleccionadaInternal.value, genero)
        }
    }

    fun añadirResultadoTmdbConDetalles(state: DetalleTmdbUiState.Exito) {
        viewModelScope.launch {
            val genero = state.generos.firstOrNull()
            seriesRepository.añadir(
                titulo = state.titulo,
                lista = listaSeleccionadaInternal.value,
                tmdbId = state.id,
                posterPath = state.posterPath,
                mediaType = state.mediaType,
                genero = genero,
                fechaLanzamiento = state.fechaLanzamiento,
                duracion = state.duracion,
                totalTemporadas = state.numTemporadas,
                totalCapitulos = state.numEpisodios
            )
        }
    }

    fun exportarDatos(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val series = todasLasSeries.value
                val json = com.google.gson.Gson().toJson(series)
                val fileName = "backup_cine_series.json"
                val file = java.io.File(context.cacheDir, fileName)
                file.writeText(json)
                
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Guardar copia de seguridad"))
            } catch (e: Exception) {
                // Error al exportar
            }
        }
    }

    fun importarDatos(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = inputStream?.bufferedReader()
                val json = reader?.readText()
                if (json != null) {
                    val type = object : com.google.gson.reflect.TypeToken<List<SerieEntity>>() {}.type
                    val series: List<SerieEntity> = com.google.gson.Gson().fromJson(json, type)
                    series.forEach { 
                        // Insertar ignorando el ID original para evitar conflictos
                        seriesRepository.añadir(
                            titulo = it.titulo,
                            lista = ListaTipo.entries.find { l -> l.name == it.lista } ?: ListaTipo.POR_VER,
                            tmdbId = it.tmdbId,
                            posterPath = it.posterPath,
                            mediaType = it.mediaType,
                            genero = it.genero,
                            fechaLanzamiento = it.fechaLanzamiento,
                            duracion = it.duracion,
                            totalTemporadas = it.totalTemporadas,
                            totalCapitulos = it.totalCapitulos
                        )
                    }
                }
            } catch (e: Exception) {
                // Error al importar
            }
        }
    }

    fun eliminar(serie: SerieEntity) {
        viewModelScope.launch {
            seriesRepository.eliminar(serie)
        }
    }

    fun limpiarCatalogo() {
        viewModelScope.launch {
            seriesRepository.limpiarTodo()
        }
    }

    fun actualizar(serie: SerieEntity) {
        viewModelScope.launch {
            seriesRepository.actualizar(serie)
        }
    }

    fun observeSerie(id: Long): Flow<SerieEntity?> = seriesRepository.observeById(id)

    suspend fun cargarSerie(id: Long): SerieEntity? = seriesRepository.getById(id)

    suspend fun obtenerDetalleTmdb(id: Long, type: String): DetalleTmdbUiState.Exito {
        val lang = idiomaInternal.value
        return if (type == "movie") {
            val d = tmdbRepository.obtenerDetallePelicula(id, lang)
            val director = d.credits?.crew?.find { it.job == "Director" }?.name
            val region = if (lang.contains("-")) lang.split("-")[1] else "ES"
            val plataformas = d.watchProviders?.results?.get(region)?.streaming ?: emptyList()
            val trailer = d.videos?.results?.find { it.site == "YouTube" && it.type == "Trailer" }?.key
                ?: d.videos?.results?.firstOrNull { it.site == "YouTube" }?.key
            val similares = tmdbRepository.obtenerSimilaresPelicula(id, lang)
            
            DetalleTmdbUiState.Exito(
                id = d.id,
                titulo = d.title,
                descripcion = d.overview ?: "",
                posterPath = d.posterPath,
                backdropPath = d.backdropPath,
                reparto = d.credits?.cast ?: emptyList(),
                generos = d.genres?.map { it.name } ?: emptyList(),
                mediaType = "movie",
                director = director,
                plataformas = plataformas,
                trailerUrl = trailer?.let { "https://www.youtube.com/watch?v=$it" },
                similares = similares,
                fechaLanzamiento = d.releaseDate,
                duracion = d.runtime?.let { "${it} min" },
                valoracion = d.voteAverage
            )
        } else {
            val d = tmdbRepository.obtenerDetalleSerie(id, lang)
            val director = d.credits?.crew?.find { it.job == "Director" || it.job == "Executive Producer" }?.name
            val region = if (lang.contains("-")) lang.split("-")[1] else "ES"
            val plataformas = d.watchProviders?.results?.get(region)?.streaming ?: emptyList()
            val trailer = d.videos?.results?.find { it.site == "YouTube" && it.type == "Trailer" }?.key
                ?: d.videos?.results?.firstOrNull { it.site == "YouTube" }?.key
            val similares = tmdbRepository.obtenerSimilaresSerie(id, lang)

            DetalleTmdbUiState.Exito(
                id = d.id,
                titulo = d.name,
                descripcion = d.overview ?: "",
                posterPath = d.posterPath,
                backdropPath = d.backdropPath,
                reparto = d.credits?.cast ?: emptyList(),
                generos = d.genres?.map { it.name } ?: emptyList(),
                mediaType = "tv",
                director = director,
                plataformas = plataformas,
                trailerUrl = trailer?.let { "https://www.youtube.com/watch?v=$it" },
                similares = similares,
                fechaLanzamiento = d.firstAirDate,
                duracion = d.episodeRunTime?.firstOrNull()?.let { "${it} min" },
                valoracion = d.voteAverage,
                numTemporadas = d.numberOfSeasons,
                numEpisodios = d.numberOfEpisodes,
                temporadas = d.seasons
            )
        }
    }

    suspend fun obtenerDetallePersona(id: Long): DetallePersonaUiState.Exito {
        val d = tmdbRepository.obtenerDetallePersona(id, idiomaInternal.value)
        return DetallePersonaUiState.Exito(
            id = d.id,
            nombre = d.name,
            biografia = d.biography,
            fotoPath = d.profilePath,
            lugarNacimiento = d.placeOfBirth,
            fechaNacimiento = d.birthday,
            creditos = d.combinedCredits?.cast?.mapNotNull { it.toDomain() } ?: emptyList()
        )
    }

    suspend fun obtenerDetalleTemporada(id: Long, seasonNumber: Int): com.example.seriespeliculas.network.TmdbSeasonDetailDto {
        return tmdbRepository.obtenerDetalleTemporada(id, seasonNumber, idiomaInternal.value)
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
        )
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

class SeriesViewModelFactory(
    private val seriesRepository: SeriesRepository,
    private val tmdbRepository: TmdbRepository,
    private val userPreferences: com.example.seriespeliculas.data.local.UserPreferences,
    private val tmdbHabilitado: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SeriesViewModel::class.java)) {
            "ViewModel desconocida: ${modelClass.name}"
        }
        @Suppress("UNCHECKED_CAST")
        return SeriesViewModel(seriesRepository, tmdbRepository, userPreferences, tmdbHabilitado) as T
    }
}
