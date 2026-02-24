package com.bigong.oguri.data.repository

import com.bigong.oguri.data.model.LoginProviderType
import com.bigong.oguri.data.model.UserMvpState
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import kotlinx.coroutines.flow.StateFlow

interface UserMvpStateRepository {
    val userMvpStateFlow: StateFlow<UserMvpState>

    fun updateUserRoleType(userRoleType: UserRoleType)

    fun updateRemainingAnnualLeaveDays(remainingAnnualLeaveDays: Int)

    fun updateWorkScheduleType(workScheduleType: WorkScheduleType)

    fun completeOnboarding()

    fun updateLoginProviderType(loginProviderType: LoginProviderType)

    fun toggleProSubscription()

    fun logout()
}
