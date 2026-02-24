package com.bigong.oguri.feature.login.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderScreenFrame
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall

@Composable
fun LoginRoute(
    onKakaoStartClick: () -> Unit,
    onGuestBrowseClick: () -> Unit,
) {
    LoginScreen(
        onKakaoStartClick = onKakaoStartClick,
        onGuestBrowseClick = onGuestBrowseClick,
    )
}

@Composable
fun LoginScreen(
    onKakaoStartClick: () -> Unit,
    onGuestBrowseClick: () -> Unit,
) {
    PlaceholderScreenFrame(
        screenTitleText = "로그인",
        screenSubtitleText = "3초 만에 전략 확인하기",
    ) {
        PlaceholderActionButton(
            labelText = "카카오로 시작하기",
            onClick = onKakaoStartClick,
        )
        Spacer(modifier = Modifier.height(PlaceholderSpacingSmall))
        PlaceholderActionButton(
            labelText = "게스트로 둘러보기",
            onClick = onGuestBrowseClick,
            emphasized = false,
        )
    }
}
