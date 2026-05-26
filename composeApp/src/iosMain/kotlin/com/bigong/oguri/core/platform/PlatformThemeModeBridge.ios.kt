package com.bigong.oguri.core.platform

import platform.Foundation.NSNotificationCenter

private const val IOS_NOTIFICATION_APPLY_THEME_MODE = "OguriApplyThemeMode"

actual fun applyPlatformThemeMode(
    isDarkThemeEnabled: Boolean,
    shouldFollowSystemTheme: Boolean,
) {
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = IOS_NOTIFICATION_APPLY_THEME_MODE,
        `object` = null,
        userInfo =
            mapOf(
                "isDarkThemeEnabled" to isDarkThemeEnabled,
                "shouldFollowSystemTheme" to shouldFollowSystemTheme,
            ),
    )
}
