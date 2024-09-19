package com.sakura.anime

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.navigation.compose.rememberNavController
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

        //https://github.com/android/compose-samples/issues/1256
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        SourceHolder.setDefaultSource(preferences.getEnum(KEY_SOURCE_MODE, DEFAULT_ANIME_SOURCE))
        setContent {
            AnimeTheme {
                NavHost(activity = this)
            }
        }
    }
}

@Composable
fun NavHost(modifier: Modifier = Modifier, activity: Activity) {
    val navController = rememberNavController()

    AnimeNavHost(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
        onNavigateToAppearance = {
            //navController.navigate(Screen.AppearanceScreen.route)
        },
        onNavigateToDanmakuSettings = {
            navController.navigate(Screen.DanmakuSettingsScreen.route)
        },
        activity = activity
    )
}



