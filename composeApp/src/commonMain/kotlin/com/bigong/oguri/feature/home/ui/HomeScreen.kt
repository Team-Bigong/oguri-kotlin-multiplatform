package com.bigong.oguri.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.data.model.HomeStrategyRecommendation
import com.bigong.oguri.data.model.MonthlyStrategyEfficiency
import com.bigong.oguri.data.model.UserState
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.data.repository.UserStateRepository
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderBannerAd
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import kotlinx.coroutines.flow.StateFlow
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_loading
import oguri.composeapp.generated.resources.common_retry_later
import oguri.composeapp.generated.resources.generic_banner_ad
import oguri.composeapp.generated.resources.home_best_strategy_efficiency
import oguri.composeapp.generated.resources.home_best_strategy_period
import oguri.composeapp.generated.resources.home_button_detail
import oguri.composeapp.generated.resources.home_button_snackbar_test
import oguri.composeapp.generated.resources.home_error_message
import oguri.composeapp.generated.resources.home_profile_summary
import oguri.composeapp.generated.resources.home_ranked_month
import oguri.composeapp.generated.resources.home_role_office_worker
import oguri.composeapp.generated.resources.home_role_student
import oguri.composeapp.generated.resources.home_section_best_strategy
import oguri.composeapp.generated.resources.home_section_destinations
import oguri.composeapp.generated.resources.home_section_top3
import oguri.composeapp.generated.resources.home_subtitle
import oguri.composeapp.generated.resources.home_title
import oguri.composeapp.generated.resources.home_work_schedule_five
import oguri.composeapp.generated.resources.home_work_schedule_six
import org.jetbrains.compose.resources.stringResource

private sealed interface HomeRouteUiState {
    data object Loading : HomeRouteUiState
    data class Success(val recommendation: HomeStrategyRecommendation) : HomeRouteUiState
    data object Error : HomeRouteUiState
}

@Composable
fun HomeRoute(
    annualLeaveStrategyRepository: AnnualLeaveStrategyRepository,
    userStateRepository: UserStateRepository,
    onStrategyDetailClick: (String) -> Unit,
    onShowSnackbarClick: () -> Unit,
) {
    val homeRouteUiState: HomeRouteUiState by produceState<HomeRouteUiState>(
        initialValue = HomeRouteUiState.Loading,
        key1 = annualLeaveStrategyRepository,
    ) {
        value = try {
            HomeRouteUiState.Success(annualLeaveStrategyRepository.getHomeStrategyRecommendation())
        } catch (_: Throwable) {
            HomeRouteUiState.Error
        }
    }
    val userStateFlow: StateFlow<UserState> = userStateRepository.userStateFlow
    val userState: UserState by userStateFlow.collectAsState()

    HomeScreen(
        userState = userState,
        homeRouteUiState = homeRouteUiState,
        onStrategyDetailClick = onStrategyDetailClick,
        onShowSnackbarClick = onShowSnackbarClick,
    )
}

@Composable
private fun HomeScreen(
    userState: UserState,
    homeRouteUiState: HomeRouteUiState,
    onStrategyDetailClick: (String) -> Unit,
    onShowSnackbarClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingLarge),
    ) {
        item {
            PlaceholderHeader(
                screenTitleText = "🦆 ${stringResource(Res.string.home_title)}",
                screenSubtitleText = stringResource(Res.string.home_subtitle),
            )
            Spacer(modifier = Modifier.height(PlaceholderSpacingMedium))
            PlaceholderInfoCard(
                lines = listOf(
                    stringResource(
                        Res.string.home_profile_summary,
                        userRoleLabelText(userState.userRoleType),
                        workScheduleLabelText(userState.workScheduleType),
                        userState.remainingAnnualLeaveDays,
                    ),
                ),
            )
        }
        item {
            when (homeRouteUiState) {
                HomeRouteUiState.Loading -> PlaceholderInfoCard(lines = listOf(stringResource(Res.string.common_loading)))
                HomeRouteUiState.Error -> PlaceholderInfoCard(
                    lines = listOf(
                        stringResource(Res.string.home_error_message),
                        stringResource(Res.string.common_retry_later),
                    ),
                )
                is HomeRouteUiState.Success -> BestStrategySection(
                    recommendation = homeRouteUiState.recommendation,
                    onStrategyDetailClick = onStrategyDetailClick,
                    onShowSnackbarClick = onShowSnackbarClick,
                )
            }
        }
        if (homeRouteUiState is HomeRouteUiState.Success) {
            item {
                TopThreeSection(topEfficiencyMonths = homeRouteUiState.recommendation.topEfficiencyMonths)
            }
            item {
                DestinationSection(recommendedDestinations = homeRouteUiState.recommendation.recommendedDestinations)
            }
        }
        item {
            PlaceholderBannerAd(labelText = stringResource(Res.string.generic_banner_ad))
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun BestStrategySection(
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
private fun TopThreeSection(
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
private fun DestinationSection(
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
private fun userRoleLabelText(userRoleType: UserRoleType): String {
    return when (userRoleType) {
        UserRoleType.OFFICE_WORKER -> stringResource(Res.string.home_role_office_worker)
        UserRoleType.STUDENT -> stringResource(Res.string.home_role_student)
    }
}

@Composable
private fun workScheduleLabelText(workScheduleType: WorkScheduleType): String {
    return when (workScheduleType) {
        WorkScheduleType.FIVE_DAYS -> stringResource(Res.string.home_work_schedule_five)
        WorkScheduleType.SIX_DAYS -> stringResource(Res.string.home_work_schedule_six)
    }
}
