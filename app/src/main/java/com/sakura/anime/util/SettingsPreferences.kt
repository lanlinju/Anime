package com.sakura.anime.util

import androidx.annotation.StringRes
import androidx.core.content.edit
import com.sakura.anime.R
import com.sakura.anime.application.AnimeApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsPreferences {
    enum class ThemeMode(@StringRes val resId: Int) {
        LIGHT(R.string.light), DARK(R.string.dark), SYSTEM(R.string.system),
    }

    private val preferences = AnimeApplication.getInstance().preferences

    private val _themeMode = MutableStateFlow(preferences.getEnum(KEY_THEME_MODE, ThemeMode.SYSTEM))
    val themeMode = _themeMode.asStateFlow()

    private val _customColor =
        MutableStateFlow(preferences.getInt(KEY_CUSTOM_COLOR, catpucchinLatte.first()))
    val customColor = _customColor.asStateFlow()

    private val _dynamicColor = MutableStateFlow(preferences.getBoolean(KEY_DYNAMIC_COLOR, true))
    val dynamicColor = _dynamicColor.asStateFlow()


    fun changeThemeMode(themeMode: ThemeMode) {
        _themeMode.value = themeMode
        preferences.edit { putEnum(KEY_THEME_MODE, themeMode) }
    }

    fun changeCustomColor(customColor: Int) {
        _customColor.value = customColor
        preferences.edit { putInt(KEY_CUSTOM_COLOR, customColor) }
    }

    fun changeDynamicColor(dynamicTheme: Boolean) {
        _dynamicColor.value = dynamicTheme
        preferences.edit { putBoolean(KEY_DYNAMIC_COLOR, dynamicTheme) }
    }
}