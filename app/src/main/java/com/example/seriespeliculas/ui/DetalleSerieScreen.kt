package com.example.seriespeliculas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.data.local.SerieEntity
import com.example.seriespeliculas.data.model.ListaTipo
import com.example.seriespeliculas.data.tmdb.TmdbImages
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private sealed interface DetalleCarga {
    data object Cargando : DetalleCarga
    data class Encontrada(val serie: SerieEntity) : DetalleCarga
    data object NoEncontrada : DetalleCarga
}

@Composable
fun DetalleSerieScreen(
    serieId: Long,
    viewModel: SeriesViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by produceState<DetalleCarga>(
        initialValue = DetalleCarga.Cargando,
        key1 = serieId,
    ) {
        value = when (val s = viewModel.cargarSerie(serieId)) {
            null -> DetalleCarga.NoEncontrada
            else -> DetalleCarga.Encontrada(s)
        }
    }

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

        when (val e = estado) {
            DetalleCarga.Cargando -> Text(
                text = "Cargando…",
                style = MaterialTheme.typography.bodyLarge,
            )

            DetalleCarga.NoEncontrada -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "No se ha encontrado ese título en tu catálogo.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedButton(onClick = onVolver) { Text("Volver") }
            }

            is DetalleCarga.Encontrada -> DetalleContenido(
                serie = e.serie,
                onActualizar = { viewModel.actualizar(it) },
                onQuitar = {
                    viewModel.eliminar(e.serie)
                    onVolver()
                },
            )
        }
    }
}

@Composable
private fun DetalleContenido(
    serie: SerieEntity,
    onActualizar: (SerieEntity) -> Unit,
    onQuitar: () -> Unit,
) {
    val scroll = rememberScrollState()
    val fecha = remember(serie.creadoEn) {
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            .format(Date(serie.creadoEn))
    }
    
    val tipo = when (serie.mediaType) {
        "movie" -> "Película"
        "tv" -> "Serie"
        else -> null
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

        Text(serie.titulo, style = MaterialTheme.typography.headlineMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Valoración: ", style = MaterialTheme.typography.titleMedium)
            RatingBar(
                rating = serie.valoracion,
                onRatingChanged = { nuevaValoracion ->
                    onActualizar(serie.copy(valoracion = nuevaValoracion))
                }
            )
        }

        OutlinedTextField(
            value = serie.notas,
            onValueChange = { nuevasNotas ->
                onActualizar(serie.copy(notas = nuevasNotas))
            },
            label = { Text("Mis notas") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            tipo?.let { Text("Tipo: $it", style = MaterialTheme.typography.bodyMedium) }
            serie.genero?.let { Text("Género: $it", style = MaterialTheme.typography.bodyMedium) }
            Text("Añadido: $fecha", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onQuitar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Quitar de la lista")
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
                onClick = { onRatingChanged(i) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Estrella $i",
                    tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
