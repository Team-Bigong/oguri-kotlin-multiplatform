package com.bigong.oguri.feature.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.feature.calendar.ui.model.StrategyCalendarUiState
import com.bigong.oguri.feature.common.ui.rememberRouteViewModel

@Composable
fun StrategyCalendarRoute(
    annualLeaveStrategyRepository: AnnualLeaveStrategyRepository,
    year: Int,
) {
    val strategyCalendarViewModel: StrategyCalendarViewModel = rememberRouteViewModel(annualLeaveStrategyRepository, year) {
        StrategyCalendarViewModel(
            annualLeaveStrategyRepository = annualLeaveStrategyRepository,
            year = year,
        )
    }
    val strategyCalendarUiState: StrategyCalendarUiState by strategyCalendarViewModel.strategyCalendarUiStateFlow.collectAsState()

    StrategyCalendarScreen(
        uiState = strategyCalendarUiState,
        year = year,
        onSelectMonth = strategyCalendarViewModel::selectMonth,
        onRetry = strategyCalendarViewModel::refresh,
    )
}
