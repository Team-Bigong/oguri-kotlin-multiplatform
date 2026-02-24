package com.bigong.oguri.feature.splash.ui

import androidx.compose.runtime.Composable
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderScreenFrame

@Composable
fun SplashRoute(
    onStartClick: () -> Unit,
) {
    SplashScreen(onStartClick = onStartClick)
}

@Composable
fun SplashScreen(
    onStartClick: () -> Unit,
) {
    PlaceholderScreenFrame(
        screenTitleText = "🦆 오구리",
        screenSubtitleText = "연차를 전략적으로 쓰면\n여행이 달라집니다.",
    ) {
        PlaceholderActionButton(
            labelText = "시작하기",
            onClick = onStartClick,
        )
    }
}
