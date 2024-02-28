package com.sakura.anime.presentation.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sakura.anime.presentation.screen.animedetail.AnimeDetailScreen
import com.sakura.anime.presentation.screen.download.DownloadScreen
import com.sakura.anime.presentation.screen.downloaddetail.DownloadDetailScreen
import com.sakura.anime.presentation.screen.favourite.FavouriteScreen
import com.sakura.anime.presentation.screen.history.HistoryScreen
import com.sakura.anime.presentation.screen.home.HomeScreen
import com.sakura.anime.presentation.screen.search.SearchScreen
import com.sakura.anime.presentation.screen.videoplay.VideoPlayScreen
import com.sakura.anime.presentation.screen.week.WeekScreen
import com.sakura.anime.util.SourceMode

@Composable
fun AnimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    currentSourceMode: SourceMode,
    onSourceChange: (SourceMode) -> Unit,
    startDestination: String = Screen.HomeScreen.route,
    onNavigateToAnimeDetail: (detailUrl: String, mode: SourceMode) -> Unit,
    onNavigateToVideoPlay: (episodeUrl: String, mode: SourceMode) -> Unit,
    onNavigateToFavourite: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDownload: () -> Unit,
    onNavigateToDownloadDetail: (detailUrl: String, title: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onBackClick: () -> Unit,
    activity: Activity
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.HomeScreen.route) {
            HomeScreen(onNavigateToAnimeDetail = { onNavigateToAnimeDetail(it, currentSourceMode) })
        }
        composable(Screen.AnimeDetailScreen.route) {
            AnimeDetailScreen(
                onRelatedAnimeClick = onNavigateToAnimeDetail,
                onNavigateToVideoPlay = onNavigateToVideoPlay,
                onBackClick = onBackClick
            )
        }
        composable(Screen.VideoPlayScreen.route) {
            VideoPlayScreen(activity = activity, onBackClick = onBackClick)
        }
        composable(Screen.SearchScreen.route) {
            SearchScreen(
                onNavigateToAnimeDetail = { onNavigateToAnimeDetail(it, currentSourceMode) },
                onBackClick = onBackClick
            )
        }
        composable(Screen.WeekScreen.route) {
            WeekScreen(
                currentSourceMode = currentSourceMode,
                onSourceChange = onSourceChange,
                onNavigateToAnimeDetail = onNavigateToAnimeDetail,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToFavourite = onNavigateToFavourite,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToDownload = onNavigateToDownload
            )
        }
        composable(Screen.FavouriteScreen.route) {
            FavouriteScreen(
                onNavigateToAnimeDetail = onNavigateToAnimeDetail,
                onBackClick = onBackClick
            )
        }
        composable(Screen.HistoryScreen.route) {
            HistoryScreen(
                onBackClick = onBackClick,
                onNavigateToAnimeDetail = onNavigateToAnimeDetail,
                onNavigateToVideoPlay = onNavigateToVideoPlay
            )
        }
        composable(Screen.DownloadScreen.route) {
            DownloadScreen(
                onBackClick = onBackClick,
                onNavigateToDownloadDetail = onNavigateToDownloadDetail,
                onNavigateToAnimeDetail = onNavigateToAnimeDetail
            )
        }
        composable(Screen.DownloadDetailScreen.route) {
            DownloadDetailScreen(
                onBackClick = onBackClick,
                onNavigateToVideoPlay = { onNavigateToVideoPlay(it, SourceMode.Yhdm) }
            )
        }
    }
}