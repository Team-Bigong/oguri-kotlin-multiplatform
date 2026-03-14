package com.bigong.oguri.feature.mypage.ui.model

import com.bigong.oguri.domain.model.MyPageInfo

data class MyPageUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val myPageInfo: MyPageInfo? = null,
    val isEditLeaveDaysBottomSheetVisible: Boolean = false,
    val pendingDeleteScheduleId: Long? = null,
    val pendingDeletePlaceId: Long? = null,
    val isLogoutDialogVisible: Boolean = false,
)
