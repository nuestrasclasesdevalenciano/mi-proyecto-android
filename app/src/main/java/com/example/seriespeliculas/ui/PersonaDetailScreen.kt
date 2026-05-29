package com.example.seriespeliculas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.seriespeliculas.DetallePersonaUiState
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.data.tmdb.TmdbImages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaDetailScreen(
    personaId: Long,
    viewModel: SeriesViewModel,
    onVolver: () -> Unit,
    onVerDetalleTmdb: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf<DetallePersonaUiState>(DetallePersonaUiState.Cargando) }

    LaunchedEffect(personaId) {
        try {
            uiState = viewModel.obtenerDetallePersona(personaId)
        } catch (e: Exception) {
            uiState = DetallePersonaUiState.Error(e.message ?: "Error al cargar detalles")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState is DetallePersonaUiState.Exito) (uiState as DetallePersonaUiState.Exito).nombre else "Artista") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when (val state = uiState) {
            is DetallePersonaUiState.Cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DetallePersonaUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                }
            }
            is DetallePersonaUiState.Exito -> {
                PersonaContent(state, onVerDetalleTmdb, Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
private fun PersonaContent(
    state: DetallePersonaUiState.Exito,
    onVerDetalleTmdb: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = TmdbImages.urlProfile(state.fotoPath, "h632"),
                contentDescription = state.nombre,
                modifier = Modifier
                    .width(150.dp)
                    .height(225.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(state.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                state.fechaNacimiento?.let {
                    Text("Nacimiento: $it", style = MaterialTheme.typography.bodyMedium)
                }
                state.lugarNacimiento?.let {
                    Text("Lugar: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!state.biografia.isNullOrBlank()) {
            Text("Biografía", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.biografia, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (state.creditos.isNotEmpty()) {
            Text("Conocido por", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(state.creditos.take(20)) { item ->
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .clickable { onVerDetalleTmdb(item.id, item.mediaType) }
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
                            item.titulo,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
