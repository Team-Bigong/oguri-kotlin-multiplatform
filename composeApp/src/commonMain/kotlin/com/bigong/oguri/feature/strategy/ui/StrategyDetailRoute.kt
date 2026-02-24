package com.bigong.oguri.feature.strategy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.feature.common.ui.rememberRouteViewModel
import com.bigong.oguri.feature.strategy.ui.model.StrategyDetailUiState

@Composable
fun StrategyDetailRoute(
    annualLeaveStrategyRepository: AnnualLeaveStrategyRepository,
    strategyIdentifier: String,
    onCalendarClick: () -> Unit,
    onSavePdfProClick: () -> Unit,
) {
    val strategyDetailViewModel: StrategyDetailViewModel = rememberRouteViewModel(
        annualLeaveStrategyRepository,
        strategyIdentifier,
    ) {
        StrategyDetailViewModel(
            annualLeaveStrategyRepository = annualLeaveStrategyRepository,
            strategyIdentifier = strategyIdentifier,
        )
    }
    val strategyDetailUiState: StrategyDetailUiState by strategyDetailViewModel.strategyDetailUiStateFlow.collectAsState()

    StrategyDetailScreen(
        strategyIdentifier = strategyIdentifier,
        strategyDetailUiState = strategyDetailUiState,
        onRetryClick = strategyDetailViewModel::refresh,
        onCalendarClick = onCalendarClick,
        onSavePdfProClick = onSavePdfProClick,
    )
}
