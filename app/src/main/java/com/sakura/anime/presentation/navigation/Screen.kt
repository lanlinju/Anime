package com.sakura.anime.presentation.navigation

import com.sakura.anime.util.SourceMode
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    object Main : Screen()

    @Serializable
    object Search : Screen()

    @Serializable
    object HistoryScreen : Screen()

    @Serializable
    object Download : Screen()

    @Serializable
    object Appearance : Screen()

    @Serializable
    object DanmakuSettings : Screen()

    @Serializable
    data class DownloadDetail(val detailUrl: String, val title: String) : Screen()

    @Serializable
    data class AnimeDetail(val detailUrl: String, val mode: SourceMode) : Screen()

    @Serializable
    data class VideoPlay(val episodeUrl: String, val mode: SourceMode) : Screen()
}
