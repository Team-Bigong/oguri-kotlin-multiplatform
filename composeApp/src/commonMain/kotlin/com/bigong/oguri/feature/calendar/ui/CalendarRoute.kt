package com.bigong.oguri.feature.calendar.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.calendar.ui.model.CalendarSideEffect
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_calendar_leave_days_updated
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarRoute(
    calendarViewModel: CalendarViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenPeriodDetail: (String, String) -> Unit = { _, _ -> },
) {
    val calendarUiState = calendarViewModel.uiState.collectAsStateWithLifecycle().value
    val leaveDaysUpdatedMessage = stringResource(Res.string.snackbar_calendar_leave_days_updated)

    LaunchedEffect(calendarViewModel) {
        calendarViewModel.sideEffect.collectLatest { sideEffect ->
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
        onCardClick = calendarViewModel::onCardClick,
        onDetailClick = calendarViewModel::onDetailClick,
        onLoadNextPage = calendarViewModel::loadNextPage,
        onRetryClick = calendarViewModel::retry,
    )
}
