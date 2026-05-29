package com.example.seriespeliculas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.data.model.ListaTipo
import com.example.seriespeliculas.data.tmdb.TmdbImages

@Composable
fun EstadisticasScreen(
    viewModel: SeriesViewModel,
    modifier: Modifier = Modifier,
    onVolver: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val todas by viewModel.todasLasSeries.collectAsState()
    
    val total = todas.size
    val vistas = todas.count { it.lista == ListaTipo.VISTAS.name }
    val viendo = todas.count { it.lista == ListaTipo.VIENDO.name }
    val porVer = todas.count { it.lista == ListaTipo.POR_VER.name }
    val rever = todas.count { it.lista == ListaTipo.REVER.name }
    
    val minutosVistos = vistas * 105 // Media de 1h 45min por título
    val horasVistas = minutosVistos / 60
    val diasVistos = horasVistas / 24

    val mediaValoracion = if (todas.any { it.valoracion > 0 }) {
        todas.filter { it.valoracion > 0 }.map { it.valoracion }.average()
    } else 0.0

    val nivelCinefilo = when {
        vistas >= 50 -> "Leyenda del Cine 🏆"
        vistas >= 20 -> "Crítico Experto 🎬"
        vistas >= 10 -> "Cinéfilo Curioso 🍿"
        else -> "Espectador Novato 🌱"
    }

    val top5 = todas.filter { it.valoracion > 0 }
        .sortedByDescending { it.valoracion }
        .take(5)

    val generosStats = todas.mapNotNull { it.genero }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
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
                    "Mi Perfil Cinéfilo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val resumen = "🎬 Mi Perfil en Series y Películas:\n" +
                                "- Total: $total títulos\n" +
                                "- Vistas: $vistas\n" +
                                "- Media: ${String.format("%.1f", mediaValoracion)} ★\n" +
                                "- Tiempo: ${horasVistas}h\n" +
                                "¡Organiza tu cine tú también!"
                    val intent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, resumen)
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Compartir mi perfil"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir estadísticas")
                }
                IconButton(onClick = {
                    val listaTexto = todas.joinToString("\n") { "- ${it.titulo} (${if (it.valoracion > 0) "${it.valoracion}★" else "Sin puntuar"})" }
                    val shareText = "🍿 Mi lista de Series y Películas:\n\n$listaTexto"
                    val intent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Compartir mi lista completa"))
                }) {
                    Icon(Icons.Default.List, contentDescription = "Compartir lista completa")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Tu Nivel", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text(nivelCinefilo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Total en biblioteca", style = MaterialTheme.typography.labelSmall)
                        Text("$total títulos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    if (horasVistas > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tiempo visto", style = MaterialTheme.typography.labelSmall)
                            Text("${horasVistas}h", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (diasVistos > 0) Text("${diasVistos} días", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Text("Distribución por Listas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProgresoItem("Vistas", vistas, total, Color(0xFF4CAF50))
                    ProgresoItem("Viendo", viendo, total, Color(0xFF9C27B0))
                    ProgresoItem("Pendientes", porVer, total, Color(0xFF2196F3))
                    ProgresoItem("Para repetir", rever, total, Color(0xFFFF9800))
                }
            }
        }

        if (generosStats.isNotEmpty()) {
            item {
                Text("Tus Géneros Favoritos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val maxCount = generosStats.first().second
                        generosStats.forEach { (genero, count) ->
                            Column {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(genero, style = MaterialTheme.typography.bodyMedium)
                                    Text("$count", style = MaterialTheme.typography.labelSmall)
                                }
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / maxCount },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Valoración media", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            String.format("%.1f", mediaValoracion),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        if (top5.isNotEmpty()) {
            item {
                Text("Mi Top 5 Personal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                            modifier = Modifier.size(40.dp, 60.dp).clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(serie.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
            Text("Mis Logros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                            fontWeight = FontWeight.Bold,
                            color = if (logro.desbloqueado) Color(0xFF2E7D32) else Color.Gray
                        )
                        Text(logro.descripcion, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

data class LogroData(val titulo: String, val descripcion: String, val desbloqueado: Boolean)

@Composable
fun ProgresoItem(etiqueta: String, cantidad: Int, total: Int, color: Color) {
    val progreso = if (total > 0) cantidad.toFloat() / total else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
            Text("$cantidad", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
