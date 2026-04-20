package com.example.seriespeliculas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.seriespeliculas.BuscarScreen
import com.example.seriespeliculas.EstadisticasScreen
import com.example.seriespeliculas.MisListasScreen
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.ui.DetalleSerieScreen
import com.example.seriespeliculas.ui.DetalleTmdbScreen

@Composable
fun SeriesNavHost(
    viewModel: SeriesViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

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
            )
        }
        composable(AppRoutes.Estadisticas) {
            EstadisticasScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
