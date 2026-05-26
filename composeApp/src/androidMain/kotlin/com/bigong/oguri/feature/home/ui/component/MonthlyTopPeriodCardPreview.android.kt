package com.bigong.oguri.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.MonthlyTopPeriod
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun MonthlyTopPeriodCardPreview() {
    OguriTheme {
        MonthlyTopPeriodCard(
            period =
                MonthlyTopPeriod(
                    rank = 1,
                    startDate = LocalDate.parse("2026-02-28"),
                    endDate = LocalDate.parse("2026-03-04"),
                    totalTripCount = 5,
                    holidayCount = 3,
                    dayOffCount = 2,
                ),
            onClick = {},
            modifier = Modifier,
        )
    }
}
