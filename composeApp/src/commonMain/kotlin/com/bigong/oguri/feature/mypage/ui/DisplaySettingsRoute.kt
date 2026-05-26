package com.bigong.oguri.feature.mypage.ui

import androidx.compose.runtime.Composable
import com.bigong.oguri.domain.model.DisplayThemeMode
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.display_settings_dark_mode
import oguri.composeapp.generated.resources.display_settings_follow_system
import oguri.composeapp.generated.resources.display_settings_light_mode
import oguri.composeapp.generated.resources.mypage_menu_display_settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun DisplaySettingsRoute(
    currentDisplayThemeMode: DisplayThemeMode,
    onDisplayThemeModeChange: (DisplayThemeMode) -> Unit,
    onBackClick: () -> Unit,
) {
    DisplaySettingsScreen(
        titleText = stringResource(Res.string.mypage_menu_display_settings),
        followSystemText = stringResource(Res.string.display_settings_follow_system),
        lightModeText = stringResource(Res.string.display_settings_light_mode),
        darkModeText = stringResource(Res.string.display_settings_dark_mode),
        currentDisplayThemeMode = currentDisplayThemeMode,
        onDisplayThemeModeChange = onDisplayThemeModeChange,
        onBackClick = onBackClick,
    )
}
