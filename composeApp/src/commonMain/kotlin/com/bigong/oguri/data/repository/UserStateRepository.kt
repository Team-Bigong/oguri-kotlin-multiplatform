package com.bigong.oguri.data.repository

import com.bigong.oguri.data.model.LoginProviderType
import com.bigong.oguri.data.model.UserState
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import kotlinx.coroutines.flow.StateFlow

interface UserStateRepository {
    val userStateFlow: StateFlow<UserState>

    fun updateUserRoleType(userRoleType: UserRoleType)

    fun updateRemainingAnnualLeaveDays(remainingAnnualLeaveDays: Int)

    fun updateWorkScheduleType(workScheduleType: WorkScheduleType)

    fun completeOnboarding()

    fun updateLoginProviderType(loginProviderType: LoginProviderType)

    fun toggleProSubscription()

    fun logout()
}
