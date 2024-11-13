package com.sakura.anime.data.remote.dto

import com.sakura.anime.domain.model.AnimeDetail

data class AnimeDetailBean(
    val title: String,
    val imgUrl: String,
    val desc: String,
    val score: String = "", /* 可为空 */
    val tags: List<String>,
    val updateTime: String = "", /* 可为空 */
    val episodes: List<EpisodeBean>,
    val relatedAnimes: List<AnimeBean>
) {
    fun toAnimeDetail(): AnimeDetail {
        return AnimeDetail(
            title = title,
            img = imgUrl,
            desc = desc,
            score = score,
            tags = tags.map { it.uppercase() },
            updateTime = updateTime,
            lastPosition = 0,
            episodes = episodes.map { it.toEpisode() },
            relatedAnimes = relatedAnimes.map { it.toAnime() }
        )
    }
}