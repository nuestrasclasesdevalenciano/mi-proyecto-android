package com.example.seriespeliculas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.seriespeliculas.BuscarUiState
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.data.tmdb.TmdbImages
import com.example.seriespeliculas.data.tmdb.TmdbSearchItem

@Composable
fun BuscarScreen(
    viewModel: SeriesViewModel,
    modifier: Modifier = Modifier,
    onVolver: () -> Unit,
    onVerDetalleTmdb: (Long, String) -> Unit
) {
    var textoValue by remember { mutableStateOf(TextFieldValue("")) }
    val buscarState by viewModel.buscarState.collectAsState()
    val proximos by viewModel.proximos.collectAsState()
    val topGeneros by viewModel.topGeneros.collectAsState()
    val recomendaciones by viewModel.recomendacionesGenerales.collectAsState()
    val recientes by viewModel.busquedasRecientes.collectAsState()
    
    val haptic = LocalHapticFeedback.current

    val curiosidad = remember {
        listOf(
            "¿Sabías que 'Pulp Fiction' solo costó 8 millones de dólares?",
            "El rugido del Rey León era en realidad el de un tigre.",
            "Alfred Hitchcock no tenía ombligo.",
            "La película más larga de la historia dura 857 horas.",
            "En 'Interstellar', los campos de maíz fueron reales, no CGI.",
            "'I'll be back' de Terminator fue una improvisación.",
            "Titanic costó más que la construcción del barco real.",
            "Viggo Mortensen se rompió dos dedos del pie pateando un casco en El Señor de los Anillos."
        ).random()
    }

    LaunchedEffect(topGeneros) {
        if (topGeneros.isNotEmpty()) {
            viewModel.cargarRecomendacionesPorGenero(topGeneros.first())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Buscar en TMDB", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = {
                viewModel.limpiarBusqueda()
                onVolver()
            }) { Text("Volver") }
        }

        OutlinedTextField(
            value = textoValue,
            onValueChange = { 
                textoValue = it
                viewModel.buscar(it.text)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Serie, película, actor o director") },
            placeholder = { Text("Ej: Christopher Nolan, Brad Pitt...") },
            singleLine = true,
            trailingIcon = {
                if (textoValue.text.isNotEmpty()) {
                    IconButton(onClick = { 
                        textoValue = TextFieldValue("")
                        viewModel.limpiarBusqueda()
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            }
        )

        when (val state = buscarState) {
            is BuscarUiState.Cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is BuscarUiState.Error -> {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(state.mensaje, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            is BuscarUiState.Exito -> {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items) { item ->
                        ItemBusqueda(item) { id, type ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onVerDetalleTmdb(id, type)
                        }
                        HorizontalDivider()
                    }
                }
            }
            BuscarUiState.Inicial -> {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    
                    // Película destacada (la primera de tendencias)
                    val destacado = proximos.firstOrNull()
                    if (destacado != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onVerDetalleTmdb(destacado.id, destacado.mediaType) }
                        ) {
                            AsyncImage(
                                model = TmdbImages.urlBackdrop(destacado.posterPath, "w780"),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text("ESTRENO DESTACADO", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                Text(destacado.titulo, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Búsquedas recientes
                    if (recientes.isNotEmpty()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Búsquedas recientes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                                TextButton(onClick = { viewModel.borrarHistorialBusqueda() }) {
                                    Text("Borrar", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                recientes.forEach { query ->
                                    SuggestionChip(
                                        onClick = { 
                                            textoValue = TextFieldValue(query)
                                            viewModel.buscar(query)
                                        },
                                        label = { Text(query) }
                                    )
                                }
                            }
                        }
                    }

                    // Trivia
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(curiosidad, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Recomendaciones por género
                    if (recomendaciones.isNotEmpty() && topGeneros.isNotEmpty()) {
                        Column {
                            Text("Porque te gusta el género ${topGeneros.first()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(recomendaciones) { item ->
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

                    // Próximos
                    if (proximos.isNotEmpty()) {
                        Column {
                            Text("Próximos lanzamientos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            proximos.take(5).forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { onVerDetalleTmdb(item.id, item.mediaType) }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = TmdbImages.urlPoster(item.posterPath, "w92"),
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp, 60.dp).clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(item.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                        Text("Muy pronto en cines", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemBusqueda(item: com.example.seriespeliculas.data.tmdb.TmdbSearchItem, onClick: (Long, String) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick(item.id, item.mediaType) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(context)
                .data(TmdbImages.urlPoster(item.posterPath, "w92"))
                .crossfade(true)
                .build(),
            contentDescription = null,
            error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.size(50.dp, 75.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.titulo, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (item.mediaType == "movie") "Película" else "Serie",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                // Puntuación de la comunidad
                item.fechaLanzamiento?.take(4)?.let { year ->
                    Text(year, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                item.puntuacion?.let { score ->
                    if (score > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                            Text(String.format("%.1f", score), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
    }
}
