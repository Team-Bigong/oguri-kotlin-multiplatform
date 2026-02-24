package com.bigong.oguri.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.onboarding.ui.component.OnboardingFormSection
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.onboarding_calculate
import oguri.composeapp.generated.resources.onboarding_subtitle
import oguri.composeapp.generated.resources.onboarding_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingScreen(
    onboardingUiState: OnboardingUiState,
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

        OnboardingFormSection(
            onboardingUiState = onboardingUiState,
            onSelectUserRoleType = onSelectUserRoleType,
            onMinusAnnualLeave = onMinusAnnualLeave,
            onPlusAnnualLeave = onPlusAnnualLeave,
            onSelectWorkScheduleType = onSelectWorkScheduleType,
        )

        PlaceholderActionButton(
            labelText = stringResource(Res.string.onboarding_calculate),
            onClick = onCalculateStrategyClick,
        )
    }
}
