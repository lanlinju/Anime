package com.sakura.anime.presentation.navigation

import android.net.Uri

const val DETAIL_ARGUMENT_URL = "detailUrl"
const val VIDEO_ARGUMENT_EPISODE_URL = "episodeUrl"
const val VIDEO_ARGUMENT_TITLE = "episodeTitleUrl"

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
        Screen(route = "videoPlay/{$VIDEO_ARGUMENT_EPISODE_URL}/{$VIDEO_ARGUMENT_TITLE}") {
        fun passUrl(episodeUrl: String, title: String): String {
            return "videoPlay/${Uri.encode(episodeUrl)}/$title"
        }
    }

    object SearchScreen : Screen(route = "search")
    object WeekScreen : Screen(route = "week")
    object FavouriteScreen : Screen(route = "favourite")
}
