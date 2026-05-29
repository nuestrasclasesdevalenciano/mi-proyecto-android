package com.example.seriespeliculas.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.seriespeliculas.SeriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcercaDeScreen(
    viewModel: SeriesViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val temaOscuro by viewModel.temaOscuro.collectAsState()
    val idioma by viewModel.idioma.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var mostrarDialogoLimpiar by remember { mutableStateOf(false) }

    // Launcher para importar archivos
    val launcherImportar = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importarDatos(it, context) }
    }

    if (mostrarDialogoLimpiar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLimpiar = false },
            title = { Text("¿Limpiar catálogo?") },
            text = { Text("Se eliminarán todas las películas y series de tus listas. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.limpiarCatalogo()
                        mostrarDialogoLimpiar = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Borrar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoLimpiar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsHeader("Apariencia")
            
            SettingsOption(
                title = "Tema de la aplicación",
                subtitle = when(temaOscuro) {
                    true -> "Modo oscuro activado"
                    false -> "Modo claro activado"
                    null -> "Siguiendo el sistema"
                },
                icon = Icons.Default.Brightness6
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SegmentedButton(
                        selected = temaOscuro == false,
                        onClick = { viewModel.cambiarTema(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) { Text("Claro") }
                    SegmentedButton(
                        selected = temaOscuro == true,
                        onClick = { viewModel.cambiarTema(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) { Text("Oscuro") }
                    SegmentedButton(
                        selected = temaOscuro == null,
                        onClick = { viewModel.cambiarTema(null) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) { Text("Auto") }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsHeader("Contenido")
            
            SettingsOption(
                title = "Idioma de información",
                subtitle = if (idioma == "es-ES") "Español (España)" else "English (USA)",
                icon = Icons.Default.Language
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SegmentedButton(
                        selected = idioma == "es-ES",
                        onClick = { viewModel.cambiarIdioma("es-ES") },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Español") }
                    SegmentedButton(
                        selected = idioma == "en-US",
                        onClick = { viewModel.cambiarIdioma("en-US") },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("English") }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsHeader("Copia de Seguridad")

            ListItem(
                headlineContent = { Text("Exportar Catálogo") },
                supportingContent = { Text("Guarda tus listas en un archivo para compartir o restaurar.") },
                leadingContent = { Icon(Icons.Default.CloudUpload, null) },
                modifier = Modifier.clickable { viewModel.exportarDatos(context) }
            )

            ListItem(
                headlineContent = { Text("Importar Catálogo") },
                supportingContent = { Text("Recupera tus datos desde un archivo guardado.") },
                leadingContent = { Icon(Icons.Default.CloudDownload, null) },
                modifier = Modifier.clickable { launcherImportar.launch("application/json") }
            )

            Spacer(Modifier.height(24.dp))
            
            // Sección Acerca de con un toque más moderno
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PlayArrow, 
                                contentDescription = null, 
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Series & Películas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "v1.2 Premium Edition",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        text = "Miguel Ángel Molpeceres López",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Desarrollador Android",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            SettingsHeader("Legal y Soporte")
            
            ListItem(
                headlineContent = { Text("Términos de TMDB") },
                supportingContent = { Text("Esta app usa la API de TMDB pero no está certificada por ellos.") },
                leadingContent = { Icon(Icons.Default.Info, null) },
                modifier = Modifier.clickable { 
                    uriHandler.openUri("https://www.themoviedb.org/")
                }
            )

            ListItem(
                headlineContent = { Text("Contactar Soporte") },
                supportingContent = { Text("miguelm227@gmail.com") },
                leadingContent = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.clickable { 
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:miguelm227@gmail.com")
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Soporte - App Series y Películas")
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Enviar correo"))
                }
            )

            ListItem(
                headlineContent = { Text("Compartir la aplicación") },
                leadingContent = { Icon(Icons.Default.Share, null) },
                modifier = Modifier.clickable { 
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "¡Prueba esta app de Series y Películas! Es genial.")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsHeader("Avanzado")

            ListItem(
                headlineContent = { Text("Borrar todo el catálogo", color = MaterialTheme.colorScheme.error) },
                supportingContent = { Text("Elimina permanentemente todas tus listas") },
                leadingContent = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { mostrarDialogoLimpiar = true }
            )

            Spacer(Modifier.height(32.dp))
            Text(
                "© 2025 Made with ❤️ in Spain",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) },
            leadingContent = { Icon(icon, null) }
        )
        content()
    }
}
