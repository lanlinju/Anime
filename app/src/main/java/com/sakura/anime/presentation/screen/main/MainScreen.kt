package com.sakura.anime.presentation.screen.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.sakura.anime.presentation.component.AdaptiveNavigationBar
import com.sakura.anime.presentation.component.NavigationBarPath
import com.sakura.anime.presentation.screen.favourite.FavouriteScreen
import com.sakura.anime.presentation.screen.home.HomeScreen
import com.sakura.anime.presentation.screen.week.WeekScreen
import com.sakura.anime.util.SourceMode
import com.sakura.anime.util.isWideScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    onNavigateToAnimeDetail: (detailUrl: String, mode: SourceMode) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDownload: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAppearance: () -> Unit,
) {
    var currentDestination by rememberSaveable { mutableStateOf(NavigationBarPath.Home.route) }
    val pagerState = rememberPagerState(initialPage = 1) { NavigationBarPath.values().size }
    val scope = rememberCoroutineScope()

    val isWideScreen = isWideScreen(LocalContext.current)

    val navigationContent: @Composable () -> Unit = {
        AdaptiveNavigationBar(
            destinations = NavigationBarPath.entries,
            currentDestination = currentDestination,
            onNavigateToDestination = {
                currentDestination = NavigationBarPath.entries[it].route
                scope.launch { pagerState.scrollToPage(it) }
            },
            isWideScreen = isWideScreen,
        )
    }

    val pagerContent: @Composable (Modifier) -> Unit = { modifier ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = modifier // 这里 error
        ) { page ->
            when (page) {
                0 -> WeekScreen(
                    onNavigateToAnimeDetail = onNavigateToAnimeDetail,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToDownload = onNavigateToDownload,
                    onNavigateToAppearance = onNavigateToAppearance
                )
                1 -> HomeScreen(onNavigateToAnimeDetail)
                2 -> FavouriteScreen(onNavigateToAnimeDetail)
            }
        }
    }

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            navigationContent()
            pagerContent(
                Modifier
                    .fillMaxHeight()
                    .weight(1f))
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            pagerContent(
                Modifier
                    .fillMaxWidth()
                    .weight(1f))
            navigationContent()
        }
    }
}
