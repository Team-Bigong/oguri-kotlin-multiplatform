package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun PlaceDetailTopBarCollapsedPreview() {
    OguriTheme {
        PlaceDetailTopBar(
            city = "보라카이",
            isSaved = false,
            isCollapsed = true,
            onBackClick = {},
            onShareClick = {},
            onSaveToggleClick = {},
        )
    }
}
