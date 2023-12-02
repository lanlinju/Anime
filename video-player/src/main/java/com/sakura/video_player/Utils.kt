package com.sakura.video_player

import androidx.compose.ui.unit.Constraints
import com.google.android.exoplayer2.video.VideoSize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal inline fun VideoSize.aspectRatio(): Float =
    if (height == 0 || width == 0) 0f else (width * pixelWidthHeightRatio) / height


internal inline fun Constraints.resizeForVideo(
    aspectRatio: Float
): Constraints {
    val height = maxHeight
    val width = if (aspectRatio <= 0f) {
        (height * 1.778f).toInt() // default 16 : 9
    } else {
        (height * aspectRatio).toInt()
    }
    return this.copy(maxWidth = width, maxHeight = height)
}


/**
 * Will return a timestamp denoting the current video [position] and the [duration] in the following
 * format "mm:ss / mm:ss"
 * **/
internal inline fun prettyVideoTimestamp(
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
