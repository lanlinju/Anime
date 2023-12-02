package com.sakura.anime.presentation.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sakura.anime.presentation.screen.animedetail.AnimeDetailScreen
import com.sakura.anime.presentation.screen.home.HomeScreen

@Composable
fun AnimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = Screen.HomeScreen.route,
    onNavigateToAnimeDetail: (detailUrl: String) -> Unit,
    onEpisodeClick: (episodeUrl: String) -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.HomeScreen.route) {
            HomeScreen(onNavigateToAnimeDetail = onNavigateToAnimeDetail)
        }
        composable(Screen.AnimeDetailScreen.route) {
            AnimeDetailScreen(onRelatedAnimeClick = onNavigateToAnimeDetail, onEpisodeClick = onEpisodeClick)
        }
    }
}