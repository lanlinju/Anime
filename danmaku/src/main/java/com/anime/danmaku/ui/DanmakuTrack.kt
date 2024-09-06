package com.anime.danmaku.ui

import androidx.compose.runtime.Stable

/**
 * 弹幕轨道, 支持放置已知长度的弹幕.
 *
 * 这意味着[已经知道长度的弹幕][SizeSpecifiedDanmaku]一定可以计算是否可以放置到此轨道上.
 */
@Stable
interface DanmakuTrack<T : SizeSpecifiedDanmaku, D> {
    /**
     * 放置弹幕到此轨道
     *
     * @return 返回已经放置的弹幕
     */
    fun place(danmaku: T): D

    /**
     * 检测这条弹幕是否可以放置到此轨道中.
     *
     * @return 可放置返回`true`
     */
    fun canPlace(danmaku: T): Boolean

    /**
     * 尝试放置弹幕
     *
     * @return 无法放置返回 `null`, 可放置则返回已放置的弹幕.
     */
    fun tryPlace(danmaku: T): D? {
        if (!canPlace(danmaku)) return null
        return place(danmaku)
    }

    /**
     * 清除当前轨道里的所有弹幕
     */
    fun clearAll()

    /**
     * 需要循环执行的逻辑帧.
     *
     * 弹幕轨道上述的方法通常依赖时间来进行判断, 可执行此逻辑帧 tick 来进行判断.
     *
     * 如果不需要判断则不需要实现此方法.
     *
     * 目前的 [FloatingDanmakuTrack] 和 [FixedDanmakuTrack] 均实现了逻辑帧并进行以下行为:
     * - 基于帧时间判断是否需要移除轨道中的过期弹幕.
     * - 基于已有弹幕判断当前时间点是否可以放置一条新弹幕.
     */
    fun tick()
}