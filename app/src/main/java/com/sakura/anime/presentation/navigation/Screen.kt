package com.sakura.anime.presentation.navigation

const val DETAIL_ARGUMENT_URL = "detailUrl"
const val Video_ARGUMENT_EPISODE_URL = "episodeUrl"
const val Search_ARGUMENT_QUERY = "query"

sealed class Screen(
    val route: String
) {
    object HomeScreen : Screen(route = "home")
    object AnimeDetailScreen : Screen(route = "animeDetail/show/{$DETAIL_ARGUMENT_URL}") {
        fun passUrl(detailUrl: String): String {
            return "animeDetail$detailUrl"
        }
    }
    object VideoPlayScreen : Screen(route = "videoPlay/v/{$Video_ARGUMENT_EPISODE_URL}") {
        fun passUrl(episodeUrl: String): String {
            return "videoPlay$episodeUrl"
        }
    }
}
