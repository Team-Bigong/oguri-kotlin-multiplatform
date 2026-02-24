package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSelectableChip
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall
import com.bigong.oguri.feature.common.ui.PlaceholderStepper
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.onboarding_leave_days_value
import oguri.composeapp.generated.resources.onboarding_prompt_identity
import oguri.composeapp.generated.resources.onboarding_prompt_leave
import oguri.composeapp.generated.resources.onboarding_prompt_work_schedule
import oguri.composeapp.generated.resources.onboarding_role_office_worker
import oguri.composeapp.generated.resources.onboarding_role_student
import oguri.composeapp.generated.resources.onboarding_work_schedule_five
import oguri.composeapp.generated.resources.onboarding_work_schedule_six
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingFormSection(
    onboardingUiState: OnboardingUiState,
    onSelectUserRoleType: (UserRoleType) -> Unit,
    onMinusAnnualLeave: () -> Unit,
    onPlusAnnualLeave: () -> Unit,
    onSelectWorkScheduleType: (WorkScheduleType) -> Unit,
) {
    PlaceholderSectionCard {
        PlaceholderSectionTitle(text = stringResource(Res.string.onboarding_prompt_identity))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
        ) {
            PlaceholderSelectableChip(
                labelText = stringResource(Res.string.onboarding_role_office_worker),
                isSelected = onboardingUiState.userRoleType == UserRoleType.OFFICE_WORKER,
                onClick = { onSelectUserRoleType(UserRoleType.OFFICE_WORKER) },
                modifier = Modifier.weight(1f),
            )
            PlaceholderSelectableChip(
                labelText = stringResource(Res.string.onboarding_role_student),
                isSelected = onboardingUiState.userRoleType == UserRoleType.STUDENT,
                onClick = { onSelectUserRoleType(UserRoleType.STUDENT) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(PlaceholderSpacingSmall))
        PlaceholderSectionTitle(text = stringResource(Res.string.onboarding_prompt_leave))
        PlaceholderStepper(
            valueText = stringResource(Res.string.onboarding_leave_days_value, onboardingUiState.remainingAnnualLeaveDays),
            onMinusClick = onMinusAnnualLeave,
            onPlusClick = onPlusAnnualLeave,
        )

        Spacer(modifier = Modifier.height(PlaceholderSpacingSmall))
        PlaceholderSectionTitle(text = stringResource(Res.string.onboarding_prompt_work_schedule))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
        ) {
            PlaceholderSelectableChip(
                labelText = stringResource(Res.string.onboarding_work_schedule_five),
                isSelected = onboardingUiState.workScheduleType == WorkScheduleType.FIVE_DAYS,
                onClick = { onSelectWorkScheduleType(WorkScheduleType.FIVE_DAYS) },
                modifier = Modifier.weight(1f),
            )
            PlaceholderSelectableChip(
                labelText = stringResource(Res.string.onboarding_work_schedule_six),
                isSelected = onboardingUiState.workScheduleType == WorkScheduleType.SIX_DAYS,
                onClick = { onSelectWorkScheduleType(WorkScheduleType.SIX_DAYS) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
