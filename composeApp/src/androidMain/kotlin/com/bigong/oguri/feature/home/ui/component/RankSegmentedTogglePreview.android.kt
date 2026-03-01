package com.bigong.oguri.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun RankSegmentedTogglePreview() {
    OguriTheme {
        RankSegmentedToggle(
            selectedRank = 1,
            rankLabels = listOf("1순위", "2순위", "3순위"),
            onRankSelected = {},
        )
    }
}
