package com.anime.danmaku.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.round

internal var LOG_ENABLE = false

internal const val LOG_TAG = "Danmaku"

internal fun <T> T.log(prefix: String = ""): T {
    val prefixStr = if (prefix.isEmpty()) "" else "[$prefix] "
    if (LOG_ENABLE) {
        if (this is Throwable) {
            Log.w(LOG_TAG, prefixStr + this.message, this)
        } else {
            Log.d(LOG_TAG, prefixStr + toString())
        }
    }
    return this
}

/**
 * Equivalent to `String.format("%.2f", value)`
 */
internal fun String.Companion.format2f(value: Float): String {
    return (round(value * 100) / 100.0).toString()
}

@Composable
internal fun isInLandscapeMode(): Boolean =
    LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE