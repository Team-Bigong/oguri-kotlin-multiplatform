package com.bigong.oguri.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun HomeGreetingSectionPreview() {
    OguriTheme {
        HomeGreetingSection(
            nameText = "오 조합, 오구리만 알고 있었어요",
            questionText = "이번엔 이렇게 쉬어볼까요?",
        )
    }
}
