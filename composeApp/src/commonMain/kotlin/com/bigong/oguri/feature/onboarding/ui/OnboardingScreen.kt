package com.bigong.oguri.feature.onboarding.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderScreenFrame
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium

@Composable
fun OnboardingRoute(
    onCalculateStrategyClick: () -> Unit,
) {
    OnboardingScreen(onCalculateStrategyClick = onCalculateStrategyClick)
}

@Composable
fun OnboardingScreen(
    onCalculateStrategyClick: () -> Unit,
) {
    PlaceholderScreenFrame(
        screenTitleText = "온보딩",
        screenSubtitleText = "직군 / 연차 / 근무 형태 입력",
    ) {
        PlaceholderInfoCard(
            lines = listOf(
                "직장인 / 학생 선택",
                "남은 연차 15일",
                "주 5일 / 주 6일",
            ),
        )
        Spacer(modifier = Modifier.height(PlaceholderSpacingMedium))
        PlaceholderActionButton(
            labelText = "전략 계산하기",
            onClick = onCalculateStrategyClick,
        )
    }
}
