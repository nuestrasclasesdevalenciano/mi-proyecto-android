package com.example.seriespeliculas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.seriespeliculas.DetalleTmdbUiState
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.data.tmdb.TmdbImages
import com.example.seriespeliculas.data.tmdb.TmdbSearchItem
import com.example.seriespeliculas.network.TmdbCastDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleTmdbScreen(
    tmdbId: Long,
    mediaType: String,
    viewModel: SeriesViewModel,
    onVolver: () -> Unit,
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
                    onAñadir = {
                        viewModel.añadirResultadoTmdb(
                            TmdbSearchItem(
                                id = state.id,
                                titulo = state.titulo,
                                posterPath = state.posterPath,
                                overview = state.descripcion,
                                mediaType = state.mediaType,
                                generoIds = emptyList() // No es crítico aquí ya que pasamos el género mapeado
                            )
                        )
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
    onAñadir: () -> Unit
) {
    val scrollState = rememberScrollState()

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
                    Text(
                        text = state.generos.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAñadir,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Añadir a mi lista")
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
                    items(state.reparto.take(10)) { actor ->
                        ActorItem(actor)
                    }
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun ActorItem(actor: TmdbCastDto) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        AsyncImage(
            model = TmdbImages.urlProfile(actor.profilePath, "w185"),
            contentDescription = actor.name,
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
