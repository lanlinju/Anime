package com.lanlinju.animius.data.remote.dto

import com.lanlinju.animius.domain.model.WebVideo

data class VideoBean(
    val videoUrl: String,            /* 视频播放地址 */
    val headers: Map<String, String> = emptyMap()
) {
    fun toWebVideo(): WebVideo {
        return WebVideo(
            url = videoUrl,
            headers = headers
        )
    }
}
