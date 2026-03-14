package com.bigong.oguri.feature.calendar.ui.model

import com.bigong.oguri.domain.model.CalendarRecommendation
import kotlinx.datetime.LocalDate

data class CalendarUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val leaveDays: Int = DEFAULT_LEAVE_DAYS,
    val selectedYear: Int = DEFAULT_SELECTED_YEAR,
    val selectedMonth: Int = DEFAULT_SELECTED_MONTH,
    val selectedDate: LocalDate? = null,
    val selectedPeriodId: Long? = null,
    val calendarRecommendation: CalendarRecommendation? = null,
) {
    companion object {
        const val DEFAULT_LEAVE_DAYS: Int = 3
        const val DEFAULT_SELECTED_YEAR: Int = 2026
        const val DEFAULT_SELECTED_MONTH: Int = 3
    }
}
