package com.bigong.oguri.feature.mypage.ui.model

sealed interface MyPageSideEffect {
    data object LeaveDaysUpdated : MyPageSideEffect

    data object SelectedPeriodDeleted : MyPageSideEffect

    data object SavedPlaceDeleted : MyPageSideEffect

    data object LoggedOut : MyPageSideEffect

    data object WithdrawCompleted : MyPageSideEffect
}
