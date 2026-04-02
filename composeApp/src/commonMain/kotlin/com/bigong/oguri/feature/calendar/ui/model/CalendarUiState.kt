package com.bigong.oguri.feature.calendar.ui.model

import kotlinx.datetime.LocalDate

data class CalendarUiState(
    val leaveDays: Int = 3,
    val selectedYear: Int = 2026,
    val selectedMonth: Int? = null,
    val isLeaveDaysRefreshing: Boolean = false,
    val expandedPeriodId: Long? = null,
    val selectedDateByPeriodId: Map<Long, LocalDate> = emptyMap(),
    val savedStateByPeriodKey: Map<String, Boolean> = emptyMap(),
)
