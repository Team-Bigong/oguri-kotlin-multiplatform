package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import com.bigong.oguri.data.model.MonthlyStrategyEfficiency
import com.bigong.oguri.feature.calendar.ui.model.StrategyCalendarUiState
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSelectableChip
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_month_highlight
import oguri.composeapp.generated.resources.calendar_month_selector_title
import oguri.composeapp.generated.resources.calendar_selected_month_summary
import oguri.composeapp.generated.resources.calendar_year_title
import oguri.composeapp.generated.resources.home_section_top3
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CalendarContent(
    uiState: StrategyCalendarUiState,
    year: Int,
    onSelectMonth: (Int) -> Unit,
) {
    val monthlyHighlights: List<MonthlyStrategyEfficiency> = uiState.strategyCalendarData?.monthlyHighlights.orEmpty()
    val safeSelectedMonthIndex: Int = uiState.selectedMonthIndex.coerceIn(0, (monthlyHighlights.size - 1).coerceAtLeast(0))

    Column(verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingLarge)) {
        PlaceholderSectionCard {
            PlaceholderSectionTitle(text = stringResource(Res.string.calendar_year_title, year))
            PlaceholderSectionTitle(text = stringResource(Res.string.calendar_month_selector_title))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall)) {
                itemsIndexed(monthlyHighlights) { index: Int, monthlyStrategyEfficiency: MonthlyStrategyEfficiency ->
                    PlaceholderSelectableChip(
                        labelText = monthlyStrategyEfficiency.monthLabelText,
                        isSelected = safeSelectedMonthIndex == index,
                        onClick = { onSelectMonth(index) },
                    )
                }
            }
            monthlyHighlights.getOrNull(safeSelectedMonthIndex)?.let { monthlyStrategyEfficiency: MonthlyStrategyEfficiency ->
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
