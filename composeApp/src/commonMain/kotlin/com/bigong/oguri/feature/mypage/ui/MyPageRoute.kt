package com.bigong.oguri.feature.mypage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metro.Provider

@Composable
fun MyPageRoute(
    myPageViewModelProvider: Provider<MyPageViewModel>,
    onOpenSuggestion: () -> Unit,
    onOpenTermsOfService: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onPeriodClick: (String, String) -> Unit,
) {
    val myPageViewModel: MyPageViewModel = remember {
        myPageViewModelProvider()
    }
    val myPageUiState = myPageViewModel.uiState.collectAsStateWithLifecycle().value

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
