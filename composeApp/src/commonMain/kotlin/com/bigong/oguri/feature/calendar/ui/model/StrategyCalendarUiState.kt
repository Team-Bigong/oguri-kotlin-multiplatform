package com.bigong.oguri.feature.calendar.ui.model

import com.bigong.oguri.data.model.StrategyCalendarData

data class StrategyCalendarUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val strategyCalendarData: StrategyCalendarData? = null,
    val selectedMonthIndex: Int = 0,
)
