package com.lanlinju.animius.data.remote.dto

import com.lanlinju.animius.domain.model.Episode

data class EpisodeBean(
    val name: String,
    val url: String
) {
    fun toEpisode(): Episode {
        return Episode(name = name, url = url)
    }
}