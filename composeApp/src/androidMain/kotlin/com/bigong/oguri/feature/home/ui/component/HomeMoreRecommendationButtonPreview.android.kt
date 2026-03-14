package com.bigong.oguri.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun HomeMoreRecommendationButtonPreview() {
    OguriTheme {
        HomeMoreRecommendationButton(
            subtitleText = "딱 맞는 연휴가 없었나요?",
            text = "다른 추천 보기",
        )
    }
}
