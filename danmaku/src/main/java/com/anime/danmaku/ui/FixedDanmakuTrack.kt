package com.anime.danmaku.ui

import androidx.compose.runtime.Stable

/**
 * FixedDanmakuTrack 中的弹幕在以下情况会移除:
 * - tick 中的逻辑帧检测
 * - [FixedDanmakuTrack.place] 覆盖了正在显示的弹幕
 * - 调用 [DanmakuTrack.clearAll]
 * 移除时必须调用 [onRemoveDanmaku] 避免内存泄露.
 */
@Stable
internal class FixedDanmakuTrack<T : SizeSpecifiedDanmaku>(
    val trackIndex: Int,
    val fromBottom: Boolean,
    private val elapsedFrameTimeNanos: () -> Long,
    private val trackHeight: Int,
    private val trackWidth: Int,
    private val hostHeight: Int,
    // 顶部或底部弹幕的显示时间，现在还不能自定义
    private val durationMillis: Long,
    // 在 tick pendingDanmaku 显示了会调用
//    private val onTickReplacePending: (FixedDanmaku<T>) -> Unit,
    // 某个弹幕需要消失, 必须调用此函数避免内存泄漏.
    private val onRemoveDanmaku: (FixedDanmaku<T>) -> Unit
) : DanmakuTrack<T, FixedDanmaku<T>> {

    internal var currentDanmaku: FixedDanmaku<T>? = null

    override fun place(danmaku: T): FixedDanmaku<T> {
        val upcomingDanmaku = FixedDanmaku(
            danmaku,
            elapsedFrameTimeNanos(),
            trackIndex,
            trackHeight,
            trackWidth,
            hostHeight,
            fromBottom
        )
        currentDanmaku?.let(onRemoveDanmaku)
        currentDanmaku = upcomingDanmaku
        return upcomingDanmaku
    }

    override fun canPlace(
        danmaku: T,
    ): Boolean {
        return currentDanmaku == null
    }


    override fun clearAll() {
        currentDanmaku?.let(onRemoveDanmaku)
        currentDanmaku = null
    }

    override fun tick() {
        val current = currentDanmaku ?: return
        val danmakuTime = current.placeFrameTimeNanos
        if (elapsedFrameTimeNanos() - danmakuTime >= durationMillis * 1_000_000) {
            onRemoveDanmaku(current)
            currentDanmaku = null
        }
    }

    override fun toString(): String {
        return "FixedTrack(index=${trackIndex}, " +
                "placeTime=${currentDanmaku?.placeFrameTimeNanos?.div(1_000_000)})"
    }
}

@Stable
internal class FixedDanmaku<T : SizeSpecifiedDanmaku>(
    var danmaku: T,
    var placeFrameTimeNanos: Long,
    internal val trackIndex: Int,
    private val trackHeight: Int,
    private val trackWidth: Int,
    private val hostHeight: Int,
    internal val fromBottom: Boolean,
) {
    val screenPosY: Float by lazy { calculatePosY() }

    val screenPosX: Float = (trackWidth - danmaku.danmakuWidth) / 2f

    internal fun calculatePosY(): Float {
        return if (fromBottom) {
            hostHeight - (trackIndex + 1) * trackHeight.toFloat()
        } else {
            trackIndex * trackHeight.toFloat()
        }
    }

    override fun toString(): String {
        return "FixedDanmaku(width=${danmaku.danmakuWidth}, y=$screenPosY)"
    }
}