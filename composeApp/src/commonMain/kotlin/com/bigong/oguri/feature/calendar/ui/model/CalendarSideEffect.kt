package com.bigong.oguri.feature.calendar.ui.model

sealed interface CalendarSideEffect {
    data object LeaveDaysUpdated : CalendarSideEffect

    data class NavigateToPeriodDetail(
        val startDate: String,
        val endDate: String,
    ) : CalendarSideEffect
}
