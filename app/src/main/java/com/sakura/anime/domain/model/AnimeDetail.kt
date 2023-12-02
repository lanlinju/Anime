package com.example.componentsui.anime.domain.model

data class AnimeDetail(
    val title: String,
    val img: String,
    val desc: String,
    val score: String,
    val tags: List<String>,
    val updateTime: String,
    val episodes: List<Episode>,
    val relatedAnimes: List<Anime>
)
