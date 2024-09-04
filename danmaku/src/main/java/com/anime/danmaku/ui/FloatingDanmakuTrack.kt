package com.anime.danmaku.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density

/**
 * FloatingDanmakuTrack 中的弹幕在以下情况会移除:
 * - tick 中的逻辑帧检测
 * - 调用 [DanmakuTrack.clearAll]
 * 移除时必须调用 [onRemoveDanmaku] 避免内存泄露.
 */
@Stable
internal class FloatingDanmakuTrack<T : SizeSpecifiedDanmaku>(
    val trackIndex: Int,
    private val elapsedFrameTimeNanos: () -> Long,
    private val trackHeight: Int,
    private val trackWidth: Int,
    private val density: Density,
    var baseSpeedPxPerSecond: Float,
    var safeSeparation: Float,
    // 放到这个轨道的弹幕里, 长度大于此基础长度才会加速弹幕运动, 等于此长度的弹幕速度为 100% [speedPxPerSecond]
    // var baseTextLength: Int,
    // val speedMultiplier: FloatState,
    // 某个弹幕需要消失, 必须调用此函数避免内存泄漏.
    private val onRemoveDanmaku: (FloatingDanmaku<T>) -> Unit
) : DanmakuTrack<T, FloatingDanmaku<T>> {
    private val danmakuList: MutableList<FloatingDanmaku<T>> = mutableListOf()

    /**
     * Checks whether this [danmaku] can be placed in the track.
     * If the last danmaku in the track is fully visible, the new danmaku can be placed.
     * Otherwise, the new danmaku cannot be placed to prevent overlap.
     *
     * @param danmaku The danmaku object to be placed.
     * @return `true` if the danmaku can be placed, otherwise `false`.
     */
    override fun canPlace(danmaku: T): Boolean {
        if (danmakuList.isEmpty()) return true
        val lastDanmaku = danmakuList.last()

        // If the last danmaku is fully visible, the new danmaku can be placed
        return if (lastDanmaku.isFullyVisible()) true else false
    }

    /**
     * Places the specified danmaku into the current track.
     * The danmaku's movement speed and placement time will be initialized according to the parameters,
     * and it will be added to the track's danmaku list.
     *
     * @param danmaku The danmaku object to be placed.
     * @return The initialized [FloatingDanmaku] object that has been placed.
     */
    override fun place(danmaku: T): FloatingDanmaku<T> {
        return FloatingDanmaku(
            danmaku,
            trackIndex = trackIndex,
            trackHeight = trackHeight,
            trackWidth = trackWidth,
            density = density,
            baseSpeedPxPerSecond = baseSpeedPxPerSecond,
            placeTimeNanos = elapsedFrameTimeNanos()
        ).also { danmakuList.add(it) }
    }

    /**
     * Clears all danmakus from the track.
     * Before clearing each danmaku, the [onRemoveDanmaku] method is called to ensure
     * related resources are released, preventing memory leaks.
     */
    override fun clearAll() {
        danmakuList.removeAll {
            onRemoveDanmaku(it)
            true
        }
    }

    /**
     * This method is called periodically during logic frames to check whether danmakus have moved
     * off-screen. If a danmaku has moved off-screen, it will be removed from the track,
     * and [onRemoveDanmaku] will be called to clean up resources.
     */
    override fun tick() {
        if (danmakuList.isEmpty()) return
        danmakuList.removeAll { danmaku ->
            danmaku.isGone().also {
                if (it) {
                    onRemoveDanmaku(danmaku)
                }
            }
        }
    }

    /**
     * Checks whether the danmaku has completely moved off the screen.
     *
     * @return `true` if the danmaku's X-axis position is less than its negative width
     * (i.e., completely off-screen), otherwise `false`.
     */
    private fun FloatingDanmaku<T>.isGone(): Boolean {
        return screenPosX <= -danmaku.danmakuWidth
    }

    /**
     * Checks whether the danmaku is fully visible.
     *
     * @return `true` if the danmaku's X-axis position is less than the track width
     * minus the danmaku width and the safe separation, otherwise `false`.
     */
    internal fun FloatingDanmaku<T>.isFullyVisible(): Boolean {
        return screenPosX <= trackWidth - danmaku.danmakuWidth - safeSeparation
    }

    override fun toString(): String {
        return "FloatingTrack(index=${trackIndex}, danmakuCount=${danmakuList.size})"
    }
}

/**
 * 一条浮动弹幕
 */
@Stable
class FloatingDanmaku<T : SizeSpecifiedDanmaku>(
    var danmaku: T,
    val trackIndex: Int,
    val trackHeight: Int,
    val trackWidth: Int,
    val density: Density,
    private var baseSpeedPxPerSecond: Float,
    val placeTimeNanos: Long,
) {

    /**
     * 弹幕在浮动轨道已滚动的距离, 是正数. 单位 px
     *
     * 例如, 如果弹幕现在在左侧刚被放置, 则等于 `0`.
     * 如果左边已滑到轨道最左侧, 则等于轨道长度.
     */
    val distanceX: Float get() = trackWidth - screenPosX

    /**
     * 弹幕在屏幕上的 Y 坐标位置, 由轨道高度和轨道索引计算得出.
     * 这是一个懒加载属性, 仅在第一次访问时计算.
     */
    val screenPosY by lazy { trackHeight.toFloat() * trackIndex }

    /**
     * 弹幕在屏幕上的 X 坐标位置. 初始位置为轨道宽度 (即弹幕刚好在屏幕右侧边缘).
     * 使用 `mutableFloatStateOf` 以确保该值是可变且可组合的状态.
     */
    var screenPosX by mutableFloatStateOf(trackWidth.toFloat())

    /**
     * 弹幕的速度, 以像素每秒为单位.
     * Unit px/s
     */
    val speedPxPerSecond = baseSpeedPxPerSecond

    /**
     * 在每一帧更新弹幕的位置.
     *
     * @param position 要更新到的新的 X 坐标位置.
     */
    fun updatePosX(position: Float) {
        screenPosX = position
    }

    override fun toString(): String {
        return "FloatingDanmaku(elapsedX=${distanceX}, y=$screenPosY)"
    }
}
