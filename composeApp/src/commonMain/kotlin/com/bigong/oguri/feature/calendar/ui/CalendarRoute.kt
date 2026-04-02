package com.bigong.oguri.feature.calendar.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.bigong.oguri.core.analytics.OguriAnalyticsEvent
import com.bigong.oguri.core.analytics.OguriAnalyticsProperty
import com.bigong.oguri.core.analytics.trackOguriEvent
import com.bigong.oguri.core.ui.component.LoginRequiredDialog
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.calendar.ui.model.CalendarSideEffect
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_calendar_leave_days_updated
import oguri.composeapp.generated.resources.snackbar_home_deleted
import oguri.composeapp.generated.resources.snackbar_home_saved
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarRoute(
    calendarViewModel: CalendarViewModel,
    snackbarHostState: SnackbarHostState,
    onLoginRequired: () -> Unit,
    onOpenPeriodDetail: (String, String) -> Unit = { _, _ -> },
) {
    val calendarUiState = calendarViewModel.uiState.collectAsStateWithLifecycle().value
    val pagedPeriodCards = calendarViewModel.pagedPeriodCards.collectAsLazyPagingItems()
    val leaveDaysUpdatedMessage = stringResource(Res.string.snackbar_calendar_leave_days_updated)
    val recommendationSavedMessage = stringResource(Res.string.snackbar_home_saved)
    val recommendationDeletedMessage = stringResource(Res.string.snackbar_home_deleted)
    var isLoginRequiredDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(calendarViewModel) {
        calendarViewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                CalendarSideEffect.LeaveDaysUpdated -> {
                    snackbarHostState.showOguriSnackbar(
                        message = leaveDaysUpdatedMessage,
                        type = OguriSnackBarType.INFO,
                    )
                }

                CalendarSideEffect.RecommendationSaved -> {
                    snackbarHostState.showOguriSnackbar(
                        message = recommendationSavedMessage,
                        type = OguriSnackBarType.SUCCESS,
                    )
                }

                CalendarSideEffect.RecommendationDeleted -> {
                    snackbarHostState.showOguriSnackbar(
                        message = recommendationDeletedMessage,
                        type = OguriSnackBarType.INFO,
                    )
                }
                CalendarSideEffect.LoginRequired -> {
                    isLoginRequiredDialogVisible = true
                }

                is CalendarSideEffect.NavigateToPeriodDetail -> {
                    onOpenPeriodDetail(sideEffect.startDate, sideEffect.endDate)
                }
            }
        }
    }

    CalendarScreen(
        calendarUiState = calendarUiState,
        pagedPeriodCards = pagedPeriodCards,
        savedStateByPeriodKey = calendarUiState.savedStateByPeriodKey,
        onLeaveDaysChanged = { leaveDays ->
            trackOguriEvent(
                eventName = OguriAnalyticsEvent.CALENDAR_LEAVE_DAYS_CHANGED,
                eventProperties =
                    mapOf(
                        OguriAnalyticsProperty.LEAVE_DAYS to leaveDays.toString(),
                    ),
            )
            calendarViewModel.updateLeaveDays(leaveDays)
        },
        onPeriodFilterChanged = { year: Int, month: Int? ->
            calendarViewModel.updatePeriodFilter(
                year = year,
                month = month,
            )
        },
        onCardClick = calendarViewModel::onCardClick,
        onSaveToggleClick = calendarViewModel::toggleSaved,
        onDetailClick = { periodId ->
            trackOguriEvent(
                eventName = OguriAnalyticsEvent.CALENDAR_PERIOD_DETAIL_CLICKED,
                eventProperties =
                    mapOf(
                        OguriAnalyticsProperty.PERIOD_ID to periodId.toString(),
                    ),
            )
            calendarViewModel.onDetailClick(periodId)
        },
    )

    if (isLoginRequiredDialogVisible) {
        LoginRequiredDialog(
            onDismissRequest = {
                isLoginRequiredDialogVisible = false
            },
            onLoginClick = {
                isLoginRequiredDialogVisible = false
                onLoginRequired()
            },
        )
    }
}
