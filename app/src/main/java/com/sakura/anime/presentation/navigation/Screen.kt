package com.sakura.anime.presentation.navigation

import android.net.Uri

const val DETAIL_ARGUMENT_URL = "detailUrl"
const val Video_ARGUMENT_EPISODE_URL = "episodeUrl"

sealed class Screen(
    val route: String
) {
    object HomeScreen : Screen(route = "home")
    object AnimeDetailScreen : Screen(route = "animeDetail/{$DETAIL_ARGUMENT_URL}") {
        fun passUrl(detailUrl: String): String {
            return "animeDetail/${Uri.encode(detailUrl)}"
        }
    }

    object VideoPlayScreen : Screen(route = "videoPlay/{$Video_ARGUMENT_EPISODE_URL}") {
        fun passUrl(episodeUrl: String): String {
            return "videoPlay/${Uri.encode(episodeUrl)}"
        }
    }

    object SearchScreen : Screen(route = "search")
    object WeekScreen : Screen(route = "week")
    object FavouriteScreen : Screen(route = "favourite")
}
