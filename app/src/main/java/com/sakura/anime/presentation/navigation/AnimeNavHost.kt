package com.sakura.anime.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sakura.anime.presentation.screen.animedetail.AnimeDetailScreen
import com.sakura.anime.presentation.screen.download.DownloadScreen
import com.sakura.anime.presentation.screen.downloaddetail.DownloadDetailScreen
import com.sakura.anime.presentation.screen.history.HistoryScreen
import com.sakura.anime.presentation.screen.main.MainScreen
import com.sakura.anime.presentation.screen.search.SearchScreen
import com.sakura.anime.presentation.screen.settings.AppearanceScreen
import com.sakura.anime.presentation.screen.settings.DanmakuSettingsScreen
import com.sakura.anime.presentation.screen.videoplay.VideoPlayScreen
import com.sakura.anime.util.SourceMode

@Composable
fun AnimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: Any = Screen.Main,
    onNavigateToAnimeDetail: (detailUrl: String, mode: SourceMode) -> Unit,
    onNavigateToVideoPlay: (episodeUrl: String, mode: SourceMode) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDownload: () -> Unit,
    onNavigateToDownloadDetail: (detailUrl: String, title: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToDanmakuSettings: () -> Unit,
    onBackClick: () -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Main> {
            MainScreen(
                onNavigateToAnimeDetail = onNavigateToAnimeDetail,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToDownload = onNavigateToDownload,
                onNavigateToAppearance = onNavigateToAppearance,
                onNavigateToDanmakuSettings = onNavigateToDanmakuSettings,
            )
        }
        composable<Screen.AnimeDetail>() {
            AnimeDetailScreen(
                onRelatedAnimeClick = onNavigateToAnimeDetail,
                onNavigateToVideoPlay = onNavigateToVideoPlay,
                onBackClick = onBackClick
            )
        }
        composable<Screen.VideoPlay>(
            enterTransition = { fadeIn() },
            popExitTransition = { fadeOut(animationSpec = tween(durationMillis = 35)) }
        ) {
            VideoPlayScreen(onBackClick = onBackClick)
        }
        composable<Screen.Search> {
            SearchScreen(
                onNavigateToAnimeDetail = onNavigateToAnimeDetail,
                onBackClick = onBackClick
            )
        }
        composable<Screen.HistoryScreen> {
            HistoryScreen(
                onBackClick = onBackClick,
                onNavigateToAnimeDetail = onNavigateToAnimeDetail,
                onNavigateToVideoPlay = onNavigateToVideoPlay
            )
        }
        composable<Screen.Download> {
            DownloadScreen(
                onBackClick = onBackClick,
                onNavigateToDownloadDetail = onNavigateToDownloadDetail,
                onNavigateToAnimeDetail = onNavigateToAnimeDetail
            )
        }
        composable<Screen.DownloadDetail> {
            DownloadDetailScreen(
                onBackClick = onBackClick,
                onNavigateToVideoPlay = onNavigateToVideoPlay
            )
        }
        composable<Screen.Appearance> {
            AppearanceScreen(
                onBackClick = onBackClick
            )
        }
        composable<Screen.DanmakuSettings> {
            DanmakuSettingsScreen(
                onBackClick = onBackClick
            )
        }
    }
}