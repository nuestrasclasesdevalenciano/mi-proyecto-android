package com.example.seriespeliculas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.seriespeliculas.OrdenTipo
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.data.local.SerieEntity
import com.example.seriespeliculas.data.model.ListaTipo
import com.example.seriespeliculas.data.tmdb.TmdbImages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisListasScreen(
    viewModel: SeriesViewModel,
    onIrABuscar: () -> Unit,
    onVerDetalle: (Long) -> Unit,
    onVerDetalleTmdb: (Long, String) -> Unit,
    onVerEstadisticas: () -> Unit,
    onVerAcercaDe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listaSeleccionada by viewModel.listaSeleccionada.collectAsState()
    val series by viewModel.seriesEnLista.collectAsState()
    val consultaLista by viewModel.consultaLista.collectAsState()
    val ordenSeleccionado by viewModel.ordenSeleccionado.collectAsState()
    val generoSeleccionado by viewModel.generoSeleccionado.collectAsState()
    val todasLasSeries by viewModel.todasLasSeries.collectAsState()
    val tendencias by viewModel.tendencias.collectAsState()
    var mostrarMenuOrden by remember { mutableStateOf(false) }
    
    val haptic = LocalHapticFeedback.current
    var filtroValue by remember { mutableStateOf(TextFieldValue(consultaLista)) }
    val scope = rememberCoroutineScope()
    
    // Sincronizar el estado del filtro con el ViewModel (debounce implícito al escribir)
    LaunchedEffect(filtroValue.text) {
        if (filtroValue.text != consultaLista) {
            viewModel.buscarEnLista(filtroValue.text)
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val generosExistentes = todasLasSeries.mapNotNull { it.genero }.distinct().sorted()
    val recoIA by viewModel.recomendacionesIA.collectAsState()
    val mensajeIA by viewModel.mensajeIA.collectAsState()
    val cargandoIA by viewModel.cargandoIA.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (listaSeleccionada == ListaTipo.POR_VER && series.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("¿Qué veo?") },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    onClick = {
                        val random = series.random()
                        onVerDetalle(random.id)
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.refrescarTendencias()
                    delay(1200) // Animación un poco más larga para que se note
                    isRefreshing = false
                }
            },
            state = pullToRefreshState,
            modifier = Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 1. Search Bar
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { onIrABuscar() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Buscar películas o series...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                }

                // 2. AI Recommendations
                if (recoIA.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Recommend, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Asistente IA", 
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(onClick = { viewModel.generarRecomendacionesIA() }, modifier = Modifier.size(24.dp)) {
                                        if (cargandoIA) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.tertiary)
                                        } else {
                                            Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Text(mensajeIA, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
                                Spacer(Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(recoIA) { item ->
                                        Card(
                                            modifier = Modifier.width(100.dp).clickable { onVerDetalleTmdb(item.id, item.mediaType) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            AsyncImage(
                                                model = TmdbImages.urlPoster(item.posterPath, "w185"),
                                                contentDescription = item.titulo,
                                                modifier = Modifier.height(150.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Trending
                if (tendencias.isNotEmpty() && consultaLista.isEmpty() && generoSeleccionado == null) {
                    item {
                        Text("Tendencias de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(tendencias) { item ->
                                Card(
                                    modifier = Modifier.width(110.dp).clickable { onVerDetalleTmdb(item.id, item.mediaType) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    AsyncImage(
                                        model = TmdbImages.urlPoster(item.posterPath, "w185"),
                                        contentDescription = item.titulo,
                                        modifier = Modifier.height(160.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // 3. List Selector Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mis listas",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(onClick = onVerEstadisticas) {
                                Icon(Icons.Default.Info, contentDescription = "Estadísticas")
                            }
                            IconButton(onClick = onVerAcercaDe) {
                                Icon(Icons.Default.Settings, contentDescription = "Acerca de")
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ListaTipo.entries.forEach { tipo ->
                            val sel = listaSeleccionada == tipo
                            FilterChip(
                                selected = sel,
                                onClick = { viewModel.seleccionarLista(tipo) },
                                label = { Text(tipo.etiqueta(), style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. Genres and Filter
                if (generosExistentes.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            item {
                                FilterChip(
                                    selected = generoSeleccionado == null,
                                    onClick = { viewModel.seleccionarGenero(null) },
                                    label = { Text("Todos") }
                                )
                            }
                            items(generosExistentes) { genero ->
                                FilterChip(
                                    selected = generoSeleccionado == genero,
                                    onClick = { viewModel.seleccionarGenero(genero) },
                                    label = { Text(genero) }
                                )
                            }
                        }
                    }
                }

                // 5. List Search and Sort
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = filtroValue,
                            onValueChange = { filtroValue = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Filtrar mi lista...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (filtroValue.text.isNotEmpty()) {
                                    IconButton(onClick = { filtroValue = TextFieldValue("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpiar filtro")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Box {
                            IconButton(onClick = { mostrarMenuOrden = true }) { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                            DropdownMenu(expanded = mostrarMenuOrden, onDismissRequest = { mostrarMenuOrden = false }) {
                                OrdenTipo.entries.forEach { orden ->
                                    DropdownMenuItem(
                                        text = { Text(orden.etiqueta) },
                                        onClick = { viewModel.cambiarOrden(orden); mostrarMenuOrden = false },
                                        trailingIcon = { if (orden == ordenSeleccionado) Icon(Icons.Default.Check, null) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. The List items
                if (series.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.MovieFilter, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(16.dp))
                                Text(if (consultaLista.isEmpty()) "¡Tu lista está vacía!" else "Sin resultados", color = MaterialTheme.colorScheme.outline)
                                if (consultaLista.isEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = onIrABuscar) {
                                        Text("Buscar algo nuevo")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(series, key = { it.id }) { serie ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.eliminar(serie)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) 
                                    MaterialTheme.colorScheme.errorContainer else Color.Transparent
                                Box(Modifier.fillMaxSize().background(color).padding(end = 20.dp), contentAlignment = Alignment.CenterEnd) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        ) {
                            Surface(color = MaterialTheme.colorScheme.background) {
                                ItemSerie(serie, onEliminar = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.eliminar(serie) 
                                }, onClick = { onVerDetalle(serie.id) })
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ItemSerie(serie: SerieEntity, onEliminar: () -> Unit, onClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(context)
                    .data(TmdbImages.urlPoster(serie.posterPath, "w154"))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.size(60.dp, 90.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            if (serie.valoracion > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "★${serie.valoracion}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(serie.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = listOfNotNull(if (serie.mediaType == "movie") "Película" else "Serie", serie.genero).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (serie.mediaType == "tv") {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "T${serie.temporadaActual} : E${serie.capituloActual}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        IconButton(onClick = onEliminar) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) }
    }
}
