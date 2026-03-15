package com.bigong.oguri.feature.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Provider

@Composable
fun CalendarRoute(
    calendarViewModelProvider: Provider<CalendarViewModel>,
    onOpenPeriodDetail: (String, String) -> Unit = { _, _ -> },
) {
    val calendarViewModel: CalendarViewModel = remember {
        calendarViewModelProvider()
    }

    CalendarScreen(
        calendarUiState = calendarViewModel.calendarUiState,
        onLeaveDaysChanged = calendarViewModel::updateLeaveDays,
        onYearMonthSelected = calendarViewModel::updateYearMonth,
        onDateClick = { selectedDate ->
            calendarViewModel.onDateClick(
                date = selectedDate,
                onOpenPeriodDetail = onOpenPeriodDetail,
            )
        },
        onPeriodClick = { selectedPeriod ->
            calendarViewModel.onPeriodClick(
                periodId = selectedPeriod.id,
                onOpenPeriodDetail = onOpenPeriodDetail,
            )
        },
        onRetryClick = calendarViewModel::retry,
    )
}
