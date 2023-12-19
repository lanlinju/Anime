package com.sakura.anime.data.remote.dto

import com.example.componentsui.anime.domain.model.AnimeDetail

data class AnimeDetailBean(
    val anime: AnimeBean,
    val desc: String,
    val score: String,
    val tags: List<String>,
    val updateTime: String,
    val episodes: List<EpisodeBean>,
    val relatedAnimes: List<AnimeBean>
) {
    fun toAnimeDetail(): AnimeDetail {
        return AnimeDetail(
            title = anime.title,
            img = anime.img,
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