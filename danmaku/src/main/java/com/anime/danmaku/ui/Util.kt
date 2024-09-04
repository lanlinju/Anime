package com.anime.danmaku.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.round

/**
 * Equivalent to `String.format("%.2f", value)`
 */
internal fun String.Companion.format2f(value: Float): String {
    return (round(value * 100) / 100.0).toString()
}

@Composable
internal fun isInLandscapeMode(): Boolean =
    LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE