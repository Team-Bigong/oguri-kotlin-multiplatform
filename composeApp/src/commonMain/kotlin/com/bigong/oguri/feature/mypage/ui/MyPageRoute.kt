package com.bigong.oguri.feature.mypage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.mypage.ui.model.MyPageSideEffect
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_mypage_saved_place_deleted
import oguri.composeapp.generated.resources.snackbar_mypage_leave_days_updated
import oguri.composeapp.generated.resources.snackbar_mypage_selected_period_deleted
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageRoute(
    myPageViewModelProvider: Provider<MyPageViewModel>,
    snackbarHostState: SnackbarHostState,
    onOpenSuggestion: () -> Unit,
    onOpenTermsOfService: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onLoggedOut: () -> Unit,
    onPeriodClick: (String, String) -> Unit,
) {
    val myPageViewModel: MyPageViewModel = remember {
        myPageViewModelProvider()
    }
    val myPageUiState = myPageViewModel.uiState.collectAsStateWithLifecycle().value
    val leaveDaysUpdatedMessage: String = stringResource(Res.string.snackbar_mypage_leave_days_updated)
    val selectedPeriodDeletedMessage: String = stringResource(Res.string.snackbar_mypage_selected_period_deleted)
    val savedPlaceDeletedMessage: String = stringResource(Res.string.snackbar_mypage_saved_place_deleted)

    LaunchedEffect(myPageViewModel) {
        myPageViewModel.sideEffect.collectLatest { sideEffect: MyPageSideEffect ->
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
            }
        }
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
        onDismissDeleteSavedPlaceDialog = myPageViewModel::dismissDeleteSavedPlaceDialog,
        onConfirmDeleteSavedPlace = myPageViewModel::confirmDeleteSavedPlace,
        onSuggestClick = onOpenSuggestion,
        onTermsOfServiceClick = onOpenTermsOfService,
        onPrivacyPolicyClick = onOpenPrivacyPolicy,
        onWithdrawClick = {},
        onLogoutClick = myPageViewModel::showLogoutDialog,
        onDismissLogoutDialog = myPageViewModel::hideLogoutDialog,
        onConfirmLogout = myPageViewModel::confirmLogout,
    )
}
