package com.bigong.oguri.data.repository

import com.bigong.oguri.data.model.LoginProviderType
import com.bigong.oguri.data.model.UserMvpState
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object InMemoryUserMvpStateRepository : UserMvpStateRepository {
    private const val MinimumAnnualLeaveDays: Int = 0
    private const val MaximumAnnualLeaveDays: Int = 30

    private val mutableUserMvpStateFlow: MutableStateFlow<UserMvpState> =
        MutableStateFlow(value = UserMvpState())

    override val userMvpStateFlow: StateFlow<UserMvpState> = mutableUserMvpStateFlow

    override fun updateUserRoleType(userRoleType: UserRoleType) {
        mutableUserMvpStateFlow.update { previousState: UserMvpState ->
            previousState.copy(userRoleType = userRoleType)
        }
    }

    override fun updateRemainingAnnualLeaveDays(remainingAnnualLeaveDays: Int) {
        val clampedAnnualLeaveDays: Int = remainingAnnualLeaveDays.coerceIn(
            minimumValue = MinimumAnnualLeaveDays,
            maximumValue = MaximumAnnualLeaveDays,
        )
        mutableUserMvpStateFlow.update { previousState: UserMvpState ->
            previousState.copy(remainingAnnualLeaveDays = clampedAnnualLeaveDays)
        }
    }

    override fun updateWorkScheduleType(workScheduleType: WorkScheduleType) {
        mutableUserMvpStateFlow.update { previousState: UserMvpState ->
            previousState.copy(workScheduleType = workScheduleType)
        }
    }

    override fun completeOnboarding() {
        mutableUserMvpStateFlow.update { previousState: UserMvpState ->
            previousState.copy(isOnboardingCompleted = true)
        }
    }

    override fun updateLoginProviderType(loginProviderType: LoginProviderType) {
        mutableUserMvpStateFlow.update { previousState: UserMvpState ->
            previousState.copy(loginProviderType = loginProviderType)
        }
    }

    override fun toggleProSubscription() {
        mutableUserMvpStateFlow.update { previousState: UserMvpState ->
            previousState.copy(isProSubscribed = !previousState.isProSubscribed)
        }
    }

    override fun logout() {
        mutableUserMvpStateFlow.update { previousState: UserMvpState ->
            previousState.copy(
                loginProviderType = LoginProviderType.NONE,
                isOnboardingCompleted = false,
                isProSubscribed = false,
            )
        }
    }
}
