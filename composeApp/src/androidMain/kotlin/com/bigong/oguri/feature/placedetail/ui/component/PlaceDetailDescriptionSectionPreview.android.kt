package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun PlaceDetailDescriptionSectionPreview() {
    OguriTheme {
        PlaceDetailDescriptionSection(
            description = "화이트 비치로 유명한 보라카이, **휴식에 집중하고 싶을 때** 특히 잘 어울리는 곳이에요.",
        )
    }
}
