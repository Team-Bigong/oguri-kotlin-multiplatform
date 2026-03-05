package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.CalendarPeriod
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun CalendarRecommendationSectionPreview() {
    OguriTheme {
        CalendarRecommendationSection(
            periods =
                listOf(
                    CalendarPeriod(id = 1L, startDate = LocalDate(2026, 2, 28), endDate = LocalDate(2026, 3, 4)),
                    CalendarPeriod(id = 2L, startDate = LocalDate(2026, 3, 7), endDate = LocalDate(2026, 3, 10)),
                ),
            selectedPeriodId = 1L,
            onPeriodClick = {},
        )
    }
}
