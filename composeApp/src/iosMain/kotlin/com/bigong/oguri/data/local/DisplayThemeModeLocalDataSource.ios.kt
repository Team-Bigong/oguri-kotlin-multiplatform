package com.bigong.oguri.data.local

import com.bigong.oguri.domain.model.DisplayThemeMode
import platform.Foundation.NSUserDefaults

private const val KEY_DISPLAY_THEME_MODE = "key_display_theme_mode"

private var displayThemeModeLocalDataSourceInstance: DisplayThemeModeLocalDataSource? = null

private class IosDisplayThemeModeLocalDataSource : DisplayThemeModeLocalDataSource {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun initialize() = Unit

    override fun readDisplayThemeMode(): DisplayThemeMode? {
        val displayThemeModeName = userDefaults.stringForKey(KEY_DISPLAY_THEME_MODE)
        if (displayThemeModeName.isNullOrBlank()) {
            return null
        }

        return runCatching { DisplayThemeMode.valueOf(displayThemeModeName) }.getOrNull()
    }

    override fun writeDisplayThemeMode(displayThemeMode: DisplayThemeMode) {
        userDefaults.setObject(displayThemeMode.name, forKey = KEY_DISPLAY_THEME_MODE)
    }
}

actual fun provideDisplayThemeModeLocalDataSource(): DisplayThemeModeLocalDataSource {
    val existingInstance = displayThemeModeLocalDataSourceInstance
    if (existingInstance != null) {
        return existingInstance
    }

    val newInstance = IosDisplayThemeModeLocalDataSource()
    displayThemeModeLocalDataSourceInstance = newInstance
    return newInstance
}
