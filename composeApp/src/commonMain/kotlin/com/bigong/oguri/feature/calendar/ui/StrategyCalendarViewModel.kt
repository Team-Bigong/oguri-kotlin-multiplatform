package com.bigong.oguri.feature.calendar.ui

import com.bigong.oguri.data.model.StrategyCalendarData
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.feature.calendar.ui.model.StrategyCalendarUiState
import com.bigong.oguri.feature.common.ui.RouteViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StrategyCalendarViewModel(
    private val annualLeaveStrategyRepository: AnnualLeaveStrategyRepository,
    private val year: Int,
) : RouteViewModel() {
    private val mutableStrategyCalendarUiStateFlow: MutableStateFlow<StrategyCalendarUiState> = MutableStateFlow(StrategyCalendarUiState())
    val strategyCalendarUiStateFlow: StateFlow<StrategyCalendarUiState> = mutableStrategyCalendarUiStateFlow.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        routeViewModelScope.launch {
            mutableStrategyCalendarUiStateFlow.update { previousUiState: StrategyCalendarUiState ->
                previousUiState.copy(isLoading = true, isError = false)
            }
            runCatching { annualLeaveStrategyRepository.getStrategyCalendar(year = year) }
                .onSuccess { data: StrategyCalendarData ->
                    mutableStrategyCalendarUiStateFlow.update { previousUiState: StrategyCalendarUiState ->
                        previousUiState.copy(isLoading = false, isError = false, strategyCalendarData = data)
                    }
                }
                .onFailure {
                    mutableStrategyCalendarUiStateFlow.update { previousUiState: StrategyCalendarUiState ->
                        previousUiState.copy(isLoading = false, isError = true, strategyCalendarData = null)
                    }
                }
        }
    }

    fun selectMonth(index: Int) {
        mutableStrategyCalendarUiStateFlow.update { previousUiState: StrategyCalendarUiState ->
            previousUiState.copy(selectedMonthIndex = index)
        }
    }
}
