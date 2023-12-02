package com.sakura.anime

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sakura.anime.presentation.navigation.AnimeNavHost
import com.sakura.anime.presentation.navigation.Screen
import com.sakura.anime.ui.theme.AnimeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimeTheme {
                val navController = rememberNavController()
                AnimeNavHost(
                    modifier = Modifier.fillMaxSize(), navController = navController,
                    onNavigateToAnimeDetail = { detailUrl ->
                        navController.navigate(route = Screen.AnimeDetailScreen.passUrl(detailUrl))
                    },
                    onEpisodeClick = { episodeUrl ->
                        navController.navigate(route = Screen.VideoPlayScreen.passUrl(episodeUrl))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    activity = this
                )
            }

        }
    }
}

