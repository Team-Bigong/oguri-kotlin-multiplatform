package com.bigong.oguri.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderScreenFrame
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall

@Composable
fun HomeRoute(
    onStrategyDetailClick: () -> Unit,
    onShowSnackbarClick: () -> Unit,
) {
    HomeScreen(
        onStrategyDetailClick = onStrategyDetailClick,
        onShowSnackbarClick = onShowSnackbarClick,
    )
}

@Composable
fun HomeScreen(
    onStrategyDetailClick: () -> Unit,
    onShowSnackbarClick: () -> Unit,
) {
    PlaceholderScreenFrame(
        screenTitleText = "홈",
        screenSubtitleText = "최고 전략 1개 강조 (MVP 핵심 화면)",
    ) {
        PlaceholderInfoCard(
            lines = listOf(
                "🔥 10월 6일 ~ 10월 12일",
                "연차 2일 → 7일 확보",
            ),
        )
        Spacer(modifier = Modifier.height(PlaceholderSpacingMedium))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PlaceholderActionButton(
                    labelText = "전략 상세 보기",
                    onClick = onStrategyDetailClick,
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                PlaceholderActionButton(
                    labelText = "상단 스낵바 테스트",
                    onClick = onShowSnackbarClick,
                    emphasized = false,
                )
            }
        }
    }
}
