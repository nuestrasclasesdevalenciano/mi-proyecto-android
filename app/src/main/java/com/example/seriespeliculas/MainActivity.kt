package com.example.seriespeliculas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.seriespeliculas.data.SeriesRepository
import com.example.seriespeliculas.data.local.AppDatabase
import com.example.seriespeliculas.data.local.SerieEntity
import com.example.seriespeliculas.data.model.ListaTipo
import com.example.seriespeliculas.data.tmdb.TmdbImages
import com.example.seriespeliculas.data.tmdb.TmdbRepository
import com.example.seriespeliculas.network.TmdbRetrofit
import com.example.seriespeliculas.ui.DetalleSerieScreen
import com.example.seriespeliculas.ui.DetalleTmdbScreen
import com.example.seriespeliculas.ui.theme.SeriesPeliculasTheme
import java.util.Properties

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = BuildConfig.TMDB_API_KEY

        val db = AppDatabase.getInstance(applicationContext)
        val repository = SeriesRepository(db.serieDao())
        val tmdbApi = TmdbRetrofit.createApi(apiKey)
        val tmdbRepository = TmdbRepository(tmdbApi, apiKey.isNotBlank())
        
        enableEdgeToEdge()
        setContent {
            SeriesPeliculasTheme {
                val viewModel: SeriesViewModel = viewModel(
                    factory = SeriesViewModelFactory(repository, tmdbRepository, tmdbHabilitado = apiKey.isNotBlank())
                )
                
                var pantallaActual by remember { mutableStateOf<Pantalla>(Pantalla.MisListas) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (val p = pantallaActual) {
                        is Pantalla.MisListas -> MisListasScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding),
                            onIrABuscar = { pantallaActual = Pantalla.Buscar },
                            onVerDetalle = { id -> pantallaActual = Pantalla.Detalle(id) },
                            onVerDetalleTmdb = { id, type -> pantallaActual = Pantalla.DetalleTmdb(id, type) },
                            onVerEstadisticas = { pantallaActual = Pantalla.Estadisticas }
                        )
                        is Pantalla.Buscar -> BuscarScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding),
                            onVolver = { pantallaActual = Pantalla.MisListas },
                            onVerDetalleTmdb = { id, type -> pantallaActual = Pantalla.DetalleTmdb(id, type) }
                        )
                        is Pantalla.Detalle -> DetalleSerieScreen(
                            serieId = p.id,
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding),
                            onVolver = { pantallaActual = Pantalla.MisListas }
                        )
                        is Pantalla.DetalleTmdb -> DetalleTmdbScreen(
                            tmdbId = p.id,
                            mediaType = p.type,
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding),
                            onVolver = { pantallaActual = Pantalla.MisListas }
                        )
                        is Pantalla.Estadisticas -> EstadisticasScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding),
                            onVolver = { pantallaActual = Pantalla.MisListas }
                        )
                    }
                }
            }
        }
    }
    
    private val projectDir get() = java.io.File(applicationInfo.dataDir).parentFile

    sealed interface Pantalla {
        data object MisListas : Pantalla
        data object Buscar : Pantalla
        data object Estadisticas : Pantalla
        data class Detalle(val id: Long) : Pantalla
        data class DetalleTmdb(val id: Long, val type: String) : Pantalla
    }
}

