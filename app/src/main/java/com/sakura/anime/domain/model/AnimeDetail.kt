package com.sakura.anime.domain.model

data class AnimeDetail(
    val title: String,
    val img: String,
    val desc: String,
    val score: String,
    val tags: List<String>,
    val updateTime: String,
    val lastPosition: Int,
    val episodes: List<Episode>,
    val relatedAnimes: List<Anime>
)
