package com.bigong.oguri.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.MonthlyStrategyEfficiency
import com.bigong.oguri.data.model.StrategyCalendarData
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.feature.common.ui.PlaceholderBannerAd
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSelectableChip
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_error_message
import oguri.composeapp.generated.resources.calendar_month_highlight
import oguri.composeapp.generated.resources.calendar_month_selector_title
import oguri.composeapp.generated.resources.calendar_selected_month_summary
import oguri.composeapp.generated.resources.calendar_subtitle
import oguri.composeapp.generated.resources.calendar_title
import oguri.composeapp.generated.resources.calendar_year_title
import oguri.composeapp.generated.resources.common_loading
import oguri.composeapp.generated.resources.common_retry_later
import oguri.composeapp.generated.resources.generic_banner_ad
import oguri.composeapp.generated.resources.home_section_top3
import org.jetbrains.compose.resources.stringResource

private sealed interface StrategyCalendarRouteUiState {
    data object Loading : StrategyCalendarRouteUiState
    data class Success(val strategyCalendarData: StrategyCalendarData) : StrategyCalendarRouteUiState
    data object Error : StrategyCalendarRouteUiState
}

@Composable
fun StrategyCalendarRoute(
    annualLeaveStrategyRepository: AnnualLeaveStrategyRepository,
    year: Int,
) {
    val strategyCalendarRouteUiState: StrategyCalendarRouteUiState by produceState<StrategyCalendarRouteUiState>(
        initialValue = StrategyCalendarRouteUiState.Loading,
        key1 = annualLeaveStrategyRepository,
        key2 = year,
    ) {
        value = try {
            StrategyCalendarRouteUiState.Success(annualLeaveStrategyRepository.getStrategyCalendar(year = year))
        } catch (_: Throwable) {
            StrategyCalendarRouteUiState.Error
        }
    }

    StrategyCalendarScreen(strategyCalendarRouteUiState = strategyCalendarRouteUiState, year = year)
}

@Composable
private fun StrategyCalendarScreen(
    strategyCalendarRouteUiState: StrategyCalendarRouteUiState,
    year: Int,
) {
    var selectedMonthIndex: Int by remember { mutableStateOf(0) }

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
        when (strategyCalendarRouteUiState) {
            StrategyCalendarRouteUiState.Loading -> item {
                PlaceholderInfoCard(lines = listOf(stringResource(Res.string.common_loading)))
            }
            StrategyCalendarRouteUiState.Error -> item {
                PlaceholderInfoCard(lines = listOf(stringResource(Res.string.calendar_error_message), stringResource(Res.string.common_retry_later)))
            }
            is StrategyCalendarRouteUiState.Success -> {
                val monthlyHighlights: List<MonthlyStrategyEfficiency> = strategyCalendarRouteUiState.strategyCalendarData.monthlyHighlights
                val safeSelectedMonthIndex: Int = selectedMonthIndex.coerceIn(0, (monthlyHighlights.size - 1).coerceAtLeast(0))
                if (safeSelectedMonthIndex != selectedMonthIndex) {
                    selectedMonthIndex = safeSelectedMonthIndex
                }
                item {
                    PlaceholderSectionCard {
                        PlaceholderSectionTitle(text = stringResource(Res.string.calendar_year_title, year))
                        PlaceholderSectionTitle(text = stringResource(Res.string.calendar_month_selector_title))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall)) {
                            itemsIndexed(monthlyHighlights) { index: Int, monthlyStrategyEfficiency: MonthlyStrategyEfficiency ->
                                PlaceholderSelectableChip(
                                    labelText = monthlyStrategyEfficiency.monthLabelText,
                                    isSelected = selectedMonthIndex == index,
                                    onClick = { selectedMonthIndex = index },
                                )
                            }
                        }
                        monthlyHighlights.getOrNull(selectedMonthIndex)?.let { monthlyStrategyEfficiency: MonthlyStrategyEfficiency ->
                            PlaceholderInfoCard(
                                lines = listOf(
                                    stringResource(Res.string.calendar_selected_month_summary, monthlyStrategyEfficiency.monthLabelText),
                                    stringResource(
                                        Res.string.calendar_month_highlight,
                                        monthlyStrategyEfficiency.monthLabelText,
                                        monthlyStrategyEfficiency.securedVacationDays,
                                    ),
                                ),
                            )
                        }
                    }
                }
                item {
                    PlaceholderSectionCard {
                        PlaceholderSectionTitle(text = stringResource(Res.string.home_section_top3))
                        monthlyHighlights.forEach { monthlyStrategyEfficiency: MonthlyStrategyEfficiency ->
                            androidx.compose.material3.Text(
                                text = stringResource(
                                    Res.string.calendar_month_highlight,
                                    monthlyStrategyEfficiency.monthLabelText,
                                    monthlyStrategyEfficiency.securedVacationDays,
                                ),
                            )
                        }
                    }
                }
            }
        }
        item { PlaceholderBannerAd(labelText = stringResource(Res.string.generic_banner_ad)) }
    }
}
