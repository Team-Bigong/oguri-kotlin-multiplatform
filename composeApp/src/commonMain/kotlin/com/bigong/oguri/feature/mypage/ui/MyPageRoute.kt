package com.bigong.oguri.feature.mypage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import dev.zacsweers.metro.Provider

private const val PRIVACY_POLICY_URL: String = "https://wealthy-clematis-4a5.notion.site/31d5bca0ac2280269bbcd799cdc73997?source=copy_link"
private const val TERMS_OF_SERVICE_URL: String = "https://wealthy-clematis-4a5.notion.site/31d5bca0ac22806aa8c4f6b0374c3a55?source=copy_link"

@Composable
fun MyPageRoute(
    myPageViewModelProvider: Provider<MyPageViewModel>,
) {
    val myPageViewModel: MyPageViewModel = remember {
        myPageViewModelProvider()
    }
    val uriHandler = LocalUriHandler.current

    MyPageScreen(
        myPageUiState = myPageViewModel.myPageUiState,
        onRetryClick = myPageViewModel::loadMyPageInfo,
        onEditLeaveDaysClick = myPageViewModel::showEditLeaveDaysBottomSheet,
        onDismissLeaveDaysBottomSheet = myPageViewModel::hideEditLeaveDaysBottomSheet,
        onSubmitLeaveDays = myPageViewModel::updatePreferredLeaveDays,
        onDeleteScheduleClick = myPageViewModel::showDeleteScheduleDialog,
        onDismissDeleteScheduleDialog = myPageViewModel::dismissDeleteScheduleDialog,
        onConfirmDeleteSchedule = myPageViewModel::confirmDeleteSchedule,
        onDeleteSavedPlaceClick = myPageViewModel::showDeleteSavedPlaceDialog,
        onDismissDeleteSavedPlaceDialog = myPageViewModel::dismissDeleteSavedPlaceDialog,
        onConfirmDeleteSavedPlace = myPageViewModel::confirmDeleteSavedPlace,
        onSuggestClick = {},
        onTermsOfServiceClick = {
            uriHandler.openUri(TERMS_OF_SERVICE_URL)
        },
        onPrivacyPolicyClick = {
            uriHandler.openUri(PRIVACY_POLICY_URL)
        },
        onWithdrawClick = {},
        onLogoutClick = myPageViewModel::showLogoutDialog,
        onDismissLogoutDialog = myPageViewModel::hideLogoutDialog,
        onConfirmLogout = myPageViewModel::hideLogoutDialog,
    )
}
