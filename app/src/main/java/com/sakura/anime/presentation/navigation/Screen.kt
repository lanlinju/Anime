package com.sakura.anime.presentation.navigation

import android.net.Uri
import com.sakura.anime.util.SourceMode

const val ROUTE_ARGUMENT_DETAIL_URL = "detailUrl"
const val ROUTE_ARGUMENT_VIDEO_EPISODE_URL = "episodeUrl"
const val ROUTE_ARGUMENT_ANIME_TITLE = "animeTitle"
const val ROUTE_ARGUMENT_SOURCE_MODE = "sourceMode"

sealed class Screen(
    val route: String
) {
    object MainScreen : Screen(route = "main")
    object HomeScreen : Screen(route = "home")
    object SearchScreen : Screen(route = "search")
    object WeekScreen : Screen(route = "week")
    object FavouriteScreen : Screen(route = "favourite")
    object HistoryScreen : Screen(route = "history")
    object DownloadScreen : Screen(route = "download")
    object AppearanceScreen : Screen(route = "Appearance")

    object DownloadDetailScreen :
        Screen(route = "downloadDetail/{$ROUTE_ARGUMENT_DETAIL_URL}/{$ROUTE_ARGUMENT_ANIME_TITLE}") {
        fun passUrl(detailUrl: String, title: String): String {
            return "downloadDetail/${Uri.encode(detailUrl)}/${title}"
        }
    }

    object AnimeDetailScreen :
        Screen(route = "animeDetail/{$ROUTE_ARGUMENT_DETAIL_URL}/{$ROUTE_ARGUMENT_SOURCE_MODE}") {
        fun passUrl(detailUrl: String, mode: SourceMode): String {
            return "animeDetail/${Uri.encode(detailUrl)}/$mode"
        }
    }

    object VideoPlayScreen :
        Screen(route = "videoPlay/{$ROUTE_ARGUMENT_VIDEO_EPISODE_URL}/{$ROUTE_ARGUMENT_SOURCE_MODE}") {
        fun passUrl(episodeUrl: String, mode: SourceMode): String {
            return "videoPlay/${Uri.encode(episodeUrl)}/$mode"
        }
    }
}
