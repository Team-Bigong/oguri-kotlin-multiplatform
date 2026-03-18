package com.bigong.oguri.feature.calendar.ui.model

import kotlinx.datetime.LocalDate

data class CalendarUiState(
    val isLoading: Boolean = true,
    val isLoadingNextPage: Boolean = false,
    val isError: Boolean = false,
    val leaveDays: Int = 3,
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 3,
    val hasMorePage: Boolean = true,
    val expandedPeriodId: Long? = null,
    val periodCards: List<CalendarPeriodCardUiModel> = emptyList(),
    val selectedDateByPeriodId: Map<Long, LocalDate> = emptyMap(),
)
