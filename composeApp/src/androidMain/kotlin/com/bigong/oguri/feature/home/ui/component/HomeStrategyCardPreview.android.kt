package com.bigong.oguri.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.previewRecommendPeriod

@Preview(showBackground = true)
@Composable
private fun HomeStrategyCardPreview() {
    OguriTheme {
        HomeStrategyCard(
            selectedRank = 1,
            savedRankSet = setOf(1),
            rankLabels = listOf("1순위", "2순위", "3순위"),
            currentPeriod = previewRecommendPeriod,
            onRankSelected = {},
            onSavedChanged = {},
        )
    }
}
