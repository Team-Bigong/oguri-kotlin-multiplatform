package com.bigong.oguri.feature.mypage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.ui.component.ConfirmAlertDialog
import com.bigong.oguri.feature.home.ui.component.HomeErrorContent
import com.bigong.oguri.feature.home.ui.component.HomeLoadingContent
import com.bigong.oguri.feature.mypage.ui.component.MyPageLeaveDaysBottomSheet
import com.bigong.oguri.feature.mypage.ui.component.MyPageMenuItem
import com.bigong.oguri.feature.mypage.ui.component.MyPageMenuSection
import com.bigong.oguri.feature.mypage.ui.component.MyPageProfileSection
import com.bigong.oguri.feature.mypage.ui.component.MyPageSavedPlaceSection
import com.bigong.oguri.feature.mypage.ui.component.MyPageSelectedPeriodSection
import com.bigong.oguri.feature.mypage.ui.model.MyPageUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_alert
import oguri.composeapp.generated.resources.mypage_delete_place_dialog_message
import oguri.composeapp.generated.resources.mypage_delete_place_dialog_title
import oguri.composeapp.generated.resources.mypage_delete_schedule_dialog_message
import oguri.composeapp.generated.resources.mypage_delete_schedule_dialog_title
import oguri.composeapp.generated.resources.mypage_dialog_cancel
import oguri.composeapp.generated.resources.mypage_dialog_confirm
import oguri.composeapp.generated.resources.mypage_error_retry
import oguri.composeapp.generated.resources.mypage_loading
import oguri.composeapp.generated.resources.mypage_logout_dialog_message
import oguri.composeapp.generated.resources.mypage_logout_dialog_title
import oguri.composeapp.generated.resources.mypage_menu_logout
import oguri.composeapp.generated.resources.mypage_menu_privacy_policy
import oguri.composeapp.generated.resources.mypage_menu_suggest
import oguri.composeapp.generated.resources.mypage_menu_terms_of_service
import oguri.composeapp.generated.resources.mypage_menu_withdraw
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageScreen(
    myPageUiState: MyPageUiState,
    onRetryClick: () -> Unit,
    onEditLeaveDaysClick: () -> Unit,
    onDismissLeaveDaysBottomSheet: () -> Unit,
    onSubmitLeaveDays: (Int) -> Unit,
    onDeleteScheduleClick: (Long) -> Unit,
    onDismissDeleteScheduleDialog: () -> Unit,
    onConfirmDeleteSchedule: () -> Unit,
    onDeleteSavedPlaceClick: (Long) -> Unit,
    onDismissDeleteSavedPlaceDialog: () -> Unit,
    onConfirmDeleteSavedPlace: () -> Unit,
    onSuggestClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDismissLogoutDialog: () -> Unit,
    onConfirmLogout: () -> Unit,
) {
    if (myPageUiState.isLoading) {
        HomeLoadingContent(message = stringResource(Res.string.mypage_loading))
        return
    }

    if (myPageUiState.isError || myPageUiState.myPageInfo == null) {
        val retryText = stringResource(Res.string.mypage_error_retry)
        HomeErrorContent(
            message = retryText,
            retryText = retryText,
            onRetryClick = onRetryClick,
        )
        return
    }

    val myPageInfo = myPageUiState.myPageInfo

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Neutral5).statusBarsPadding(),
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            MyPageProfileSection(
                nickname = myPageInfo.nickname,
                remainingLeaveDays = myPageInfo.remainingLeaveDays,
                preferredLeaveDays = myPageInfo.preferredLeaveDays,
                onEditLeaveDaysClick = onEditLeaveDaysClick,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Neutral20)
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
            MyPageSelectedPeriodSection(
                selectedPeriods = myPageInfo.selectedPeriods,
                onDeleteClick = onDeleteScheduleClick,
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            MyPageSavedPlaceSection(
                savedPlaces = myPageInfo.savedPlaces,
                onDeleteClick = onDeleteSavedPlaceClick,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        item {
            MyPageMenuSection(
                menuItems =
                    listOf(
                        MyPageMenuItem(label = stringResource(Res.string.mypage_menu_suggest), onClick = onSuggestClick),
                        MyPageMenuItem(label = stringResource(Res.string.mypage_menu_terms_of_service), onClick = onTermsOfServiceClick),
                        MyPageMenuItem(label = stringResource(Res.string.mypage_menu_privacy_policy), onClick = onPrivacyPolicyClick),
                        MyPageMenuItem(label = stringResource(Res.string.mypage_menu_withdraw), onClick = onWithdrawClick),
                        MyPageMenuItem(label = stringResource(Res.string.mypage_menu_logout), onClick = onLogoutClick),
                    ),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (myPageUiState.isEditLeaveDaysBottomSheetVisible) {
        MyPageLeaveDaysBottomSheet(
            currentLeaveDays = myPageInfo.preferredLeaveDays,
            onDismissRequest = onDismissLeaveDaysBottomSheet,
            onSubmit = onSubmitLeaveDays,
        )
    }

    if (myPageUiState.pendingDeleteScheduleId != null) {
        ConfirmAlertDialog(
            iconResource = Res.drawable.ic_alert,
            titleText = stringResource(Res.string.mypage_delete_schedule_dialog_title),
            messageText = stringResource(Res.string.mypage_delete_schedule_dialog_message),
            confirmButtonText = stringResource(Res.string.mypage_dialog_confirm),
            cancelButtonText = stringResource(Res.string.mypage_dialog_cancel),
            onDismissRequest = onDismissDeleteScheduleDialog,
            onConfirmClick = onConfirmDeleteSchedule,
            onCancelClick = onDismissDeleteScheduleDialog,
        )
    }

    if (myPageUiState.pendingDeletePlaceId != null) {
        ConfirmAlertDialog(
            iconResource = Res.drawable.ic_alert,
            titleText = stringResource(Res.string.mypage_delete_place_dialog_title),
            messageText = stringResource(Res.string.mypage_delete_place_dialog_message),
            confirmButtonText = stringResource(Res.string.mypage_dialog_confirm),
            cancelButtonText = stringResource(Res.string.mypage_dialog_cancel),
            onDismissRequest = onDismissDeleteSavedPlaceDialog,
            onConfirmClick = onConfirmDeleteSavedPlace,
            onCancelClick = onDismissDeleteSavedPlaceDialog,
        )
    }

    if (myPageUiState.isLogoutDialogVisible) {
        ConfirmAlertDialog(
            iconResource = Res.drawable.ic_alert,
            titleText = stringResource(Res.string.mypage_logout_dialog_title),
            messageText = stringResource(Res.string.mypage_logout_dialog_message),
            confirmButtonText = stringResource(Res.string.mypage_dialog_confirm),
            cancelButtonText = stringResource(Res.string.mypage_dialog_cancel),
            onDismissRequest = onDismissLogoutDialog,
            onConfirmClick = onConfirmLogout,
            onCancelClick = onDismissLogoutDialog,
        )
    }
}
