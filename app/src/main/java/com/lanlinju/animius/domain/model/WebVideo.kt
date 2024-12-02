package com.lanlinju.animius.domain.model

data class WebVideo(
    val url: String,                              /* 视频播放地址 */
    val headers: Map<String, String> = emptyMap() /* 用于配置Referer, User-Agent等*/
)
