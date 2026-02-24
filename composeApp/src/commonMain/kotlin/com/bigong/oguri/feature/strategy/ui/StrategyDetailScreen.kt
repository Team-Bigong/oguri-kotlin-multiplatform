package com.bigong.oguri.feature.strategy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderBannerAd
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.strategy.ui.component.StrategyDetailContentSections
import com.bigong.oguri.feature.strategy.ui.model.StrategyDetailUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_loading
import oguri.composeapp.generated.resources.common_retry
import oguri.composeapp.generated.resources.common_retry_later
import oguri.composeapp.generated.resources.generic_banner_ad
import oguri.composeapp.generated.resources.strategy_detail_error_message
import oguri.composeapp.generated.resources.strategy_detail_subtitle
import oguri.composeapp.generated.resources.strategy_detail_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun StrategyDetailScreen(
    strategyIdentifier: String,
    strategyDetailUiState: StrategyDetailUiState,
    onRetryClick: () -> Unit,
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
        item {
            when {
                strategyDetailUiState.isLoading -> PlaceholderInfoCard(lines = listOf(stringResource(Res.string.common_loading)))
                strategyDetailUiState.isError -> PlaceholderSectionCard {
                    PlaceholderInfoCard(
                        lines = listOf(
                            stringResource(Res.string.strategy_detail_error_message),
                            stringResource(Res.string.common_retry_later),
                        ),
                    )
                    PlaceholderActionButton(
                        labelText = stringResource(Res.string.common_retry),
                        onClick = onRetryClick,
                        emphasized = false,
                    )
                }
                strategyDetailUiState.strategyDetailData != null -> StrategyDetailContentSections(
                    strategyDetailData = strategyDetailUiState.strategyDetailData,
                    onCalendarClick = onCalendarClick,
                    onSavePdfProClick = onSavePdfProClick,
                )
            }
        }
        item {
            PlaceholderBannerAd(labelText = stringResource(Res.string.generic_banner_ad))
        }
    }
}
