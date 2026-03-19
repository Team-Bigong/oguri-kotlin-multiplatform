package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun CalendarRecommendationCardPreview() {
    OguriTheme {
        CalendarRecommendationCard(
            periodCard =
                CalendarPeriodCardUiModel(
                    id = 1L,
                    startDate = LocalDate.parse("2026-02-28"),
                    endDate = LocalDate.parse("2026-03-04"),
                    dDay = 10,
                    isSaved = true,
                    dayOffCount = 2,
                    totalTripCount = 5,
                    holidayNames = listOf("삼일절", "대체휴일"),
                    holidays = emptyList(),
                ),
            isExpanded = true,
            listViewportBottomInWindow = 2_000f,
            onCardClick = {},
            onSaveToggleClick = {},
            onDetailClick = {},
            onRequestScrollBy = {},
        )
    }
}
