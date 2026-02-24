package com.bigong.oguri.feature.mypage.ui

import com.bigong.oguri.data.model.UserState
import com.bigong.oguri.data.model.WorkScheduleType
import com.bigong.oguri.data.repository.UserStateRepository
import com.bigong.oguri.feature.common.ui.RouteViewModel
import com.bigong.oguri.feature.mypage.ui.model.MyPageUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val ANNUAL_LEAVE_INCREMENT: Int = 1

class MyPageViewModel(
    private val userStateRepository: UserStateRepository,
) : RouteViewModel() {
    private val mutableMyPageUiStateFlow: MutableStateFlow<MyPageUiState> = MutableStateFlow(MyPageUiState())
    val myPageUiStateFlow: StateFlow<MyPageUiState> = mutableMyPageUiStateFlow.asStateFlow()

    init {
        routeViewModelScope.launch {
            userStateRepository.userStateFlow.collect { userState: UserState ->
                mutableMyPageUiStateFlow.value = MyPageUiState(userState = userState)
            }
        }
    }

    fun increaseAnnualLeaveDays(currentDays: Int) {
        userStateRepository.updateRemainingAnnualLeaveDays(currentDays + ANNUAL_LEAVE_INCREMENT)
    }

    fun decreaseAnnualLeaveDays(currentDays: Int) {
        userStateRepository.updateRemainingAnnualLeaveDays(currentDays - ANNUAL_LEAVE_INCREMENT)
    }

    fun toggleWorkScheduleType(currentWorkScheduleType: WorkScheduleType) {
        val nextWorkScheduleType: WorkScheduleType = if (currentWorkScheduleType == WorkScheduleType.FIVE_DAYS) {
            WorkScheduleType.SIX_DAYS
        } else {
            WorkScheduleType.FIVE_DAYS
        }
        userStateRepository.updateWorkScheduleType(nextWorkScheduleType)
    }

    fun toggleProSubscription() {
        userStateRepository.toggleProSubscription()
    }

    fun logout() {
        userStateRepository.logout()
    }
}
