package com.bigong.oguri.feature.strategy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.StrategyDetailData
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderBannerAd
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_loading
import oguri.composeapp.generated.resources.common_retry_later
import oguri.composeapp.generated.resources.generic_banner_ad
import oguri.composeapp.generated.resources.strategy_detail_button_calendar
import oguri.composeapp.generated.resources.strategy_detail_button_mini_calendar
import oguri.composeapp.generated.resources.strategy_detail_button_pdf_pro
import oguri.composeapp.generated.resources.strategy_detail_error_message
import oguri.composeapp.generated.resources.strategy_detail_section_destinations
import oguri.composeapp.generated.resources.strategy_detail_section_leave_usage
import oguri.composeapp.generated.resources.strategy_detail_section_total_secured
import oguri.composeapp.generated.resources.strategy_detail_subtitle
import oguri.composeapp.generated.resources.strategy_detail_title
import oguri.composeapp.generated.resources.strategy_detail_total_days_badge
import org.jetbrains.compose.resources.stringResource

private sealed interface StrategyDetailRouteUiState {
    data object Loading : StrategyDetailRouteUiState
    data class Success(val strategyDetailData: StrategyDetailData) : StrategyDetailRouteUiState
    data object Error : StrategyDetailRouteUiState
}

@Composable
fun StrategyDetailRoute(
    annualLeaveStrategyRepository: AnnualLeaveStrategyRepository,
    strategyIdentifier: String,
    onCalendarClick: () -> Unit,
    onSavePdfProClick: () -> Unit,
) {
    val strategyDetailRouteUiState: StrategyDetailRouteUiState by produceState<StrategyDetailRouteUiState>(
        initialValue = StrategyDetailRouteUiState.Loading,
        key1 = annualLeaveStrategyRepository,
        key2 = strategyIdentifier,
    ) {
        value = try {
            StrategyDetailRouteUiState.Success(
                annualLeaveStrategyRepository.getStrategyDetail(strategyIdentifier = strategyIdentifier),
            )
        } catch (_: Throwable) {
            StrategyDetailRouteUiState.Error
        }
    }

    StrategyDetailScreen(
        strategyIdentifier = strategyIdentifier,
        strategyDetailRouteUiState = strategyDetailRouteUiState,
        onCalendarClick = onCalendarClick,
        onSavePdfProClick = onSavePdfProClick,
    )
}

@Composable
private fun StrategyDetailScreen(
    strategyIdentifier: String,
    strategyDetailRouteUiState: StrategyDetailRouteUiState,
    onCalendarClick: () -> Unit,
    onSavePdfProClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingLarge),
    ) {
        item {
            PlaceholderHeader(
                screenTitleText = stringResource(Res.string.strategy_detail_title),
                screenSubtitleText = stringResource(Res.string.strategy_detail_subtitle, strategyIdentifier),
            )
        }
        when (strategyDetailRouteUiState) {
            StrategyDetailRouteUiState.Loading -> item {
                PlaceholderInfoCard(lines = listOf(stringResource(Res.string.common_loading)))
            }
            StrategyDetailRouteUiState.Error -> item {
                PlaceholderInfoCard(
                    lines = listOf(
                        stringResource(Res.string.strategy_detail_error_message),
                        stringResource(Res.string.common_retry_later),
                    ),
                )
            }
            is StrategyDetailRouteUiState.Success -> {
                item {
                    StrategyDetailContentSections(
                        strategyDetailData = strategyDetailRouteUiState.strategyDetailData,
                        onCalendarClick = onCalendarClick,
                        onSavePdfProClick = onSavePdfProClick,
                    )
                }
            }
        }
        item {
            PlaceholderBannerAd(labelText = stringResource(Res.string.generic_banner_ad))
        }
    }
}

@Composable
private fun StrategyDetailContentSections(
    strategyDetailData: StrategyDetailData,
    onCalendarClick: () -> Unit,
    onSavePdfProClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingMedium)) {
        PlaceholderSectionCard {
            PlaceholderSectionTitle(text = strategyDetailData.titleText)
            PlaceholderSectionTitle(text = stringResource(Res.string.strategy_detail_section_leave_usage))
            strategyDetailData.annualLeaveUsageDateTexts.forEach { annualLeaveUsageDateText: String ->
                androidx.compose.material3.Text(
                    text = annualLeaveUsageDateText,
                    style = com.bigong.oguri.core.designsystem.OguriTheme.typography.bodyMedium,
                )
            }
            PlaceholderSectionTitle(text = stringResource(Res.string.strategy_detail_section_total_secured))
            androidx.compose.material3.Text(
                text = stringResource(
                    Res.string.strategy_detail_total_days_badge,
                    strategyDetailData.totalVacationDaysSecured,
                ),
                style = com.bigong.oguri.core.designsystem.OguriTheme.typography.heroTitle,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingMedium)) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                    PlaceholderActionButton(
                        labelText = stringResource(Res.string.strategy_detail_button_mini_calendar),
                        onClick = onCalendarClick,
                        emphasized = false,
                    )
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                    PlaceholderActionButton(
                        labelText = stringResource(Res.string.strategy_detail_button_calendar),
                        onClick = onCalendarClick,
                    )
                }
            }
        }

        PlaceholderSectionCard {
            PlaceholderSectionTitle(text = stringResource(Res.string.strategy_detail_section_destinations))
            androidx.compose.material3.Text(
                text = strategyDetailData.recommendedDestinations.joinToString(separator = " · "),
                style = com.bigong.oguri.core.designsystem.OguriTheme.typography.bodyLarge,
            )
        }

        PlaceholderActionButton(
            labelText = stringResource(Res.string.strategy_detail_button_pdf_pro),
            onClick = onSavePdfProClick,
            emphasized = false,
        )
    }
}
