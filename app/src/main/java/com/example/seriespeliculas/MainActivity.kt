package com.example.seriespeliculas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seriespeliculas.ui.theme.SeriesPeliculasTheme
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.OutlinedTextField

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeriesPeliculasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MisListasScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
private enum class Lista { POR_VER, VISTAS, REVER }
@Composable
fun MisListasScreen(modifier: Modifier = Modifier) {
    var listaSeleccionada by remember { mutableStateOf(Lista.POR_VER) }
    var mostrandoBuscar by remember { mutableStateOf(false) }
    if (mostrandoBuscar) {
        BuscarScreen(
            modifier = modifier,
            onVolver = { mostrandoBuscar = false }
        )
        return
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Mis listas")
        Text(text = "Seleccionada: $listaSeleccionada")
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PestañaLista(
                texto = "Por ver",
                seleccionada = listaSeleccionada == Lista.POR_VER,
                onClick = { listaSeleccionada = Lista.POR_VER }
            )
            PestañaLista(
                texto = "Vistas",
                seleccionada = listaSeleccionada == Lista.VISTAS,
                onClick = { listaSeleccionada = Lista.VISTAS }
            )
            PestañaLista(
                texto = "Rever",
                seleccionada = listaSeleccionada == Lista.REVER,
                onClick = { listaSeleccionada = Lista.REVER }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (listaSeleccionada) {
            Lista.POR_VER -> Text("Aquí irán tus series/películas por ver.")
            Lista.VISTAS -> Text("Aquí irán las que ya viste.")
            Lista.REVER -> Text("Aquí irán las que quieres volver a ver.")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { mostrandoBuscar = true  },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Buscar")
        }
    }
}
@Composable
private fun PestañaLista(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    if (seleccionada) {
        Button(onClick = onClick) { Text(texto) }
    } else {
        OutlinedButton(onClick = onClick) { Text(texto) }
    }
}
@Composable
fun BuscarScreen(
    modifier: Modifier = Modifier,
    onVolver: () -> Unit
) {
    var texto by remember { mutableStateOf("") }

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
            Text("Buscar")
            OutlinedButton(onClick = onVolver) { Text("Volver") }
        }

        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Escribe una serie o película") },
            singleLine = true
        )

        Text("Resultados (de prueba):")
        Text("- $texto 1")
        Text("- $texto 2")
        Text("- $texto 3")
    }
}
@Preview(showBackground = true)
@Composable
fun MisListasPreview() {
    SeriesPeliculasTheme {
        MisListasScreen()
    }
}