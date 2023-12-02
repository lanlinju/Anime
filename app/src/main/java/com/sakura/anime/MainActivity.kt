package com.sakura.anime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sakura.anime.presentation.navigation.AnimeNavHost
import com.sakura.anime.presentation.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AnimeNavHost(
                modifier = Modifier.fillMaxSize(), navController = navController,
                onNavigateToAnimeDetail = { detailUrl ->
                    navController.navigate(route = Screen.AnimeDetailScreen.passUrl(detailUrl))
                },
                onEpisodeClick = { }
            )
        }
    }
}

