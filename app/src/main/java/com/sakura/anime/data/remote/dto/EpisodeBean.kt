package com.sakura.anime.data.remote.dto

import com.sakura.anime.domain.model.Episode

data class EpisodeBean(
    val name: String,
    val url: String
) {
    fun toEpisode(): Episode {
        return Episode(name = name, url = url)
    }
}