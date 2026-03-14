package com.bigong.oguri.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_binoculars

@Preview(showBackground = true)
@Composable
private fun GuideHeaderPreview() {
    OguriTheme {
        GuideHeader(
            iconResource = Res.drawable.ic_binoculars,
            titleText = "이때 가면 딱 좋은 곳들이에요",
            highlightedText = "딱 좋은 곳",
            subtitleText = "마음에 드는 카드를 눌러보세요",
        )
    }
}
