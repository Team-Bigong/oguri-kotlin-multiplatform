package com.bigong.oguri.feature.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.CalendarHoliday
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    OguriTheme {
        CalendarScreen(
            calendarUiState =
                CalendarUiState(
                    isLoading = false,
                    periodCards = previewPeriodCards(),
                    expandedPeriodId = 1L,
                    selectedDateByPeriodId = mapOf(1L to LocalDate.parse("2026-02-28")),
                    hasMorePage = false,
                ),
            onLeaveDaysChanged = {},
            onCardClick = {},
            onDetailClick = {},
            onLoadNextPage = {},
            onRetryClick = {},
        )
    }
}

private fun previewPeriodCards(): List<CalendarPeriodCardUiModel> =
    listOf(
        CalendarPeriodCardUiModel(
            id = 1L,
            startDate = LocalDate.parse("2026-02-28"),
            endDate = LocalDate.parse("2026-03-04"),
            dDay = 10,
            dayOffCount = 2,
            totalTripCount = 5,
            holidayNames = listOf("삼일절", "대체휴일"),
            holidays =
                listOf(
                    CalendarHoliday(LocalDate.parse("2026-03-01"), "삼일절"),
                    CalendarHoliday(LocalDate.parse("2026-03-02"), "대체휴일"),
                ),
        ),
        CalendarPeriodCardUiModel(
            id = 2L,
            startDate = LocalDate.parse("2026-03-07"),
            endDate = LocalDate.parse("2026-03-10"),
            dDay = 17,
            dayOffCount = 2,
            totalTripCount = 4,
            holidayNames = listOf("주말"),
            holidays = listOf(CalendarHoliday(LocalDate.parse("2026-03-08"), "주말")),
        ),
    )
