package com.example.seriespeliculas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.seriespeliculas.data.SeriesRepository
import com.example.seriespeliculas.data.local.AppDatabase
import com.example.seriespeliculas.data.local.UserPreferences
import com.example.seriespeliculas.data.tmdb.TmdbRepository
import com.example.seriespeliculas.navigation.AppRoutes
import com.example.seriespeliculas.navigation.SeriesNavHost
import com.example.seriespeliculas.network.TmdbRetrofit
import com.example.seriespeliculas.ui.theme.SeriesPeliculasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = BuildConfig.TMDB_API_KEY

        val db = AppDatabase.getInstance(applicationContext)
        val repository = SeriesRepository(db.serieDao())
        val userPreferences = UserPreferences(applicationContext)
        val tmdbApi = TmdbRetrofit.createApi(apiKey)
        val tmdbRepository = TmdbRepository(tmdbApi, apiKey.isNotBlank())
        
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val viewModel: SeriesViewModel = viewModel(
                factory = SeriesViewModelFactory(
                    seriesRepository = repository,
                    tmdbRepository = tmdbRepository,
                    userPreferences = userPreferences,
                    tmdbHabilitado = apiKey.isNotBlank()
                )
            )
            val temaOscuro by viewModel.temaOscuro.collectAsState()
            
            SeriesPeliculasTheme(
                darkTheme = temaOscuro ?: androidx.compose.foundation.isSystemInDarkTheme()
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        // Solo mostramos la barra en las pantallas principales
                        val screens = listOf(AppRoutes.Listas, AppRoutes.Buscar, AppRoutes.Estadisticas)
                        val showBottomBar = screens.any { it == currentDestination?.route }

                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                                    label = { Text("Listas") },
                                    selected = currentDestination?.hierarchy?.any { it.route == AppRoutes.Listas } == true,
                                    onClick = {
                                        navController.navigate(AppRoutes.Listas) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    label = { Text("Descubrir") },
                                    selected = currentDestination?.hierarchy?.any { it.route == AppRoutes.Buscar } == true,
                                    onClick = {
                                        navController.navigate(AppRoutes.Buscar) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                                    label = { Text("Estadísticas") },
                                    selected = currentDestination?.hierarchy?.any { it.route == AppRoutes.Estadisticas } == true,
                                    onClick = {
                                        navController.navigate(AppRoutes.Estadisticas) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    SeriesNavHost(
                        viewModel = viewModel,
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
