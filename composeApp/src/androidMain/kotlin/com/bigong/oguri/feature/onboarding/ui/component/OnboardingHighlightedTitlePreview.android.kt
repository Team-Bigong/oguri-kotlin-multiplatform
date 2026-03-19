package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun OnboardingHighlightedTitlePreview() {
    OguriTheme {
        OnboardingHighlightedTitle(
            fullText = "시작하기 전,\n이용약관에 동의해주세요",
            highlightedText = "이용약관",
        )
    }
}
