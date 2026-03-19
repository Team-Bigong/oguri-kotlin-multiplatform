package com.bigong.oguri.feature.mypage.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.mypage.ui.model.MyPageSideEffect
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_login_required
import oguri.composeapp.generated.resources.snackbar_mypage_leave_days_updated
import oguri.composeapp.generated.resources.snackbar_mypage_saved_place_deleted
import oguri.composeapp.generated.resources.snackbar_mypage_selected_period_deleted
import oguri.composeapp.generated.resources.snackbar_withdraw_failed
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageRoute(
    myPageViewModel: MyPageViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenSuggestion: () -> Unit,
    onOpenTermsOfService: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onLoggedOut: () -> Unit,
    onWithdrawCompleted: () -> Unit,
    onLoginRequired: () -> Unit,
    onPeriodClick: (String, String) -> Unit,
    onSavedPlaceClick: (Long) -> Unit,
) {
    val myPageUiState = myPageViewModel.uiState.collectAsStateWithLifecycle().value
    val leaveDaysUpdatedMessage = stringResource(Res.string.snackbar_mypage_leave_days_updated)
    val selectedPeriodDeletedMessage = stringResource(Res.string.snackbar_mypage_selected_period_deleted)
    val savedPlaceDeletedMessage = stringResource(Res.string.snackbar_mypage_saved_place_deleted)
    val loginRequiredMessage = stringResource(Res.string.snackbar_login_required)
    val withdrawFailedMessage = stringResource(Res.string.snackbar_withdraw_failed)

    LaunchedEffect(myPageViewModel) {
        myPageViewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                MyPageSideEffect.LeaveDaysUpdated -> {
                    snackbarHostState.showOguriSnackbar(
                        message = leaveDaysUpdatedMessage,
                        type = OguriSnackBarType.SUCCESS,
                    )
                }
                MyPageSideEffect.SelectedPeriodDeleted -> {
                    snackbarHostState.showOguriSnackbar(
                        message = selectedPeriodDeletedMessage,
                        type = OguriSnackBarType.INFO,
                    )
                }
                MyPageSideEffect.SavedPlaceDeleted -> {
                    snackbarHostState.showOguriSnackbar(
                        message = savedPlaceDeletedMessage,
                        type = OguriSnackBarType.INFO,
                    )
                }
                MyPageSideEffect.LoggedOut -> {
                    onLoggedOut()
                }
                MyPageSideEffect.WithdrawCompleted -> {
                    onWithdrawCompleted()
                }

                MyPageSideEffect.WithdrawFailed -> {
                    snackbarHostState.showOguriSnackbar(
                        message = withdrawFailedMessage,
                        type = OguriSnackBarType.ALERT,
                    )
                }

                MyPageSideEffect.LoginRequired -> {
                    snackbarHostState.showOguriSnackbar(
                        message = loginRequiredMessage,
                        type = OguriSnackBarType.INFO,
                    )
                    onLoginRequired()
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        myPageViewModel.refreshMyPageInfo()
    }

    MyPageScreen(
        myPageUiState = myPageUiState,
        onRetryClick = myPageViewModel::loadMyPageInfo,
        onEditLeaveDaysClick = myPageViewModel::showEditLeaveDaysBottomSheet,
        onDismissLeaveDaysBottomSheet = myPageViewModel::hideEditLeaveDaysBottomSheet,
        onSubmitLeaveDays = myPageViewModel::updateLeaveDays,
        onDeleteScheduleClick = myPageViewModel::showDeleteScheduleDialog,
        onSelectedPeriodClick = onPeriodClick,
        onDismissDeleteScheduleDialog = myPageViewModel::dismissDeleteScheduleDialog,
        onConfirmDeleteSchedule = myPageViewModel::confirmDeleteSchedule,
        onDeleteSavedPlaceClick = myPageViewModel::showDeleteSavedPlaceDialog,
        onSavedPlaceClick = onSavedPlaceClick,
        onDismissDeleteSavedPlaceDialog = myPageViewModel::dismissDeleteSavedPlaceDialog,
        onConfirmDeleteSavedPlace = myPageViewModel::confirmDeleteSavedPlace,
        onSuggestClick = onOpenSuggestion,
        onTermsOfServiceClick = onOpenTermsOfService,
        onPrivacyPolicyClick = onOpenPrivacyPolicy,
        onWithdrawClick = myPageViewModel::showWithdrawDialog,
        onWithdrawInputChange = myPageViewModel::updateWithdrawInput,
        onDismissWithdrawDialog = myPageViewModel::hideWithdrawDialog,
        onConfirmWithdraw = myPageViewModel::confirmWithdraw,
        onLogoutClick = myPageViewModel::showLogoutDialog,
        onDismissLogoutDialog = myPageViewModel::hideLogoutDialog,
        onConfirmLogout = myPageViewModel::confirmLogout,
    )
}
