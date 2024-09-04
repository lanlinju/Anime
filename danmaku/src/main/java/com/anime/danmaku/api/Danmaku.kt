package com.anime.danmaku.api

import androidx.compose.runtime.Immutable

@Immutable
class DanmakuPresentation(
    val danmaku: Danmaku,
    val isSelf: Boolean,
) {
    val id get() = danmaku.id
}

@Immutable
data class Danmaku(
    val id: String,
    val providerId: String,
    val playTimeMillis: Long,
    val senderId: String,
    val location: DanmakuLocation,
    val text: String,
    val color: Int, // RGB
) {
    override fun toString(): String {
        return "Danmaku(id='$id', providerId='$providerId', playTimeMillis=$playTimeMillis, senderId='$senderId', location=$location, text='$text', color=$color)"
    }
}

enum class DanmakuLocation {
    TOP,
    BOTTOM,
    NORMAL,
}