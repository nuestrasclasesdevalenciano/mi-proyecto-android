package com.example.seriespeliculas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.seriespeliculas.DetalleTmdbUiState
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.data.tmdb.TmdbImages
import com.example.seriespeliculas.network.TmdbCastDto
import com.example.seriespeliculas.network.TmdbEpisodeDto
import com.example.seriespeliculas.network.TmdbSeasonDetailDto
import com.example.seriespeliculas.network.TmdbSeasonDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleTmdbScreen(
    tmdbId: Long,
    mediaType: String,
    viewModel: SeriesViewModel,
    onVolver: () -> Unit,
    onVerArtista: (Long) -> Unit = {},
    onVerDetalleTmdb: (Long, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf<DetalleTmdbUiState>(DetalleTmdbUiState.Cargando) }

    LaunchedEffect(tmdbId) {
        try {
            uiState = viewModel.obtenerDetalleTmdb(tmdbId, mediaType)
        } catch (e: Exception) {
            uiState = DetalleTmdbUiState.Error(e.message ?: "Error al cargar detalles")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when (val state = uiState) {
            is DetalleTmdbUiState.Cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DetalleTmdbUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                }
            }
            is DetalleTmdbUiState.Exito -> {
                DetalleContent(
                    state = state,
                    viewModel = viewModel,
                    onVerArtista = onVerArtista,
                    onVerSimilar = onVerDetalleTmdb,
                    onAñadir = {
                        viewModel.añadirResultadoTmdbConDetalles(state)
                        onVolver()
                    }
                )
            }
        }
    }
}

@Composable
private fun DetalleContent(
    state: DetalleTmdbUiState.Exito,
    viewModel: SeriesViewModel,
    onVerArtista: (Long) -> Unit,
    onVerSimilar: (Long, String) -> Unit = { _, _ -> },
    onAñadir: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var selectedSeasonNum by remember { mutableStateOf<Int?>(null) }
    var seasonDetail by remember { mutableStateOf<TmdbSeasonDetailDto?>(null) }
    var loadingSeason by remember { mutableStateOf(false) }

    LaunchedEffect(selectedSeasonNum) {
        selectedSeasonNum?.let { num ->
            loadingSeason = true
            try {
                seasonDetail = viewModel.obtenerDetalleTemporada(state.id, num)
            } catch (e: Exception) { /* Silently fail */ }
            loadingSeason = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Image (Backdrop)
        Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
            AsyncImage(
                model = TmdbImages.urlBackdrop(state.backdropPath, "w780"),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                            startY = 300f
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-40).dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.width(120.dp).height(180.dp)
                ) {
                    AsyncImage(
                        model = TmdbImages.urlPoster(state.posterPath, "w342"),
                        contentDescription = state.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.titulo,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.valoracion?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                Text(String.format("%.1f", it), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        state.fechaLanzamiento?.take(4)?.let { year ->
                            Text(year, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                        }
                        state.duracion?.let {
                            Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    state.director?.let {
                        Text(
                            text = "Dir: $it",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = state.generos.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (state.plataformas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Disponible en",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.plataformas) { plataforma ->
                        AsyncImage(
                            model = TmdbImages.urlPoster(plataforma.logoPath, "w92"),
                            contentDescription = plataforma.name,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAñadir,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Añadir")
                }

                state.trailerUrl?.let { url ->
                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            val chooser = android.content.Intent.createChooser(intent, "Ver Tráiler con...")
                            context.startActivity(chooser)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tráiler")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sinopsis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )

            if (!state.temporadas.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Temporadas (${state.numTemporadas})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("Pulsa una temporada para ver sus episodios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.temporadas) { season ->
                        SeasonItem(season, isSelected = selectedSeasonNum == season.seasonNumber) {
                            selectedSeasonNum = season.seasonNumber
                        }
                    }
                }
            }
            
            if (loadingSeason) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else if (seasonDetail != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Episodios: ${seasonDetail?.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                seasonDetail?.episodes?.forEach { episode ->
                    EpisodeItem(episode)
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.reparto.isNotEmpty()) {
                Text(
                    text = "Reparto principal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.reparto) { actor ->
                        ActorItem(actor, onClick = { onVerArtista(actor.id) })
                    }
                }
            }

            if (state.similares.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Títulos similares",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.similares) { item ->
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .clickable { onVerSimilar(item.id, item.mediaType) }
                        ) {
                            AsyncImage(
                                model = TmdbImages.urlPoster(item.posterPath, "w185"),
                                contentDescription = item.titulo,
                                modifier = Modifier
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.titulo,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun SeasonItem(season: TmdbSeasonDto, isSelected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column {
            AsyncImage(
                model = TmdbImages.urlPoster(season.posterPath, "w185"),
                contentDescription = season.name,
                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.height(200.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(8.dp)) {
                Text(season.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                Text("${season.episodeCount} episodios", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                season.airDate?.take(4)?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun EpisodeItem(episode: TmdbEpisodeDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = TmdbImages.urlBackdrop(episode.stillPath, "w300"),
                contentDescription = null,
                modifier = Modifier.size(100.dp, 60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "${episode.episodeNumber}. ${episode.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    episode.runtime?.let { Text("${it} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary) }
                    episode.airDate?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline) }
                }
                if (!episode.overview.isNullOrBlank()) {
                    Text(
                        text = episode.overview,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (!episode.guestStars.isNullOrEmpty()) {
                    Text(
                        text = "Invitados: " + episode.guestStars.take(3).joinToString { it.name },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ActorItem(actor: TmdbCastDto, onClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = TmdbImages.urlProfile(actor.profilePath, "w185"),
            contentDescription = actor.name,
            error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = actor.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
