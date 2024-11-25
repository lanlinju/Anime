package com.lanlinju.animius.util

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.SchemeVibrant

// 参考 https://github.com/you-apps/VibeYou/blob/c5c7eb3e9a2228b715689d9524cdc98db139287d/app/src/main/java/app/suhasdissa/vibeyou/utils/ThemeUtil.kt#L10
@SuppressLint("RestrictedApi")
fun getSchemeFromSeed(color: Int, dark: Boolean): ColorScheme {
    val hct = Hct.fromInt(color)
    return SchemeVibrant(hct, dark, 0.0).toColorScheme()
}

val catpucchinLatte = arrayOf(
    android.graphics.Color.rgb(220, 138, 120),
    android.graphics.Color.rgb(221, 120, 120),
    android.graphics.Color.rgb(234, 118, 203),
    android.graphics.Color.rgb(136, 77, 210),
    android.graphics.Color.rgb(210, 65, 57),
    android.graphics.Color.rgb(230, 69, 83),
    android.graphics.Color.rgb(254, 100, 11),
    android.graphics.Color.rgb(223, 142, 29),
    android.graphics.Color.rgb(64, 160, 43),
    android.graphics.Color.rgb(23, 146, 153),
    android.graphics.Color.rgb(4, 165, 229),
    android.graphics.Color.rgb(32, 159, 181),
    android.graphics.Color.rgb(30, 102, 245),
    android.graphics.Color.rgb(114, 135, 253),
)

@SuppressLint("RestrictedApi")
fun DynamicScheme.toColorScheme() = ColorScheme(
    primary = Color(primary),
    onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer),
    onPrimaryContainer = Color(onPrimaryContainer),
    inversePrimary = Color(inversePrimary),
    secondary = Color(secondary),
    onSecondary = Color(onSecondary),
    secondaryContainer = Color(secondaryContainer),
    onSecondaryContainer = Color(onSecondaryContainer),
    tertiary = Color(tertiary),
    onTertiary = Color(onTertiary),
    tertiaryContainer = Color(tertiaryContainer),
    onTertiaryContainer = Color(onTertiaryContainer),
    background = Color(background),
    onBackground = Color(onBackground),
    surface = Color(surface),
    onSurface = Color(onSurface),
    surfaceVariant = Color(surfaceVariant),
    onSurfaceVariant = Color(onSurfaceVariant),
    surfaceTint = Color(surfaceTint),
    inverseSurface = Color(inverseSurface),
    inverseOnSurface = Color(inverseOnSurface),
    error = Color(error),
    onError = Color(onError),
    errorContainer = Color(errorContainer),
    onErrorContainer = Color(onErrorContainer),
    outline = Color(outline),
    outlineVariant = Color(outlineVariant),
    scrim = Color(scrim),
    surfaceBright = Color(surfaceBright),
    surfaceDim = Color(surfaceDim),
    surfaceContainer = Color(surfaceContainer),
    surfaceContainerHigh = Color(surfaceContainerHigh),
    surfaceContainerHighest = Color(surfaceContainerHighest),
    surfaceContainerLow = Color(surfaceContainerLow),
    surfaceContainerLowest = Color(surfaceContainerLowest),
)

suspend fun dynamicColorOf(bitmap: Bitmap): Color? {
    val palette = Palette
        .from(bitmap)
        .maximumColorCount(8)
        .generate()

    val dominantSwatch = palette.dominantSwatch ?: return null

    return Color(dominantSwatch.rgb)
}