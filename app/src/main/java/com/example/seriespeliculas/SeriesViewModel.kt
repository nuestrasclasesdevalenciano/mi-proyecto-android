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
        val mediaType: String
    ) : DetalleTmdbUiState
    data class Error(val mensaje: String) : DetalleTmdbUiState
}

enum class OrdenTipo(val etiqueta: String) {
    FECHA_DESC("Más recientes"),
    FECHA_ASC("Más antiguos"),
    VALORACION_DESC("Mejor valorados"),
    VALORACION_ASC("Peor valorados")
}

class SeriesViewModel(
    private val seriesRepository: SeriesRepository,
    private val tmdbRepository: TmdbRepository,
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

    init {
        cargarTendencias()
    }

    private fun cargarTendencias() {
        if (!tmdbHabilitado) return
        viewModelScope.launch {
            try {
                val items = tmdbRepository.obtenerTendencias()
                tendenciasInternal.value = items
            } catch (e: Exception) {
                // Silently fail for trending
            }
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
            buscarStateInternal.value = BuscarUiState.Cargando
            try {
                val items = tmdbRepository.buscarMulti(q)
                buscarStateInternal.value = BuscarUiState.Exito(items)
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

    fun añadirTituloAListaActual(titulo: String) {
        viewModelScope.launch {
            seriesRepository.añadir(titulo, listaSeleccionadaInternal.value)
        }
    }

    fun añadirResultadoTmdb(item: TmdbSearchItem) {
        viewModelScope.launch {
            val genero = tmdbRepository.mapearGenero(item.generoIds)
            seriesRepository.añadirDesdeTmdb(item, listaSeleccionadaInternal.value, genero)
        }
    }

    fun eliminar(serie: SerieEntity) {
        viewModelScope.launch {
            seriesRepository.eliminar(serie)
        }
    }

    fun actualizar(serie: SerieEntity) {
        viewModelScope.launch {
            seriesRepository.actualizar(serie)
        }
    }

    suspend fun cargarSerie(id: Long): SerieEntity? = seriesRepository.getById(id)

    suspend fun obtenerDetalleTmdb(id: Long, type: String): DetalleTmdbUiState.Exito {
        return if (type == "movie") {
            val d = tmdbRepository.obtenerDetallePelicula(id)
            DetalleTmdbUiState.Exito(
                id = d.id,
                titulo = d.title,
                descripcion = d.overview ?: "",
                posterPath = d.posterPath,
                backdropPath = d.backdropPath,
                reparto = d.credits?.cast ?: emptyList(),
                generos = d.genres?.map { it.name } ?: emptyList(),
                mediaType = "movie"
            )
        } else {
            val d = tmdbRepository.obtenerDetalleSerie(id)
            DetalleTmdbUiState.Exito(
                id = d.id,
                titulo = d.name,
                descripcion = d.overview ?: "",
                posterPath = d.posterPath,
                backdropPath = d.backdropPath,
                reparto = d.credits?.cast ?: emptyList(),
                generos = d.genres?.map { it.name } ?: emptyList(),
                mediaType = "tv"
            )
        }
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
    private val tmdbHabilitado: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SeriesViewModel::class.java)) {
            "ViewModel desconocida: ${modelClass.name}"
        }
        @Suppress("UNCHECKED_CAST")
        return SeriesViewModel(seriesRepository, tmdbRepository, tmdbHabilitado) as T
    }
}
