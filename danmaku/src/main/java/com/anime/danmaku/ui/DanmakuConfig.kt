package com.anime.danmaku.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 用于限制弹幕的最低和最高速度
 * 同时根据弹幕长度基于[baseSpeed]计算相应速度
 */
internal val MaxSpeedMultiplier = 3f
internal val BaseTextLength = 80.dp
internal val MaxTextLength = 620.dp

/**
 * Configuration for the presentation of each [Danmaku].
 */
@Immutable
data class DanmakuConfig(
    /**
     * Controls the text styles of the [Danmaku].
     * For example, font size, stroke width.
     */
    val style: DanmakuStyle = DanmakuStyle.Default,
    /**
     * Time for the [Danmaku] to move from the right edge to the left edge of the screen.
     * In other words, it controls the movement speed of a [Danmaku].
     *
     * Unit: dp/s
     */
    val baseSpeed: Float = 88f,
    /**
     * The minimum distance between two [Danmaku]s so that they don't overlap.
     */
    val safeSeparation: Dp = 2.dp,
    /**
     * 弹幕在屏幕中的显示区域. 0.1 表示屏幕的 10%.
     *
     * 范围: `[0, 1]`
     */
    val displayArea: Float = 0.25f,
    /**
     * 允许彩色弹幕. 禁用时将会把所有彩色弹幕都显示为白色.
     */
    val enableColor: Boolean = true,
    /**
     * 是否启用顶部弹幕
     */
    val enableTop: Boolean = true,
    /**
     * 是否启用浮动弹幕
     */
    val enableFloating: Boolean = true,
    /**
     * 是否启用底部弹幕
     */
    val enableBottom: Boolean = true,
    /**
     * 调试模式, 启用发送弹幕的信息和弹幕处理信息.
     */
    val isDebug: Boolean = false,
    /**
     * 轨道属性
     */
    val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default,
) {
    companion object {
        @Stable
        val Default = DanmakuConfig()
    }
}

@Immutable
class DanmakuStyle(
    val fontSize: TextUnit = 18.sp,
    val fontWeight: FontWeight = FontWeight.W600,
    val alpha: Float = 0.8f,
    val strokeColor: Color = Color.Black,
    val strokeWidth: Float = 2f,
    val shadow: Shadow? = null,
) {
    @Stable
    fun styleForBorder(): TextStyle = TextStyle(
        fontSize = fontSize,
        color = strokeColor.copy(alpha),
        fontWeight = fontWeight,
        drawStyle = Stroke(
            width = strokeWidth,
            join = StrokeJoin.Round,
        ),
        textMotion = TextMotion.Animated,
        shadow = shadow,
    )

    // 'inside' the border
    @Stable
    fun styleForText(color: Color = Color.White): TextStyle = TextStyle(
        fontSize = fontSize,
        color = color.copy(alpha),
        fontWeight = fontWeight,
        textMotion = TextMotion.Animated,
    )

    override fun toString(): String {
        return "DanmakuStyle(fontSize=$fontSize, fontWeight=$fontWeight, alpha=$alpha, strokeColor=$strokeColor, strokeMiter=$strokeWidth, shadow=$shadow)"
    }

    companion object {
        @Stable
        val Default = DanmakuStyle()
    }
}

@Immutable
class DanmakuTrackProperties(
    /**
     * Shift of the danmaku to be considered as fully out of the screen.
     */
    val visibilitySafeArea: Int = 0,
    /**
     * vertical padding of track, both top and bottom.
     */
    val verticalPadding: Int = 1,
    /**
     * speed multiplier for speed of floating danmaku.
     * represents a multiplier to speed that 2x length of danmaku text
     */
    val speedMultiplier: Float = 1.14f,
    /**
     * fixed danmaku present duration.
     * unit: ms
     */
    val fixedDanmakuPresentDuration: Long = 5000
) {
    companion object {
        @Stable
        val Default = DanmakuTrackProperties()
    }
}