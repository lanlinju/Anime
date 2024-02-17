package com.example.componentsui.anime.domain.model

data class Episode(
    val name: String,
    val url: String, /* 剧集url,但并不是真正的视频url，需要在对应剧集页面解析视频url */
    val lastPosition: Long = 0L, /* 记录上次播放位置 */
    val isPlayed: Boolean = false, /* 用于标记是否已经播放过 */
    val isDownloaded: Boolean = false /* 用于标记是否已经加入下载列表 */
)
