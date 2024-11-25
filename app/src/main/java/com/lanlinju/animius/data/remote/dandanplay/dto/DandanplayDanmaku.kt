package com.lanlinju.animius.data.remote.dandanplay.dto

import com.anime.danmaku.api.Danmaku
import com.anime.danmaku.api.DanmakuLocation
import com.lanlinju.animius.data.remote.dandanplay.DandanplayDanmakuProvider
import kotlinx.serialization.Serializable

@Serializable
class DandanplayDanmaku(
    val cid: Long,
    val p: String,
    val m: String, // content
)

fun DandanplayDanmaku.toDanmakuOrNull(): Danmaku? {
    /*
    p参数格式为出现时间,模式,颜色,用户ID，各个参数之间使用英文逗号分隔
    弹幕出现时间：格式为 0.00，单位为秒，精确到小数点后两位，例如12.34、445.6、789.01
    弹幕模式：1-普通弹幕，4-底部弹幕，5-顶部弹幕
    颜色：32位整数表示的颜色，算法为 Rx256x256+Gx256+B，R/G/B的范围应是0-255
    用户ID：字符串形式表示的用户ID，通常为数字，不会包含特殊字符
     */
    val (time, mode, color, userId) = p.split(",").let {
        if (it.size < 4) return null else it
    }

    val timeSecs = time.toDoubleOrNull() ?: return null

    return Danmaku(
        id = cid.toString(),
        providerId = DandanplayDanmakuProvider.ID,
        playTimeMillis = (timeSecs * 1000).toLong(),
        senderId = userId,
        location = when (mode.toIntOrNull()) {
            1 -> DanmakuLocation.NORMAL
            4 -> DanmakuLocation.BOTTOM
            5 -> DanmakuLocation.TOP
            else -> return null
        },
        text = m,
        color = color.toIntOrNull() ?: return null
    )
}

@Serializable
class DandanplayDanmakuListResponse(
    val count: Int,
    val comments: List<DandanplayDanmaku>
)