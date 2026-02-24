package com.bigong.oguri.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.calendar.ui.component.CalendarContent
import com.bigong.oguri.feature.calendar.ui.model.StrategyCalendarUiState
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderBannerAd
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_error_message
import oguri.composeapp.generated.resources.calendar_subtitle
import oguri.composeapp.generated.resources.calendar_title
import oguri.composeapp.generated.resources.common_loading
import oguri.composeapp.generated.resources.common_retry
import oguri.composeapp.generated.resources.common_retry_later
import oguri.composeapp.generated.resources.generic_banner_ad
import org.jetbrains.compose.resources.stringResource

@Composable
fun StrategyCalendarScreen(
    uiState: StrategyCalendarUiState,
    year: Int,
    onSelectMonth: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingLarge),
    ) {
        item {
            PlaceholderHeader(
                screenTitleText = stringResource(Res.string.calendar_title),
                screenSubtitleText = stringResource(Res.string.calendar_subtitle),
            )
        }
        item {
            when {
                uiState.isLoading -> PlaceholderInfoCard(lines = listOf(stringResource(Res.string.common_loading)))
                uiState.isError -> PlaceholderSectionCard {
                    PlaceholderInfoCard(
                        lines = listOf(
                            stringResource(Res.string.calendar_error_message),
                            stringResource(Res.string.common_retry_later),
                        ),
                    )
                    PlaceholderActionButton(
                        labelText = stringResource(Res.string.common_retry),
                        onClick = onRetry,
                        emphasized = false,
                    )
                }
                uiState.strategyCalendarData != null -> CalendarContent(
                    uiState = uiState,
                    year = year,
                    onSelectMonth = onSelectMonth,
                )
            }
        }
        item {
            PlaceholderBannerAd(labelText = stringResource(Res.string.generic_banner_ad))
        }
    }
}
