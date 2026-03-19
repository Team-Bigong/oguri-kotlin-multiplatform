package com.bigong.oguri.feature.perioddetail.ui.model

import com.bigong.oguri.domain.model.CalendarPeriodDetail

data class PeriodDetailUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val periodDetail: CalendarPeriodDetail? = null,
    val isSaved: Boolean = false,
    val dDay: Int = 0,
)
