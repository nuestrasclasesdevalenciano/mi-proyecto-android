package com.example.seriespeliculas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.data.local.SerieEntity
import com.example.seriespeliculas.data.model.ListaTipo
import com.example.seriespeliculas.data.tmdb.TmdbImages
import com.example.seriespeliculas.network.TmdbEpisodeDto
import com.example.seriespeliculas.network.TmdbSeasonDetailDto
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun DetalleSerieScreen(
    serieId: Long,
    viewModel: SeriesViewModel,
    onVolver: () -> Unit,
    onVerDetalleTmdb: (Long, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val serieOpt by viewModel.observeSerie(serieId).collectAsState(initial = null)
    val topGeneros by viewModel.topGeneros.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Detalle", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onVolver) { Text("Volver") }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val serie = serieOpt
        if (serie == null) {
            Text(
                text = "Cargando o no encontrado...",
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            DetalleContenido(
                serie = serie,
                topGeneros = topGeneros,
                viewModel = viewModel,
                onActualizar = { viewModel.actualizar(it) },
                onQuitar = {
                    viewModel.eliminar(serie)
                    onVolver()
                },
                onVerDetalleTmdb = onVerDetalleTmdb
            )
        }
    }
}

@Composable
private fun DetalleContenido(
    serie: SerieEntity,
    topGeneros: List<String>,
    viewModel: SeriesViewModel,
    onActualizar: (SerieEntity) -> Unit,
    onQuitar: () -> Unit,
    onVerDetalleTmdb: (Long, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    
    // Estado local con TextFieldValue para evitar problemas con acentos e IME en dispositivos antiguos
    var notasLocal by remember(serie.id) { 
        mutableStateOf(TextFieldValue(serie.notas)) 
    }
    
    // Sincronización con el ViewModel con debounce
    LaunchedEffect(notasLocal.text) {
        if (notasLocal.text != serie.notas) {
            delay(600)
            onActualizar(serie.copy(notas = notasLocal.text))
        }
    }

    val fechaAgregado = remember(serie.creadoEn) {
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            .format(Date(serie.creadoEn))
    }
    
    val tipo = when (serie.mediaType) {
        "movie" -> "Película"
        "tv" -> "Serie"
        else -> null
    }

    val coincideConTusGustos = remember(serie.genero, topGeneros) {
        serie.genero != null && topGeneros.contains(serie.genero)
    }
    
    // Para el explorador de episodios
    var selectedSeasonNum by remember { mutableStateOf<Int?>(null) }
    var seasonDetail by remember { mutableStateOf<TmdbSeasonDetailDto?>(null) }
    var loadingSeason by remember { mutableStateOf(false) }

    LaunchedEffect(selectedSeasonNum) {
        if (selectedSeasonNum != null && serie.tmdbId != null) {
            loadingSeason = true
            try {
                seasonDetail = viewModel.obtenerDetalleTemporada(serie.tmdbId, selectedSeasonNum!!)
            } catch (e: Exception) { /* Silently fail */ }
            loadingSeason = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val posterUrl = TmdbImages.urlPoster(serie.posterPath, "w342")
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(180.dp)
                    .height(270.dp)
                    .align(Alignment.CenterHorizontally),
            )
        }

        if (coincideConTusGustos) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("¡IA Predict: Es muy probable que te guste!", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(serie.titulo, style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    serie.fechaLanzamiento?.take(4)?.let { year ->
                        Text(year, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    serie.duracion?.let { duration ->
                        Text(duration, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            IconButton(onClick = {
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, "Mira esto: ${serie.titulo}. Lo tengo en mi lista de películas y series.")
                    type = "text/plain"
                }
                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }) {
                Icon(Icons.Default.Share, contentDescription = "Compartir")
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Valoración: ", style = MaterialTheme.typography.titleMedium)
            RatingBar(
                rating = serie.valoracion,
                onRatingChanged = { nuevaValoracion ->
                    onActualizar(serie.copy(valoracion = nuevaValoracion))
                }
            )
        }

        if (serie.mediaType == "tv") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Seguimiento de episodios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    if (serie.totalCapitulos != null && serie.totalCapitulos!! > 0) {
                        val progreso = serie.capituloActual.toFloat() / serie.totalCapitulos!!
                        Column {
                            LinearProgressIndicator(
                                progress = { progreso },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Progreso total", style = MaterialTheme.typography.labelSmall)
                                Text("${(progreso * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Temporada: ", modifier = Modifier.weight(1f))
                        IconButton(onClick = { if (serie.temporadaActual > 1) onActualizar(serie.copy(temporadaActual = serie.temporadaActual - 1)) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                        }
                        Text("${serie.temporadaActual}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { 
                            val max = serie.totalTemporadas ?: Int.MAX_VALUE
                            if (serie.temporadaActual < max) onActualizar(serie.copy(temporadaActual = serie.temporadaActual + 1)) 
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Episodio: ", modifier = Modifier.weight(1f))
                        IconButton(onClick = { if (serie.capituloActual > 1) onActualizar(serie.copy(capituloActual = serie.capituloActual - 1)) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                        }
                        Text("${serie.capituloActual}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { 
                            // Opcional: limitar al máximo de capítulos si lo tenemos
                            onActualizar(serie.copy(capituloActual = serie.capituloActual + 1)) 
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                        }
                    }

                    // Botón de compartir progreso
                    TextButton(
                        onClick = {
                            val shareText = "📺 Estoy viendo ${serie.titulo}. ¡Voy por la Temporada ${serie.temporadaActual}, Episodio ${serie.capituloActual}!"
                            val intent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Compartir progreso"))
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir mi progreso", style = MaterialTheme.typography.labelMedium)
                    }
                    
                    // Botón para explorar temporadas y episodios
                    TextButton(
                        onClick = {
                            // Abrimos la temporada actual por defecto si no hay ninguna seleccionada
                            selectedSeasonNum = if (selectedSeasonNum == null) serie.temporadaActual else null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.List, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (selectedSeasonNum == null) "Explorar episodios" else "Ocultar episodios", style = MaterialTheme.typography.labelMedium)
                    }

                    if (selectedSeasonNum != null) {
                        if (loadingSeason) {
                            Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            }
                        } else if (seasonDetail != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                seasonDetail?.episodes?.forEach { episode ->
                                    EpisodeItem(episode)
                                }
                            }
                        }
                    }
                    
                    if (serie.totalCapitulos != null) {
                        Text(
                            text = "Total capítulos en la serie: ${serie.totalCapitulos}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.End),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = notasLocal,
            onValueChange = { notasLocal = it },
            label = { Text("Mis notas y opinión") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 10,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            trailingIcon = {
                if (notasLocal.text.isNotEmpty()) {
                    IconButton(onClick = { notasLocal = TextFieldValue("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar notas")
                    }
                }
            },
            supportingText = {
                Text(
                    text = "${notasLocal.text.length} caracteres",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            tipo?.let { Text("Tipo: $it", style = MaterialTheme.typography.bodyMedium) }
            serie.genero?.let { Text("Género: $it", style = MaterialTheme.typography.bodyMedium) }
            Text("Añadido: $fechaAgregado", style = MaterialTheme.typography.bodySmall)
            Text("Lista actual: ${serie.lista}", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (serie.lista) {
            ListaTipo.POR_VER.name -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onActualizar(serie.copy(lista = ListaTipo.VIENDO.name)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Empezar a ver")
                    }
                    Button(
                        onClick = { onActualizar(serie.copy(lista = ListaTipo.VISTAS.name)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Marcar como Vista", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
            ListaTipo.VIENDO.name -> {
                Button(
                    onClick = { onActualizar(serie.copy(lista = ListaTipo.VISTAS.name)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("¡Terminada! Marcar como Vista")
                }
            }
            ListaTipo.VISTAS.name, ListaTipo.REVER.name -> {
                val esRever = serie.lista == ListaTipo.REVER.name
                Button(
                    onClick = {
                        val nuevaLista = if (esRever) ListaTipo.VISTAS.name else ListaTipo.REVER.name
                        onActualizar(serie.copy(lista = nuevaLista))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (esRever) "Ya la he vuelto a ver" else "Añadir a Rever")
                }
            }
        }

        OutlinedButton(
            onClick = onQuitar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Eliminar de mi catálogo")
        }

        serie.tmdbId?.let { tmdbId ->
            TextButton(
                onClick = { onVerDetalleTmdb(tmdbId, serie.mediaType ?: "movie") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ver temporadas, reparto y más")
            }
        }
    }
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    maxStars: Int = 5
) {
    Row {
        for (i in 1..maxStars) {
            IconButton(
                onClick = {
                    // Si pulsa la misma estrella que ya tiene, se podría resetear a 0 o dejar igual.
                    // Por ahora simplemente actualizamos al valor i.
                    onRatingChanged(i)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = "Estrella $i",
                    tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
