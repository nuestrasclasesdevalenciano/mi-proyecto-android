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
import com.example.seriespeliculas.MisListasScreen
import com.example.seriespeliculas.SeriesViewModel
import com.example.seriespeliculas.ui.DetalleSerieScreen

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
                onIrBuscar = { navController.navigate(AppRoutes.Buscar) },
                onIrDetalle = { id -> navController.navigate(AppRoutes.detalle(id)) },
            )
        }
        composable(AppRoutes.Buscar) {
            val lista by viewModel.listaSeleccionada.collectAsStateWithLifecycle()
            BuscarScreen(
                listaDestinoEtiqueta = lista.etiqueta(),
                viewModel = viewModel,
                onVolver = {
                    viewModel.limpiarBusqueda()
                    navController.popBackStack()
                },
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
    }
}
