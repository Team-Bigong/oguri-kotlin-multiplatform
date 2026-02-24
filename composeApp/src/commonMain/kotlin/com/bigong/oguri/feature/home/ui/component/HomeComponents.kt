package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.HomeStrategyRecommendation
import com.bigong.oguri.data.model.MonthlyStrategyEfficiency
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_best_strategy_efficiency
import oguri.composeapp.generated.resources.home_best_strategy_period
import oguri.composeapp.generated.resources.home_button_detail
import oguri.composeapp.generated.resources.home_button_snackbar_test
import oguri.composeapp.generated.resources.home_ranked_month
import oguri.composeapp.generated.resources.home_role_office_worker
import oguri.composeapp.generated.resources.home_role_student
import oguri.composeapp.generated.resources.home_section_best_strategy
import oguri.composeapp.generated.resources.home_section_destinations
import oguri.composeapp.generated.resources.home_section_top3
import oguri.composeapp.generated.resources.home_work_schedule_five
import oguri.composeapp.generated.resources.home_work_schedule_six
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BestStrategySection(
    recommendation: HomeStrategyRecommendation,
    onStrategyDetailClick: (String) -> Unit,
    onShowSnackbarClick: () -> Unit,
) {
    PlaceholderSectionCard {
        PlaceholderSectionTitle(text = stringResource(Res.string.home_section_best_strategy))
        PlaceholderInfoCard(
            lines = listOf(
                stringResource(
                    Res.string.home_best_strategy_period,
                    recommendation.startDateText,
                    recommendation.endDateText,
                ),
                stringResource(
                    Res.string.home_best_strategy_efficiency,
                    recommendation.annualLeaveDaysUsed,
                    recommendation.totalVacationDaysSecured,
                ),
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingMedium)) {
            Box(modifier = Modifier.weight(1f)) {
                PlaceholderActionButton(
                    labelText = stringResource(Res.string.home_button_detail),
                    onClick = { onStrategyDetailClick(recommendation.strategyIdentifier) },
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                PlaceholderActionButton(
                    labelText = stringResource(Res.string.home_button_snackbar_test),
                    onClick = onShowSnackbarClick,
                    emphasized = false,
                )
            }
        }
    }
}

@Composable
internal fun TopThreeSection(
    topEfficiencyMonths: List<MonthlyStrategyEfficiency>,
) {
    PlaceholderSectionCard {
        PlaceholderSectionTitle(text = stringResource(Res.string.home_section_top3))
        topEfficiencyMonths.take(3).forEachIndexed { index: Int, monthlyStrategyEfficiency: MonthlyStrategyEfficiency ->
            androidx.compose.material3.Text(
                text = stringResource(
                    Res.string.home_ranked_month,
                    index + 1,
                    monthlyStrategyEfficiency.monthLabelText,
                    monthlyStrategyEfficiency.securedVacationDays,
                ),
                style = com.bigong.oguri.core.designsystem.OguriTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
internal fun DestinationSection(
    recommendedDestinations: List<String>,
) {
    PlaceholderSectionCard {
        PlaceholderSectionTitle(text = stringResource(Res.string.home_section_destinations))
        androidx.compose.material3.Text(
            text = recommendedDestinations.joinToString(separator = " · "),
            style = com.bigong.oguri.core.designsystem.OguriTheme.typography.bodyLarge,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun userRoleLabelText(userRoleType: UserRoleType): String {
    return when (userRoleType) {
        UserRoleType.OFFICE_WORKER -> stringResource(Res.string.home_role_office_worker)
        UserRoleType.STUDENT -> stringResource(Res.string.home_role_student)
    }
}

@Composable
internal fun workScheduleLabelText(workScheduleType: WorkScheduleType): String {
    return when (workScheduleType) {
        WorkScheduleType.FIVE_DAYS -> stringResource(Res.string.home_work_schedule_five)
        WorkScheduleType.SIX_DAYS -> stringResource(Res.string.home_work_schedule_six)
    }
}
