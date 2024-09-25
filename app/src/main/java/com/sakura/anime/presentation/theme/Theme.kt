package com.sakura.anime.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.sakura.anime.util.SettingsPreferences
import com.sakura.anime.util.getSchemeFromSeed

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AnimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    customColorScheme: ColorScheme = LightColorScheme,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> customColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            activity.window.statusBarColor = colorScheme.background.toArgb()
            activity.window.navigationBarColor =
                colorScheme.surfaceColorAtElevation(5.dp).toArgb()
            val insetsController = WindowCompat.getInsetsController(
                activity.window,
                view
            )
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun AnimeTheme(content: @Composable () -> Unit) {
    val themeModeState by SettingsPreferences.themeMode.collectAsState()
    val customColor by SettingsPreferences.customColor.collectAsState()
    val dynamicColor by SettingsPreferences.dynamicColor.collectAsState()

    val darkTheme = when (themeModeState) {
        SettingsPreferences.ThemeMode.SYSTEM -> isSystemInDarkTheme()
        SettingsPreferences.ThemeMode.DARK -> true
        else -> false
    }

    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // 当前系统版本大于或等于Android 8.0
        getSchemeFromSeed(customColor, darkTheme)
    } else {
        // 当前系统版本小于Android 8.0
        LightColorScheme
    }

    AnimeTheme(
        darkTheme = darkTheme,
        customColorScheme = colorScheme,
        dynamicColor = dynamicColor,
        content = content
    )
}