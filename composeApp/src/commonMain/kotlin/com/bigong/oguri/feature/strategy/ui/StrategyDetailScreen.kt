package com.bigong.oguri.feature.strategy.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderScreenFrame
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium

@Composable
fun StrategyDetailRoute(
    strategyIdentifier: String,
    onCalendarClick: () -> Unit,
) {
    StrategyDetailScreen(
        strategyIdentifier = strategyIdentifier,
        onCalendarClick = onCalendarClick,
    )
}

@Composable
fun StrategyDetailScreen(
    strategyIdentifier: String,
    onCalendarClick: () -> Unit,
) {
    PlaceholderScreenFrame(
        screenTitleText = "전략 상세",
        screenSubtitleText = "전략 ID: $strategyIdentifier",
    ) {
        PlaceholderInfoCard(
            lines = listOf(
                "연차 사용: 10/6, 10/7",
                "총 확보: 7일",
                "하단 배너 광고 영역(추후)",
            ),
        )
        Spacer(modifier = Modifier.height(PlaceholderSpacingMedium))
        PlaceholderActionButton(
            labelText = "전략 캘린더 보기",
            onClick = onCalendarClick,
            emphasized = false,
        )
    }
}
