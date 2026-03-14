package com.bigong.oguri.feature.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.CalendarHoliday
import com.bigong.oguri.domain.model.CalendarPeriod
import com.bigong.oguri.domain.model.CalendarRecommendation
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import kotlinx.datetime.LocalDate

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun CalendarScreenPreview() {
    OguriTheme {
        CalendarScreen(
            calendarUiState =
                CalendarUiState(
                    isLoading = false,
                    leaveDays = 3,
                    selectedYear = 2026,
                    selectedMonth = 3,
                    selectedDate = LocalDate(2026, 2, 28),
                    selectedPeriodId = 1L,
                    calendarRecommendation =
                        CalendarRecommendation(
                            leaveDays = 3,
                            year = 2026,
                            month = 3,
                            holidays =
                                listOf(
                                    CalendarHoliday(date = LocalDate(2026, 3, 1), name = "삼일절"),
                                    CalendarHoliday(date = LocalDate(2026, 3, 2), name = "대체휴일"),
                                ),
                            periods =
                                listOf(
                                    CalendarPeriod(id = 1L, startDate = LocalDate(2026, 2, 28), endDate = LocalDate(2026, 3, 4)),
                                    CalendarPeriod(id = 2L, startDate = LocalDate(2026, 3, 7), endDate = LocalDate(2026, 3, 10)),
                                    CalendarPeriod(id = 3L, startDate = LocalDate(2026, 3, 12), endDate = LocalDate(2026, 3, 15)),
                                ),
                        ),
                ),
            onLeaveDaysChanged = {},
            onYearMonthSelected = { _, _ -> },
            onDateClick = {},
            onPeriodClick = {},
            onRetryClick = {},
        )
    }
}
