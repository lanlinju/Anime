package com.sakura.anime.data.remote.dandanplay.dto

import kotlinx.serialization.Serializable

@Serializable
data class DandanplaySearchEpisodeResponse(
    val hasMore: Boolean = false,
    val animes: List<SearchAnimeEpisodes> = listOf(),
    val errorCode: Int = 0,
    val success: Boolean = true,
    val errorMessage: String? = null,
)

@Serializable
data class SearchAnimeEpisodes(
    val animeId: Int,
    val animeTitle: String,
    val type: String,
    val typeDescription: String,
    val episodes: List<SearchEpisodeDetails> = listOf(),
)

@Serializable
data class SearchEpisodeDetails(
    val episodeId: Int,
    val episodeTitle: String,
)