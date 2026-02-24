package com.bigong.oguri.data.repository

import com.bigong.oguri.data.model.LoginProviderType
import com.bigong.oguri.data.model.UserState
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object InMemoryUserStateRepository : UserStateRepository {
    private const val MinimumAnnualLeaveDays: Int = 0
    private const val MaximumAnnualLeaveDays: Int = 30

    private val mutableUserStateFlow: MutableStateFlow<UserState> =
        MutableStateFlow(value = UserState())

    override val userStateFlow: StateFlow<UserState> = mutableUserStateFlow

    override fun updateUserRoleType(userRoleType: UserRoleType) {
        mutableUserStateFlow.update { previousState: UserState ->
            previousState.copy(userRoleType = userRoleType)
        }
    }

    override fun updateRemainingAnnualLeaveDays(remainingAnnualLeaveDays: Int) {
        val clampedAnnualLeaveDays: Int = remainingAnnualLeaveDays.coerceIn(
            minimumValue = MinimumAnnualLeaveDays,
            maximumValue = MaximumAnnualLeaveDays,
        )
        mutableUserStateFlow.update { previousState: UserState ->
            previousState.copy(remainingAnnualLeaveDays = clampedAnnualLeaveDays)
        }
    }

    override fun updateWorkScheduleType(workScheduleType: WorkScheduleType) {
        mutableUserStateFlow.update { previousState: UserState ->
            previousState.copy(workScheduleType = workScheduleType)
        }
    }

    override fun completeOnboarding() {
        mutableUserStateFlow.update { previousState: UserState ->
            previousState.copy(isOnboardingCompleted = true)
        }
    }

    override fun updateLoginProviderType(loginProviderType: LoginProviderType) {
        mutableUserStateFlow.update { previousState: UserState ->
            previousState.copy(loginProviderType = loginProviderType)
        }
    }

    override fun toggleProSubscription() {
        mutableUserStateFlow.update { previousState: UserState ->
            previousState.copy(isProSubscribed = !previousState.isProSubscribed)
        }
    }

    override fun logout() {
        mutableUserStateFlow.update { previousState: UserState ->
            previousState.copy(
                loginProviderType = LoginProviderType.NONE,
                isOnboardingCompleted = false,
                isProSubscribed = false,
            )
        }
    }
}
