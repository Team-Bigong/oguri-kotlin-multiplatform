package com.bigong.oguri.feature.strategy.ui

import com.bigong.oguri.data.model.StrategyDetailData
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.feature.common.ui.RouteViewModel
import com.bigong.oguri.feature.strategy.ui.model.StrategyDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StrategyDetailViewModel(
    private val annualLeaveStrategyRepository: AnnualLeaveStrategyRepository,
    private val strategyIdentifier: String,
) : RouteViewModel() {
    private val mutableStrategyDetailUiStateFlow: MutableStateFlow<StrategyDetailUiState> = MutableStateFlow(StrategyDetailUiState())
    val strategyDetailUiStateFlow: StateFlow<StrategyDetailUiState> = mutableStrategyDetailUiStateFlow.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        routeViewModelScope.launch {
            mutableStrategyDetailUiStateFlow.update { previousUiState: StrategyDetailUiState ->
                previousUiState.copy(isLoading = true, isError = false)
            }
            runCatching {
                annualLeaveStrategyRepository.getStrategyDetail(strategyIdentifier = strategyIdentifier)
            }.onSuccess { data: StrategyDetailData ->
                mutableStrategyDetailUiStateFlow.update {
                    StrategyDetailUiState(isLoading = false, strategyDetailData = data)
                }
            }.onFailure {
                mutableStrategyDetailUiStateFlow.update {
                    StrategyDetailUiState(isLoading = false, isError = true)
                }
            }
        }
    }
}
