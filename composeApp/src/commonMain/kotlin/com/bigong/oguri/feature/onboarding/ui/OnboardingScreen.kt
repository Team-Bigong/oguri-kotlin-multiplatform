package com.bigong.oguri.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import com.bigong.oguri.data.repository.UserStateRepository
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSelectableChip
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall
import com.bigong.oguri.feature.common.ui.PlaceholderStepper
import kotlinx.coroutines.flow.StateFlow
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.onboarding_calculate
import oguri.composeapp.generated.resources.onboarding_leave_days_value
import oguri.composeapp.generated.resources.onboarding_prompt_identity
import oguri.composeapp.generated.resources.onboarding_prompt_leave
import oguri.composeapp.generated.resources.onboarding_prompt_work_schedule
import oguri.composeapp.generated.resources.onboarding_role_office_worker
import oguri.composeapp.generated.resources.onboarding_role_student
import oguri.composeapp.generated.resources.onboarding_subtitle
import oguri.composeapp.generated.resources.onboarding_title
import oguri.composeapp.generated.resources.onboarding_work_schedule_five
import oguri.composeapp.generated.resources.onboarding_work_schedule_six
import org.jetbrains.compose.resources.stringResource

private const val AnnualLeaveStep: Int = 1

@Composable
fun OnboardingRoute(
    userStateRepository: UserStateRepository,
    onCalculateStrategyClick: () -> Unit,
) {
    val userStateFlow: StateFlow<com.bigong.oguri.data.model.UserState> = userStateRepository.userStateFlow
    val userState by userStateFlow.collectAsState()

    OnboardingScreen(
        userRoleType = userState.userRoleType,
        remainingAnnualLeaveDays = userState.remainingAnnualLeaveDays,
        workScheduleType = userState.workScheduleType,
        onSelectUserRoleType = userStateRepository::updateUserRoleType,
        onMinusAnnualLeave = {
            userStateRepository.updateRemainingAnnualLeaveDays(
                userState.remainingAnnualLeaveDays - AnnualLeaveStep,
            )
        },
        onPlusAnnualLeave = {
            userStateRepository.updateRemainingAnnualLeaveDays(
                userState.remainingAnnualLeaveDays + AnnualLeaveStep,
            )
        },
        onSelectWorkScheduleType = userStateRepository::updateWorkScheduleType,
        onCalculateStrategyClick = {
            userStateRepository.completeOnboarding()
            onCalculateStrategyClick()
        },
    )
}

@Composable
fun OnboardingScreen(
    userRoleType: UserRoleType,
    remainingAnnualLeaveDays: Int,
    workScheduleType: WorkScheduleType,
    onSelectUserRoleType: (UserRoleType) -> Unit,
    onMinusAnnualLeave: () -> Unit,
    onPlusAnnualLeave: () -> Unit,
    onSelectWorkScheduleType: (WorkScheduleType) -> Unit,
    onCalculateStrategyClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingLarge),
    ) {
        PlaceholderHeader(
            screenTitleText = stringResource(Res.string.onboarding_title),
            screenSubtitleText = stringResource(Res.string.onboarding_subtitle),
        )

        PlaceholderSectionCard {
            PlaceholderSectionTitle(text = stringResource(Res.string.onboarding_prompt_identity))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
            ) {
                PlaceholderSelectableChip(
                    labelText = stringResource(Res.string.onboarding_role_office_worker),
                    isSelected = userRoleType == UserRoleType.OFFICE_WORKER,
                    onClick = { onSelectUserRoleType(UserRoleType.OFFICE_WORKER) },
                    modifier = Modifier.weight(1f),
                )
                PlaceholderSelectableChip(
                    labelText = stringResource(Res.string.onboarding_role_student),
                    isSelected = userRoleType == UserRoleType.STUDENT,
                    onClick = { onSelectUserRoleType(UserRoleType.STUDENT) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(PlaceholderSpacingSmall))
            PlaceholderSectionTitle(text = stringResource(Res.string.onboarding_prompt_leave))
            PlaceholderStepper(
                valueText = stringResource(Res.string.onboarding_leave_days_value, remainingAnnualLeaveDays),
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
                    isSelected = workScheduleType == WorkScheduleType.FIVE_DAYS,
                    onClick = { onSelectWorkScheduleType(WorkScheduleType.FIVE_DAYS) },
                    modifier = Modifier.weight(1f),
                )
                PlaceholderSelectableChip(
                    labelText = stringResource(Res.string.onboarding_work_schedule_six),
                    isSelected = workScheduleType == WorkScheduleType.SIX_DAYS,
                    onClick = { onSelectWorkScheduleType(WorkScheduleType.SIX_DAYS) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        PlaceholderActionButton(
            labelText = stringResource(Res.string.onboarding_calculate),
            onClick = onCalculateStrategyClick,
        )
    }
}
