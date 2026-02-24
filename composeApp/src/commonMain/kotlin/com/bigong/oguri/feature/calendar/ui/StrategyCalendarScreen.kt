package com.bigong.oguri.feature.calendar.ui

import androidx.compose.runtime.Composable
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderScreenFrame

@Composable
fun StrategyCalendarRoute() {
    StrategyCalendarScreen()
}

@Composable
fun StrategyCalendarScreen() {
    PlaceholderScreenFrame(
        screenTitleText = "전략 캘린더",
        screenSubtitleText = "월별 전략 달력 (플레이스홀더)",
    ) {
        PlaceholderInfoCard(
            lines = listOf(
                "🔥 5월 - 4일 확보 가능",
                "🔥 9월 - 8일 확보 가능",
                "🔥 10월 - 7일 확보 가능",
            ),
        )
    }
}
