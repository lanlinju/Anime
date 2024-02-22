package com.sakura.anime.domain.model

import com.example.componentsui.anime.domain.model.Episode

data class Video(
    val title: String,
    val url: String,
    val episodeName: String,
    val currentEpisodeIndex: Int,
    val episodes: List<Episode>
)
