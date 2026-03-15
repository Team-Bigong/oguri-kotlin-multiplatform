package com.bigong.oguri.feature.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.feature.calendar.ui.model.CalendarSideEffect
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CalendarRoute(
    calendarViewModelProvider: Provider<CalendarViewModel>,
    onOpenPeriodDetail: (String, String) -> Unit = { _, _ -> },
) {
    val calendarViewModel: CalendarViewModel = remember {
        calendarViewModelProvider()
    }
    val calendarUiState = calendarViewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(calendarViewModel) {
        calendarViewModel.sideEffect.collectLatest { sideEffect: CalendarSideEffect ->
            when (sideEffect) {
                is CalendarSideEffect.NavigateToPeriodDetail -> {
                    onOpenPeriodDetail(sideEffect.startDate, sideEffect.endDate)
                }
            }
        }
    }

    CalendarScreen(
        calendarUiState = calendarUiState,
        onLeaveDaysChanged = calendarViewModel::updateLeaveDays,
        onYearMonthSelected = calendarViewModel::updateYearMonth,
        onDateClick = { selectedDate ->
            calendarViewModel.onDateClick(date = selectedDate)
        },
        onPeriodClick = { selectedPeriod ->
            calendarViewModel.onPeriodClick(periodId = selectedPeriod.id)
        },
        onRetryClick = calendarViewModel::retry,
    )
}
