package com.sakura.anime.data.remote.dto

import com.sakura.anime.domain.model.Video

data class VideoBean(
    val title: String,
    val url: String,
    val episodeName: String,
    val episodes: List<EpisodeBean>
) {
    fun toVideo(): Video {
        val index = episodes.indexOfFirst { it.name == episodeName }
        return Video(
            title = title,
            url = url,
            episodeName = episodeName,
            currentEpisodeIndex = index,
            episodes = episodes.map { it.toEpisode() }
        )
    }
}
