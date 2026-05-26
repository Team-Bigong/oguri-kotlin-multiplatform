package com.bigong.oguri.core.platform

import androidx.appcompat.app.AppCompatDelegate

actual fun applyPlatformThemeMode(
    isDarkThemeEnabled: Boolean,
    shouldFollowSystemTheme: Boolean,
) {
    val nightMode =
        if (shouldFollowSystemTheme) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else if (isDarkThemeEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
    AppCompatDelegate.setDefaultNightMode(nightMode)
}
