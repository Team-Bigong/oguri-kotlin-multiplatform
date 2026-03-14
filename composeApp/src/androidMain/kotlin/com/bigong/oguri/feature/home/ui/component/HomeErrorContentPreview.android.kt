package com.bigong.oguri.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun HomeErrorContentPreview() {
    OguriTheme {
        HomeErrorContent(
            message = "다시 시도해주세요",
            retryText = "다시 시도",
            onRetryClick = {},
        )
    }
}