@Composable
fun MisListasScreen(
    viewModel: SeriesViewModel,
    onIrABuscar: () -> Unit,
    onVerDetalle: (Long) -> Unit,
    onVerDetalleTmdb: (Long, String) -> Unit,
    onVerEstadisticas: () -> Unit,
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

    val generosExistentes = todasLasSeries.mapNotNull { it.genero }.distinct().sorted()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1. Search Bar entry point at the very top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onIrABuscar() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Buscar películas o series...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // 2. Trending
        if (tendencias.isNotEmpty()) {
            Text(
                text = "Tendencias de hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tendencias) { item ->
                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { onVerDetalleTmdb(item.id, item.mediaType ?: "movie") },
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            AsyncImage(
                                model = TmdbImages.urlPoster(item.posterPath, "w185"),
                                contentDescription = item.titulo,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = item.titulo,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. User's lists title and stats button
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
            IconButton(onClick = onVerEstadisticas) {
                Icon(Icons.Default.Info, contentDescription = "Estadísticas")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PestanaLista(
                texto = "Por ver",
                seleccionada = listaSeleccionada == ListaTipo.POR_VER,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.seleccionarLista(ListaTipo.POR_VER) }
            )
            PestanaLista(
                texto = "Vistas",
                seleccionada = listaSeleccionada == ListaTipo.VISTAS,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.seleccionarLista(ListaTipo.VISTAS) }
            )
            PestanaLista(
                texto = "Rever",
                seleccionada = listaSeleccionada == ListaTipo.REVER,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.seleccionarLista(ListaTipo.REVER) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (generosExistentes.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    AssistChip(
                        onClick = { viewModel.seleccionarGenero(null) },
                        label = { Text("Todos") },
                        colors = if (generoSeleccionado == null) AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else AssistChipDefaults.assistChipColors()
                    )
                }
                items(generosExistentes) { genero ->
                    AssistChip(
                        onClick = { viewModel.seleccionarGenero(genero) },
                        label = { Text(genero) },
                        colors = if (generoSeleccionado == genero) AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else AssistChipDefaults.assistChipColors()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = consultaLista,
                onValueChange = { viewModel.buscarEnLista(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar en esta lista...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            Box {
                IconButton(onClick = { mostrarMenuOrden = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Ordenar")
                }
                DropdownMenu(
                    expanded = mostrarMenuOrden,
                    onDismissRequest = { mostrarMenuOrden = false }
                ) {
                    OrdenTipo.entries.forEach { orden ->
                        DropdownMenuItem(
                            text = { Text(orden.etiqueta) },
                            onClick = {
                                viewModel.cambiarOrden(orden)
                                mostrarMenuOrden = false
                            },
                            trailingIcon = {
                                if (orden == ordenSeleccionado) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (series.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    if (consultaLista.isEmpty()) "No hay elementos en esta lista."
                    else "No se encontraron resultados para '$consultaLista'."
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(series) { serie ->
                    ItemSerie(
                        serie = serie,
                        onEliminar = { viewModel.eliminar(serie) },
                        onClick = { onVerDetalle(serie.id) }
                    )
                    HorizontalDivider()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Se han movido arriba
        }
    }
}

@Composable
fun EstadisticasScreen(
    viewModel: SeriesViewModel,
    modifier: Modifier = Modifier,
    onVolver: () -> Unit
) {
    val todas by viewModel.todasLasSeries.collectAsState()
    
    val total = todas.size
    val vistas = todas.count { it.lista == ListaTipo.VISTAS.name }
    val porVer = todas.count { it.lista == ListaTipo.POR_VER.name }
    val rever = todas.count { it.lista == ListaTipo.REVER.name }
    
    val mediaValoracion = if (todas.any { it.valoracion > 0 }) {
        todas.filter { it.valoracion > 0 }.map { it.valoracion }.average()
    } else 0.0

    val top5 = todas.filter { it.valoracion > 0 }
        .sortedByDescending { it.valoracion }
        .take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Estadísticas",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onVolver) { Text("Cerrar") }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total en biblioteca", style = MaterialTheme.typography.titleMedium)
                    Text("$total títulos", style = MaterialTheme.typography.headlineLarge)
                }
            }
        }

        item {
            Text("Progreso de visionado", style = MaterialTheme.typography.titleLarge)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProgresoItem("Vistas", vistas, total, Color(0xFF4CAF50))
                ProgresoItem("Pendientes", porVer, total, Color(0xFF2196F3))
                ProgresoItem("Para repetir", rever, total, Color(0xFFFF9800))
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Valoración media", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            String.format("%.1f", mediaValoracion),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                    }
                }
            }
        }

        if (top5.isNotEmpty()) {
            item {
                Text("Mi Top 5", style = MaterialTheme.typography.titleLarge)
            }

            items(top5) { serie ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = TmdbImages.urlPoster(serie.posterPath, "w92"),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp, 60.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(serie.titulo, style = MaterialTheme.typography.titleMedium)
                            Row {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = if (index < serie.valoracion) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (index < serie.valoracion) Color(0xFFFFB300) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Logros", style = MaterialTheme.typography.titleLarge)
        }

        val logros = listOf(
            LogroData("Cinéfilo Novato", "Añade 10 títulos", total >= 10),
            LogroData("Crítico Experto", "Valora 5 títulos", todas.count { it.valoracion > 0 } >= 5),
            LogroData("Maratoniano", "Ve 5 series/pelis", vistas >= 5),
            LogroData("Exigente", "Da 5 estrellas a 3 títulos", todas.count { it.valoracion == 5 } >= 3)
        )

        items(logros) { logro ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (logro.desbloqueado) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = if (logro.desbloqueado) Color(0xFF4CAF50) else Color.Gray
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            logro.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (logro.desbloqueado) Color(0xFF2E7D32) else Color.Gray
                        )
                        Text(logro.descripcion, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemSeriePreview() {
    SeriesPeliculasTheme {
        ItemSerie(
            serie = SerieEntity(
                titulo = "Ejemplo de Película",
                lista = ListaTipo.POR_VER.name,
                genero = "Acción",
                valoracion = 4,
                mediaType = "movie"
            ),
            onEliminar = {},
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProgresoItemPreview() {
    SeriesPeliculasTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProgresoItem("Vistas", 5, 10, Color(0xFF4CAF50))
            ProgresoItem("Pendientes", 3, 10, Color(0xFF2196F3))
        }
    }
}

data class LogroData(val titulo: String, val descripcion: String, val desbloqueado: Boolean)

@Composable
fun ProgresoItem(etiqueta: String, cantidad: Int, total: Int, color: Color) {
    val progreso = if (total > 0) cantidad.toFloat() / total else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(etiqueta)
            Text("$cantidad ($total)")
        }
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ItemSerie(
    serie: SerieEntity,
    onEliminar: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = TmdbImages.urlPoster(serie.posterPath, "w92"),
            contentDescription = null,
            modifier = Modifier.size(60.dp, 90.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = serie.titulo, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                val subtexto = listOfNotNull(
                    if (serie.mediaType == "movie") "Película" else "Serie",
                    serie.genero
                ).joinToString(" • ")
                Text(
                    text = subtexto,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
            if (serie.valoracion > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < serie.valoracion) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (index < serie.valoracion) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
        IconButton(onClick = onEliminar) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
        }
    }
}

@Composable
private fun PestanaLista(
    texto: String,
    seleccionada: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (seleccionada) {
        Button(onClick = onClick, modifier = modifier) { Text(texto) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(texto) }
    }
}

@Composable
fun BuscarScreen(
    viewModel: SeriesViewModel,
    modifier: Modifier = Modifier,
    onVolver: () -> Unit,
    onVerDetalleTmdb: (Long, String) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val buscarState by viewModel.buscarState.collectAsState()

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
            value = texto,
            onValueChange = { 
                texto = it
                viewModel.buscar(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Escribe una serie o película") },
            singleLine = true
        )

        when (val state = buscarState) {
            is BuscarUiState.Cargando -> Text("Cargando...")
            is BuscarUiState.Error -> Text("Error: ${state.mensaje}")
            is BuscarUiState.Exito -> {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = TmdbImages.urlPoster(item.posterPath, "w92"),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp, 75.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.titulo, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (item.mediaType == "movie") "Película" else "Serie",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(onClick = { 
                                onVerDetalleTmdb(item.id, item.mediaType ?: "movie")
                            }) {
                                Text("Ver")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
            BuscarUiState.Inicial -> Text("Escribe al menos 2 caracteres para buscar.")
        }
    }
}
