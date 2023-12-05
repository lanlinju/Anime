package com.sakura.anime.presentation.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sakura.anime.presentation.screen.animedetail.AnimeDetailScreen
import com.sakura.anime.presentation.screen.home.HomeScreen
import com.sakura.anime.presentation.screen.search.SearchScreen
import com.sakura.anime.presentation.screen.videoplay.VideoPlayScreen
import com.sakura.anime.presentation.screen.week.WeekScreen

@Composable
fun AnimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = Screen.HomeScreen.route,
    onNavigateToAnimeDetail: (detailUrl: String) -> Unit,
    onEpisodeClick: (episodeUrl: String) -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
    activity: Activity
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
        composable(Screen.VideoPlayScreen.route) {
            VideoPlayScreen(activity = activity, onBackClick = onBackClick)
        }
        composable(Screen.SearchScreen.route) {
            SearchScreen(navController=navController,onBackClick=onBackClick)
        }
        composable(Screen.WeekScreen.route) {
            WeekScreen(onNavigateToAnimeDetail = onNavigateToAnimeDetail, onSearchClick = onSearchClick)
        }

    }
}