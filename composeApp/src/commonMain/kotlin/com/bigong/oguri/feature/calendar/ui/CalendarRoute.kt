package com.bigong.oguri.feature.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.calendar.ui.model.CalendarSideEffect
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_calendar_leave_days_updated
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarRoute(
    calendarViewModelProvider: Provider<CalendarViewModel>,
    snackbarHostState: SnackbarHostState,
    onOpenPeriodDetail: (String, String) -> Unit = { _, _ -> },
) {
    val calendarViewModel: CalendarViewModel = remember {
        calendarViewModelProvider()
    }
    val calendarUiState = calendarViewModel.uiState.collectAsStateWithLifecycle().value
    val leaveDaysUpdatedMessage: String = stringResource(Res.string.snackbar_calendar_leave_days_updated)

    LaunchedEffect(calendarViewModel) {
        calendarViewModel.sideEffect.collectLatest { sideEffect: CalendarSideEffect ->
            when (sideEffect) {
                CalendarSideEffect.LeaveDaysUpdated -> {
                    snackbarHostState.showOguriSnackbar(
                        message = leaveDaysUpdatedMessage,
                        type = OguriSnackBarType.INFO,
                    )
                }
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
