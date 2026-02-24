package com.bigong.oguri.feature.onboarding.ui.model

import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType

data class OnboardingUiState(
    val userRoleType: UserRoleType = UserRoleType.OFFICE_WORKER,
    val remainingAnnualLeaveDays: Int = 15,
    val workScheduleType: WorkScheduleType = WorkScheduleType.FIVE_DAYS,
)
