package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun PeriodDetailScreenPreview() {
    OguriTheme {
        PeriodDetailScreen(
            periodId = 1L,
            onBackClick = {},
        )
    }
}
