package com.example.seriespeliculas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.ui.AcercaDeScreen
import com.example.seriespeliculas.ui.BuscarScreen
import com.example.seriespeliculas.ui.DetalleSerieScreen
import com.example.seriespeliculas.ui.DetalleTmdbScreen
import androidx.navigation.NavHostController
import com.example.seriespeliculas.ui.EstadisticasScreen
import com.example.seriespeliculas.ui.MisListasScreen
import com.example.seriespeliculas.ui.PersonaDetailScreen

@Composable
fun SeriesNavHost(
    viewModel: SeriesViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Listas,
        modifier = modifier,
    ) {
        composable(AppRoutes.Listas) {
            MisListasScreen(
                viewModel = viewModel,
                onIrABuscar = { navController.navigate(AppRoutes.Buscar) },
                onVerDetalle = { id -> navController.navigate(AppRoutes.detalle(id)) },
                onVerDetalleTmdb = { id, type -> navController.navigate(AppRoutes.detalleTmdb(id, type)) },
                onVerEstadisticas = { navController.navigate(AppRoutes.Estadisticas) },
                onVerAcercaDe = { navController.navigate(AppRoutes.AcercaDe) }
            )
        }
        composable(AppRoutes.Buscar) {
            BuscarScreen(
                viewModel = viewModel,
                onVolver = {
                    viewModel.limpiarBusqueda()
                    navController.popBackStack()
                },
                onVerDetalleTmdb = { id, type -> navController.navigate(AppRoutes.detalleTmdb(id, type)) }
            )
        }
        composable(
            route = AppRoutes.Detalle,
            arguments = listOf(
                navArgument("serieId") { type = NavType.LongType },
            ),
        ) { entry ->
            val serieId = entry.arguments!!.getLong("serieId")
            DetalleSerieScreen(
                serieId = serieId,
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onVerDetalleTmdb = { tmdbId, type ->
                    navController.navigate(AppRoutes.detalleTmdb(tmdbId, type))
                }
            )
        }
        composable(
            route = AppRoutes.DetalleTmdb,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType },
            ),
        ) { entry ->
            val id = entry.arguments!!.getLong("id")
            val type = entry.arguments!!.getString("type") ?: "movie"
            DetalleTmdbScreen(
                tmdbId = id,
                mediaType = type,
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onVerArtista = { artistaId ->
                    navController.navigate(AppRoutes.detallePersona(artistaId))
                },
                onVerDetalleTmdb = { tmdbId, mediaType ->
                    navController.navigate(AppRoutes.detalleTmdb(tmdbId, mediaType)) {
                        // Evitar pilas infinitas de detalles si se navega mucho por similares
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoutes.Estadisticas) {
            EstadisticasScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(
            route = AppRoutes.DetallePersona,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType }
            )
        ) { entry ->
            val id = entry.arguments!!.getLong("id")
            PersonaDetailScreen(
                personaId = id,
                viewModel = viewModel,
                onVolver = { navController.popBackStack() },
                onVerDetalleTmdb = { tmdbId, type ->
                    navController.navigate(AppRoutes.detalleTmdb(tmdbId, type))
                }
            )
        }
        composable(AppRoutes.AcercaDe) {
            AcercaDeScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
