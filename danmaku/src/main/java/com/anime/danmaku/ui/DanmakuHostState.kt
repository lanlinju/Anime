package com.anime.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.anime.danmaku.api.DanmakuLocation
import com.anime.danmaku.api.DanmakuPresentation
import kotlin.math.floor


@Composable
fun rememberDanmakuHostState(danmakuConfig: DanmakuConfig = DanmakuConfig.Default): DanmakuHostState {
    val density = LocalDensity.current
    val trackStubMeasurer = rememberTextMeasurer(1)
    val danmakuTextMeasurer = rememberTextMeasurer(200)
    val baseStyle = MaterialTheme.typography.bodyMedium
    return remember {
        DanmakuHostState(danmakuConfig, trackStubMeasurer, danmakuTextMeasurer, density, baseStyle)
    }
}

class DanmakuHostState(
    val config: DanmakuConfig = DanmakuConfig.Default,
    val trackStubMeasurer: TextMeasurer,
    val danmakuTextMeasurer: TextMeasurer,
    val density: Density,
    val baseStyle: TextStyle,
) {
    internal var hostWidth by mutableIntStateOf(0)
    internal var hostHeight by mutableIntStateOf(0)

    /**
     * 所有在 [floatingTracks], [topTracks] 和 [bottomTracks] 弹幕.
     */
    internal val presentFloatingDanmaku = mutableStateListOf<FloatingDanmaku<StyledDanmaku>>()
    internal val presentFixedDanmaku = mutableListOf<FixedDanmaku<StyledDanmaku>>()

    // 弹幕轨道
    internal val floatingTracks = mutableListOf<FloatingDanmakuTrack<StyledDanmaku>>()
    internal val topTracks = mutableListOf<FixedDanmakuTrack<StyledDanmaku>>()
    internal val bottomTracks = mutableListOf<FixedDanmakuTrack<StyledDanmaku>>()

    var elapsedFrameTimeNanos: Long = 0L
    val isDebug = config.isDebug
    var paused by mutableStateOf(false)

    val trackWidth by derivedStateOf { hostWidth }
    val trackHeight by lazy {
        val dummyTextLayout = dummyDanmaku(
            trackStubMeasurer,
            baseStyle,
            config.style,
            "Lorem Ipsum"
        ).solidTextLayout
        val verticalPadding = with(density) {
            (config.danmakuTrackProperties.verticalPadding * 2).dp.toPx()
        }
        val trackHeight = (dummyTextLayout.size.height + verticalPadding).toInt()
        trackHeight
    }

    /**
     * 尝试发送弹幕到屏幕, 如果当前时间点已没有更多轨道可以使用则会发送失败.
     *
     * 对于一定发送成功的版本, 请查看 [DanmakuHostState.send].
     * 若是浮动弹幕则加入到 [presentFloatingDanmaku], 固定弹幕加到 [presentFixedDanmaku].
     *
     * @return 如果发送成功则返回 true
     * @see DanmakuHostState.send
     */
    fun trySend(danmaku: DanmakuPresentation): Boolean {
        val styledDanmaku = StyledDanmaku(
            presentation = danmaku,
            measurer = danmakuTextMeasurer,
            baseStyle = baseStyle,
            style = config.style,
            enableColor = config.enableColor,
            isDebug = config.isDebug,
        )
        return when (danmaku.danmaku.location) {
            DanmakuLocation.NORMAL -> {
                val floatingDanmaku = floatingTracks.firstNotNullOfOrNull {
                    it.tryPlace(styledDanmaku)
                }
                floatingDanmaku?.also(presentFloatingDanmaku::add) != null
            }

            DanmakuLocation.TOP -> {
                val floatingDanmaku = topTracks.firstNotNullOfOrNull {
                    it.tryPlace(styledDanmaku)
                }
                floatingDanmaku?.also(presentFixedDanmaku::add) != null
            }

            DanmakuLocation.BOTTOM -> {
                val floatingDanmaku = bottomTracks.firstNotNullOfOrNull {
                    it.tryPlace(styledDanmaku)
                }
                floatingDanmaku?.also(presentFixedDanmaku::add) != null
            }
        }
    }

    /**
     * 逻辑帧 tick, 主要用于移除超出屏幕外或超过时间的弹幕
     */
    @UiThread
    fun tick() {
        floatingTracks.forEach { it.tick() }
        topTracks.forEach { it.tick() }
        bottomTracks.forEach { it.tick() }
    }

    /**
     * 在每一帧中调用，主要用于更新浮动弹幕的位置。
     * 该方法为一个协程循环，确保弹幕的位置根据时间的变化得到更新。
     */
    suspend fun interpolateFrameLoop() {
        var lastFrameTimeNanos = withFrameNanos { it }

        while (true) {
            withFrameNanos { currentFrameTimeNanos ->
                val delta = currentFrameTimeNanos - lastFrameTimeNanos

                elapsedFrameTimeNanos += delta
                lastFrameTimeNanos = currentFrameTimeNanos

                // 更新浮动弹幕的位置
                for (danmaku in presentFloatingDanmaku) {
                    val time = (elapsedFrameTimeNanos - danmaku.placeTimeNanos) / 1_000_000_000f
                    val x = time * danmaku.speedPxPerSecond
                    danmaku.updatePosX(danmaku.trackWidth - x)
                }
            }
        }
    }

    fun setTrackCount() {
        val trackCount = floor(hostHeight / trackHeight * config.displayArea)
            .coerceAtLeast(1f)
            .toInt()
        initTrackCount(trackCount, config)
    }

    /**
     * 更新弹幕轨道数量, 同时也会更新轨道属性
     */
    @UiThread
    private fun initTrackCount(count: Int, config: DanmakuConfig) {
        val newFloatingTrackSpeed =
            with(density) { this@DanmakuHostState.config.baseSpeed.dp.toPx() }
        val newFloatingTrackSafeSeparation =
            with(density) { this@DanmakuHostState.config.safeSeparation.toPx() }

        floatingTracks.setTrackCountImpl(if (config.enableFloating) count else 0) { index ->
            FloatingDanmakuTrack(
                trackIndex = index,
                elapsedFrameTimeNanos = { elapsedFrameTimeNanos },
                trackHeight = trackHeight,
                trackWidth = trackWidth,
                density = density,
                baseSpeedPxPerSecond = newFloatingTrackSpeed,
                safeSeparation = newFloatingTrackSafeSeparation,
                // speedMultiplier = floatingSpeedMultiplierState,
                onRemoveDanmaku = { removed ->
                    presentFloatingDanmaku.removeFirst { it.danmaku == removed.danmaku }
                },
            )
        }
        topTracks.setTrackCountImpl(if (config.enableTop) count else 0) { index ->
            FixedDanmakuTrack(
                trackIndex = index,
                elapsedFrameTimeNanos = { elapsedFrameTimeNanos },
                trackHeight = trackHeight,
                trackWidth = trackWidth,
                hostHeight = hostHeight,
                fromBottom = false,
                durationMillis = config.danmakuTrackProperties.fixedDanmakuPresentDuration,
                onRemoveDanmaku = { removed -> presentFixedDanmaku.removeFirst { it.danmaku == removed.danmaku } },
            )
        }
        bottomTracks.setTrackCountImpl(if (config.enableBottom) count else 0) { index ->
            FixedDanmakuTrack(
                trackIndex = index,
                elapsedFrameTimeNanos = { elapsedFrameTimeNanos },
                trackHeight = trackHeight,
                trackWidth = trackWidth,
                hostHeight = hostHeight,
                fromBottom = true,
                durationMillis = config.danmakuTrackProperties.fixedDanmakuPresentDuration,
                onRemoveDanmaku = { removed -> presentFixedDanmaku.removeFirst { it.danmaku == removed.danmaku } },
            )
        }
    }

    /**
     * 清除当前显示的所有弹幕。
     */
    @UiThread
    private fun clearPresentDanmaku() {
        floatingTracks.forEach { it.clearAll() }
        topTracks.forEach { it.clearAll() }
        bottomTracks.forEach { it.clearAll() }

        check(presentFloatingDanmaku.size == 0) {
            "presentFloatingDanmaku is not totally cleared after releasing track."
        }
        check(presentFixedDanmaku.size == 0) {
            "presentFixedDanmaku is not totally cleared after releasing track."
        }
    }

    /**
     * 清空屏幕弹幕
     * Todo: 将[list] 填充到屏幕.
     */
    suspend fun repopulate(list: List<DanmakuPresentation>) {
        clearPresentDanmaku()
    }

    // Todo: 发送弹幕到屏幕, 此方法一定会保证弹幕发送出去
    suspend fun send(danmaku: DanmakuPresentation) {
        error("Send is not yet implemented")
    }

    fun play() {
        paused = false
    }

    fun pause() {
        paused = true
    }
}

private fun <D : SizeSpecifiedDanmaku, DT, T : DanmakuTrack<D, DT>>
        MutableList<T>.setTrackCountImpl(count: Int, newInstance: (index: Int) -> T) {
    when {
        size == count -> return
        // 清除 track 的同时要把 track 里的 danmaku 也要清除
        count < size -> repeat(size - count) { removeLast().clearAll() }
        else -> addAll(List(count - size) { newInstance(size + it) })
    }
}

private inline fun <T> MutableList<T>.removeFirst(predicate: (T) -> Boolean): T? {
    val index = indexOfFirst(predicate)
    if (index == -1) return null
    return removeAt(index)
}