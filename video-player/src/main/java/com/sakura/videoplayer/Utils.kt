package com.sakura.videoplayer

import android.widget.FrameLayout
import androidx.compose.ui.unit.Constraints
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.video.VideoSize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


internal inline fun VideoSize.aspectRatio(): Float =
    if (height == 0 || width == 0) 0f else (width * pixelWidthHeightRatio) / height

/**
 * The [FrameLayout] will not resize itself if the fractional difference between its natural
 * aspect ratio and the requested aspect ratio falls below this threshold.
 *
 *
 * This tolerance allows the view to occupy the whole of the screen when the requested aspect
 * ratio is very close, but not exactly equal to, the aspect ratio of the screen. This may reduce
 * the number of view layers that need to be composited by the underlying system, which can help
 * to reduce power consumption.
 */
private const val MAX_ASPECT_RATIO_DIFFERENCE_FRACTION = 0.01f
private const val VIDEO_ASPECT_RATIO_16_9 = 16f.div(9f) // 16 : 9, 1.7777778
private const val VIDEO_ASPECT_RATIO_4_3 = 4f.div(3f)  //   4 : 3, 1.3333334

internal inline fun Constraints.resizeForVideo(
    mode: ResizeMode,
    aspectRatio: Float
): Constraints {
    if (aspectRatio <= 0f) {
        // 设置默认视频显示大小为：横屏模式下宽高比为16:9的大小
        val width = (maxHeight * VIDEO_ASPECT_RATIO_16_9).toInt() // default 16 : 9
        return this.copy(maxWidth = width)
    }

    var width = maxWidth
    var height = maxHeight
    val constraintAspectRatio: Float = (width / height).toFloat()
    val difference = aspectRatio / constraintAspectRatio - 1

    if (kotlin.math.abs(difference) <= MAX_ASPECT_RATIO_DIFFERENCE_FRACTION) {
        // 视频比例与屏幕比例十分接近，不处理
        return this
    }

    when (mode) {
        ResizeMode.Fit -> {
            if (difference > 0) { /* difference 大于零 为竖屏模式 */
                height = (width / aspectRatio).toInt()
            } else { /* 横屏模式 */
                width = (height * aspectRatio).toInt()
            }
        }

        ResizeMode.Zoom -> {
            if (difference > 0) {
                width = (height * aspectRatio).toInt()
            } else {
                height = (width / aspectRatio).toInt()
            }
        }

        ResizeMode.FixedWidth -> {
            height = (width / aspectRatio).toInt()
        }

        ResizeMode.FixedHeight -> {
            width = (height * aspectRatio).toInt()
        }

        ResizeMode.FixedRatio_16_9 -> {
            if (difference > 0) {
                height = (width / VIDEO_ASPECT_RATIO_16_9).toInt()
            } else {
                width = (height * VIDEO_ASPECT_RATIO_16_9).toInt()
            }
        }

        ResizeMode.FixedRatio_4_3 -> {
            if (difference > 0) {
                height = (width / VIDEO_ASPECT_RATIO_4_3).toInt()
            } else {
                width = (height * VIDEO_ASPECT_RATIO_4_3).toInt()
            }
        }

        ResizeMode.Full -> {
            if (difference > 0) {
                width = (height * aspectRatio).toInt()
            } else {
                height = (width / aspectRatio).toInt()
            }
        }

        ResizeMode.Fill -> Unit
    }

    return this.copy(maxWidth = width, maxHeight = height)
}


/**
 * Will return a timestamp denoting the current video [position] and the [duration] in the following
 * format "mm:ss / mm:ss"
 * **/
fun prettyVideoTimestamp(
    position: Duration,
    duration: Duration
): String = buildString {
    appendMinutesAndSeconds(position)
    append("/")
    appendMinutesAndSeconds(duration)
}

/**
 * Will split [duration] in minutes and seconds and append it to [this] in the following format "mm:ss"
 * */
private fun StringBuilder.appendMinutesAndSeconds(duration: Duration) {
    val minutes = duration.inWholeMinutes
    val seconds = (duration - minutes.minutes).inWholeSeconds
    appendDoubleDigit(minutes)
    append(':')
    appendDoubleDigit(seconds)
}

/**
 * Will append [value] as double digit to [this].
 * If a single digit value is passed, ex: 4 then a 0 will be added as prefix resulting in 04
 * */
private fun StringBuilder.appendDoubleDigit(value: Long) {
    if (value < 10) {
        append(0)
        append(value)
    } else {
        append(value)
    }
}

internal fun mediaItemCreator(uri: String): MediaItem {
    val builder = MediaItem.Builder().setUri(uri)
    if (uri.contains(".m3u8")) {
        builder.setMimeType(MimeTypes.APPLICATION_M3U8)
    }
    return builder.build()
}

internal fun loadControlCreator(): LoadControl {
    return DefaultLoadControl.Builder()
        .setBufferDurationsMs(360000, 600000, 1000, 5000)
        .setBackBuffer(50000, true)
        .build()
}
