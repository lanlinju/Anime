package com.sakura.anime

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sakura.anime.presentation.component.NavigationBar
import com.sakura.anime.presentation.component.NavigationBarPaths
import com.sakura.anime.presentation.navigation.AnimeNavHost
import com.sakura.anime.presentation.navigation.Screen
import com.sakura.anime.presentation.theme.AnimeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AnimeTheme {
                MainScreen(Modifier.fillMaxSize(), this)
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, activity: Activity) {
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val isNavBarVisible = remember(currentBackStackEntry) {
        NavigationBarPaths.values().any { it.route == currentDestination?.route }
    }

    Box(modifier) {
        AnimeNavHost(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize(),
            navController = navController,
            onNavigateToAnimeDetail = { detailUrl ->
                navController.navigate(route = Screen.AnimeDetailScreen.passUrl(detailUrl))
            },
            onEpisodeClick = { episodeUrl, title ->
                navController.navigate(route = Screen.VideoPlayScreen.passUrl(episodeUrl, title))
            },
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToFavourite = {
                navController.navigate(Screen.FavouriteScreen.route)
            },
            onNavigateToHistory = {
                navController.navigate(Screen.HistoryScreen.route)
            },
            onSearchClick = {
                navController.navigate(Screen.SearchScreen.route)
            },
            activity = activity
        )

        AnimatedVisibility(
            visible = isNavBarVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            NavigationBar(navController = navController)
        }
    }

}

