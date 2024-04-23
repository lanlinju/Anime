package com.sakura.anime

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sakura.anime.presentation.component.NavigationBar
import com.sakura.anime.presentation.component.NavigationBarPaths
import com.sakura.anime.presentation.navigation.AnimeNavHost
import com.sakura.anime.presentation.navigation.Screen
import com.sakura.anime.presentation.theme.AnimeTheme
import com.sakura.anime.util.KEY_SOURCE_MODE
import com.sakura.anime.util.SourceHolder
import com.sakura.anime.util.SourceHolder.DEFAULT_ANIME_SOURCE
import com.sakura.anime.util.getEnum
import com.sakura.anime.util.preferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        SourceHolder.setDefaultSource(preferences.getEnum(KEY_SOURCE_MODE, DEFAULT_ANIME_SOURCE))

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

    Box(modifier.background(MaterialTheme.colorScheme.background)) {
        AnimeNavHost(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize(),
            navController = navController,
            onNavigateToAnimeDetail = { detailUrl, mode ->
                navController.navigate(route = Screen.AnimeDetailScreen.passUrl(detailUrl, mode))
            },
            onNavigateToVideoPlay = { episodeUrl, mode ->
                navController.navigate(route = Screen.VideoPlayScreen.passUrl(episodeUrl, mode))
            },
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToHistory = {
                navController.navigate(Screen.HistoryScreen.route)
            },
            onNavigateToDownload = {
                navController.navigate(Screen.DownloadScreen.route)
            },
            onNavigateToDownloadDetail = { detailUrl, title ->
                navController.navigate(Screen.DownloadDetailScreen.passUrl(detailUrl, title))
            },
            onNavigateToSearch = {
                navController.navigate(Screen.SearchScreen.route)
            },
            activity = activity
        )

        BottomNavigationBar(Modifier.align(Alignment.BottomCenter), navController)
    }

}

@Composable
private fun BottomNavigationBar(modifier: Modifier, navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isNavBarVisible = remember(currentBackStackEntry) {
        val currentDestination = currentBackStackEntry?.destination
        NavigationBarPaths.values().any { it.route == currentDestination?.route }
    }

    AnimatedVisibility(
        visible = isNavBarVisible,
        modifier = modifier,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        NavigationBar(navController = navController)
    }
}

