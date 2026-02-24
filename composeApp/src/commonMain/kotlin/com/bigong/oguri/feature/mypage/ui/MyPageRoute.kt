package com.bigong.oguri.feature.mypage.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bigong.oguri.data.repository.UserStateRepository
import com.bigong.oguri.feature.common.ui.rememberRouteViewModel
import com.bigong.oguri.feature.mypage.ui.model.MyPageUiState

@Composable
fun MyPageRoute(
    userStateRepository: UserStateRepository,
    onSupportInquiryClick: () -> Unit,
    onShowDummySnackbar: (String) -> Unit,
    onShowProToggleSnackbar: (Boolean) -> Unit,
    onShowLogoutSnackbar: () -> Unit,
) {
    val myPageViewModel: MyPageViewModel = rememberRouteViewModel(userStateRepository) {
        MyPageViewModel(userStateRepository = userStateRepository)
    }
    val myPageUiState: MyPageUiState by myPageViewModel.myPageUiStateFlow.collectAsState()
    val userState = myPageUiState.userState

    MyPageScreen(
        uiState = myPageUiState,
        onIncreaseAnnualLeave = { myPageViewModel.increaseAnnualLeaveDays(userState.remainingAnnualLeaveDays) },
        onDecreaseAnnualLeave = { myPageViewModel.decreaseAnnualLeaveDays(userState.remainingAnnualLeaveDays) },
        onToggleWorkSchedule = { myPageViewModel.toggleWorkScheduleType(userState.workScheduleType) },
        onTogglePro = {
            myPageViewModel.toggleProSubscription()
            onShowProToggleSnackbar(!userState.isProSubscribed)
        },
        onSupportInquiryClick = onSupportInquiryClick,
        onDummyActionClick = onShowDummySnackbar,
        onLogoutClick = {
            myPageViewModel.logout()
            onShowLogoutSnackbar()
        },
    )
}
