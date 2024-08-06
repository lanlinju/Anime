package com.sakura.anime.domain.model

import com.example.componentsui.anime.domain.model.Episode

data class Video(
    val title: String,
    val url: String, /* 视频播放地址 */
    val episodeName: String,
    val currentEpisodeIndex: Int, /* 当前播放剧集索引，根据[episodeName]计算得出*/
    val episodes: List<Episode>
)
