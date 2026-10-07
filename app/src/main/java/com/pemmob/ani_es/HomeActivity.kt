package com.pemmob.ani_es

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pemmob.ani_es.ui.screen.DetailScreen
import com.pemmob.ani_es.ui.screen.HomeScreen
import com.pemmob.ani_es.ui.theme.AniesTheme
import com.pemmob.ani_es.ui.viewmodel.AnimeViewModel

/**
 * Launcher Activity utama (dan satu-satunya) aplikasi Ani-es.
 * Menampung NavHost dengan 2 rute (Home dan Detail) serta membagikan
 * satu instance AnimeViewModel agar data pencarian tetap utuh saat kembali dari detail.
 */
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AniesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AniEsAppNavigation()
                }
            }
        }
    }
}

/**
 * Komponen Navigasi Utama:
 * - Rute "home": Menampilkan daftar pencarian & populer
 * - Rute "detail/{malId}": Menampilkan informasi detail anime
 */
@Composable
fun AniEsAppNavigation(
    viewModel: AnimeViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable(route = "home") {
            HomeScreen(
                viewModel = viewModel,
                onAnimeClick = { malId ->
                    navController.navigate("detail/$malId")
                }
            )
        }

        composable(
            route = "detail/{malId}",
            arguments = listOf(
                navArgument("malId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val malId = backStackEntry.arguments?.getInt("malId") ?: 0
            DetailScreen(
                viewModel = viewModel,
                malId = malId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}