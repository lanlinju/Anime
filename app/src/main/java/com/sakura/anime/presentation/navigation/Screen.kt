package com.sakura.anime.presentation.navigation

import android.net.Uri
import com.sakura.anime.util.SourceMode

const val DETAIL_ARGUMENT_URL = "detailUrl"
const val VIDEO_ARGUMENT_EPISODE_URL = "episodeUrl"
const val ANIME_ARGUMENT_TITLE = "animeTitle"
const val SOURCE_MODE_ARGUMENT = "sourceMode"

sealed class Screen(
    val route: String
) {
    object HomeScreen : Screen(route = "home")
    object AnimeDetailScreen :
        Screen(route = "animeDetail/{$DETAIL_ARGUMENT_URL}/{$SOURCE_MODE_ARGUMENT}") {
        fun passUrl(detailUrl: String, mode: SourceMode): String {
            return "animeDetail/${Uri.encode(detailUrl)}/$mode"
        }
    }

    object VideoPlayScreen :
        Screen(route = "videoPlay/{$VIDEO_ARGUMENT_EPISODE_URL}/{$SOURCE_MODE_ARGUMENT}") {
        fun passUrl(episodeUrl: String, mode: SourceMode): String {
            return "videoPlay/${Uri.encode(episodeUrl)}/$mode"
        }
    }

    object SearchScreen : Screen(route = "search")
    object WeekScreen : Screen(route = "week")
    object FavouriteScreen : Screen(route = "favourite")
    object HistoryScreen : Screen(route = "history")
    object DownloadScreen : Screen(route = "download")
    object DownloadDetailScreen :
        Screen(route = "downloadDetail/{$DETAIL_ARGUMENT_URL}/{$ANIME_ARGUMENT_TITLE}") {
        fun passUrl(detailUrl: String, title: String): String {
            return "downloadDetail/${Uri.encode(detailUrl)}/${title}"
        }
    }
}
