package com.bigong.oguri.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.data.model.HomeStrategyRecommendation
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderBannerAd
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import com.bigong.oguri.feature.home.ui.component.BestStrategySection
import com.bigong.oguri.feature.home.ui.component.DestinationSection
import com.bigong.oguri.feature.home.ui.component.TopThreeSection
import com.bigong.oguri.feature.home.ui.component.userRoleLabelText
import com.bigong.oguri.feature.home.ui.component.workScheduleLabelText
import com.bigong.oguri.feature.home.ui.model.HomeUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_loading
import oguri.composeapp.generated.resources.common_retry
import oguri.composeapp.generated.resources.common_retry_later
import oguri.composeapp.generated.resources.generic_banner_ad
import oguri.composeapp.generated.resources.home_error_message
import oguri.composeapp.generated.resources.home_profile_summary
import oguri.composeapp.generated.resources.home_subtitle
import oguri.composeapp.generated.resources.home_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    onRetryClick: () -> Unit,
    onStrategyDetailClick: (String) -> Unit,
    onShowSnackbarClick: () -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
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
                lines =
                    listOf(
                        stringResource(
                            Res.string.home_profile_summary,
                            userRoleLabelText(homeUiState.userState.userRoleType),
                            workScheduleLabelText(homeUiState.userState.workScheduleType),
                            homeUiState.userState.remainingAnnualLeaveDays,
                        ),
                    ),
            )
        }
        item {
            when {
                homeUiState.isLoading -> {
                    PlaceholderInfoCard(lines = listOf(stringResource(Res.string.common_loading)))
                }

                homeUiState.isError -> {
                    PlaceholderSectionCard {
                        PlaceholderInfoCard(
                            lines =
                                listOf(
                                    stringResource(Res.string.home_error_message),
                                    stringResource(Res.string.common_retry_later),
                                ),
                        )
                        PlaceholderActionButton(
                            labelText = stringResource(Res.string.common_retry),
                            onClick = onRetryClick,
                            emphasized = false,
                        )
                    }
                }

                homeUiState.recommendation != null -> {
                    BestStrategySection(
                        recommendation = homeUiState.recommendation,
                        onStrategyDetailClick = onStrategyDetailClick,
                        onShowSnackbarClick = onShowSnackbarClick,
                    )
                }
            }
        }
        homeUiState.recommendation?.let { recommendation: HomeStrategyRecommendation ->
            item {
                TopThreeSection(topEfficiencyMonths = recommendation.topEfficiencyMonths)
            }
            item {
                DestinationSection(recommendedDestinations = recommendation.recommendedDestinations)
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
