package com.lanlinju.animius.data.remote.dto

import com.lanlinju.animius.domain.model.AnimeDetail

data class AnimeDetailBean(
    val title: String,
    val imgUrl: String,
    val desc: String,
    val tags: List<String> = emptyList(),
    val episodes: List<EpisodeBean> = emptyList(),  /* 保持对旧的数据兼容 */
    val relatedAnimes: List<AnimeBean>,
    val channels: Map<Int, List<EpisodeBean>> = emptyMap(),
) {
    fun toAnimeDetail(): AnimeDetail {
        val tempChannels = if (episodes.isNotEmpty()) { /* 保持对旧的不支持多线路的兼容 */
            mapOf(0 to episodes.map { it.toEpisode() })
        } else {
            channels.mapValues { it.value.map { it.toEpisode() } }
        }
        return AnimeDetail(
            title = title,
            img = imgUrl,
            desc = desc,
            tags = tags.map { it.uppercase() },
            lastPosition = 0,
            episodes = tempChannels[0] ?: emptyList(),
            relatedAnimes = relatedAnimes.map { it.toAnime() },
            channels = tempChannels,
        )
    }
}