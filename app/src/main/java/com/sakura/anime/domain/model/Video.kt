package com.sakura.anime.domain.model

import com.example.componentsui.anime.domain.model.Episode

data class Video(
    val title: String,
    val url: String, /* 视频播放地址 */
    val episodeName: String,
    val episodeUrl: String,    /* 当前播放的剧集url */
    val lastPosition: Long = 0L,
    val currentEpisodeIndex: Int, /* 当前播放剧集索引，根据[episodeName]计算得出*/
    val episodes: List<Episode>,
    val headers: Map<String, String> = emptyMap() /* 用于配置Referer, User-Agent等*/
)
