package com.example.componentsui.anime.domain.model

data class Episode(
    val name: String,
    val url: String, /* 如果播放本地视频是表示视频url，否则则是剧集url，需要在对应剧集页面解析视频url */
    val lastPosition: Long = 0L, /* 记录上次播放位置 */
    val isPlayed: Boolean = false, /* 用于标记是否已经播放过 */
    val isDownloaded: Boolean = false /* 用于标记是否已经加入下载列表 */
)
