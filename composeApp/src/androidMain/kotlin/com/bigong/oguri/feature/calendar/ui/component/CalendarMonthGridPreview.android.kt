package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.CalendarHoliday
import com.bigong.oguri.domain.model.CalendarPeriod
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun CalendarMonthGridPreview() {
    OguriTheme {
        CalendarMonthGrid(
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
                ),
            selectedPeriod = CalendarPeriod(id = 1L, startDate = LocalDate(2026, 2, 28), endDate = LocalDate(2026, 3, 4)),
            selectedDate = LocalDate(2026, 3, 1),
            todayDate = LocalDate(2026, 3, 2),
            onDateClick = {},
        )
    }
}
