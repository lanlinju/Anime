package com.sakura.anime.presentation.navigation

import android.net.Uri

const val DETAIL_ARGUMENT_URL = "detailUrl"
const val VIDEO_ARGUMENT_EPISODE_URL = "episodeUrl"
const val ANIME_ARGUMENT_TITLE = "animeTitle"

sealed class Screen(
    val route: String
) {
    object HomeScreen : Screen(route = "home")
    object AnimeDetailScreen : Screen(route = "animeDetail/{$DETAIL_ARGUMENT_URL}") {
        fun passUrl(detailUrl: String): String {
            return "animeDetail/${Uri.encode(detailUrl)}"
        }
    }

    object VideoPlayScreen :
        Screen(route = "videoPlay/{$VIDEO_ARGUMENT_EPISODE_URL}") {
        fun passUrl(episodeUrl: String): String {
            return "videoPlay/${Uri.encode(episodeUrl)}"
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
