package com.bigong.oguri.feature.mypage.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.DisplayThemeMode

@Preview(showBackground = true)
@Composable
private fun DisplaySettingsScreenPreview() {
    OguriTheme {
        DisplaySettingsScreen(
            titleText = "디스플레이 설정",
            followSystemText = "시스템 설정에 따름",
            lightModeText = "라이트 모드",
            darkModeText = "다크 모드",
            currentDisplayThemeMode = DisplayThemeMode.SYSTEM,
            onDisplayThemeModeChange = {},
            onBackClick = {},
        )
    }
}
