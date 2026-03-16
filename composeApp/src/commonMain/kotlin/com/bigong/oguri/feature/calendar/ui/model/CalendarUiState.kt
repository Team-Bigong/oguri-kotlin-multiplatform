package com.bigong.oguri.feature.calendar.ui.model

import com.bigong.oguri.domain.model.CalendarRecommendation
import kotlinx.datetime.LocalDate

data class CalendarUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val leaveDays: Int = 3,
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 3,
    val selectedDate: LocalDate? = null,
    val selectedPeriodId: Long? = null,
    val calendarRecommendation: CalendarRecommendation? = null,
)
