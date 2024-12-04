package com.lanlinju.animius.data.remote.dto

import com.lanlinju.animius.domain.model.Video
import com.lanlinju.animius.domain.model.WebVideo

data class VideoBean(
    val title: String = "",
    val videoUrl: String,            /* 视频播放地址 */
    val episodeName: String = "",    /* 当前播放的剧集数名 */
    val episodes: List<EpisodeBean> = emptyList(),
    val headers: Map<String, String> = emptyMap()
) {
    @Deprecated("Using toWebVideo() instead")
    fun toVideo(): Video {
        val index = episodes.indexOfFirst { it.name == episodeName }
        val episodeUrl = episodes[index].url
        return Video(
            title = title,
            url = videoUrl,
            episodeName = episodeName,
            episodeUrl = episodeUrl,
            currentEpisodeIndex = index,
            episodes = episodes.map { it.toEpisode() },
            headers = headers,
        )
    }

    fun toWebVideo(): WebVideo {
        return WebVideo(
            url = videoUrl,
            headers = headers
        )
    }
}
