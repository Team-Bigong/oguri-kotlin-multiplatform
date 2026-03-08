package com.bigong.oguri.feature.calendar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bigong.oguri.domain.model.CalendarPeriod
import com.bigong.oguri.domain.usecase.GetCalendarRecommendationUseCase
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

@Inject
class CalendarViewModel(
    private val getCalendarRecommendationUseCase: GetCalendarRecommendationUseCase,
) : ViewModel() {
    private val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var calendarUiState: CalendarUiState by mutableStateOf(CalendarUiState())
        private set

    init {
        fetchCalendarRecommendation(
            year = calendarUiState.selectedYear,
            month = calendarUiState.selectedMonth,
        )
    }

    fun updateLeaveDays(leaveDays: Int) {
        if (leaveDays <= 0) {
            return
        }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    getCalendarRecommendationUseCase.updateDayOffCount(dayOffCount = leaveDays)
                }
            }.onSuccess { updatedDayOffCount ->
                calendarUiState = calendarUiState.copy(leaveDays = updatedDayOffCount)
                fetchCalendarRecommendation(
                    year = calendarUiState.selectedYear,
                    month = calendarUiState.selectedMonth,
                )
            }
        }
    }

    fun updateYearMonth(
        year: Int,
        month: Int,
    ) {
        calendarUiState = calendarUiState.copy(
            selectedYear = year,
            selectedMonth = month,
        )
        fetchCalendarRecommendation(
            year = year,
            month = month,
        )
    }

    fun onDateClick(
        date: LocalDate,
        onOpenPeriodDetail: (Long) -> Unit,
    ) {
        val matchedPeriod =
            calendarUiState.calendarRecommendation?.periods?.firstOrNull { period: CalendarPeriod ->
                date in period.startDate..period.endDate
            }
        if (matchedPeriod == null) {
            return
        }

        val isSameDateTapped = calendarUiState.selectedDate == date
        val isSamePeriodTapped = matchedPeriod.id == calendarUiState.selectedPeriodId
        if (isSameDateTapped && isSamePeriodTapped) {
            onOpenPeriodDetail(matchedPeriod.id)
            return
        }

        calendarUiState = calendarUiState.copy(
            selectedDate = date,
            selectedPeriodId = matchedPeriod.id,
        )
    }

    fun onPeriodClick(
        periodId: Long,
        onOpenPeriodDetail: (Long) -> Unit,
    ) {
        if (calendarUiState.selectedPeriodId == periodId) {
            onOpenPeriodDetail(periodId)
            return
        }

        val selectedPeriod =
            calendarUiState.calendarRecommendation?.periods?.firstOrNull { period: CalendarPeriod ->
                period.id == periodId
            } ?: return

        calendarUiState = calendarUiState.copy(
            selectedPeriodId = periodId,
            selectedDate = selectedPeriod.startDate,
        )
    }

    fun retry() {
        fetchCalendarRecommendation(
            year = calendarUiState.selectedYear,
            month = calendarUiState.selectedMonth,
        )
    }

    private fun fetchCalendarRecommendation(
        year: Int,
        month: Int,
    ) {
        viewModelScope.launch {
            calendarUiState = calendarUiState.copy(isLoading = true, isError = false)

            runCatching {
                withContext(Dispatchers.Default) {
                    getCalendarRecommendationUseCase(
                        year = year,
                        month = month,
                    )
                }
            }.onSuccess { recommendation ->
                val defaultSelectedPeriod = recommendation.periods.firstOrNull()
                calendarUiState = calendarUiState.copy(
                    isLoading = false,
                    isError = false,
                    leaveDays = recommendation.leaveDays,
                    selectedYear = recommendation.year,
                    selectedMonth = recommendation.month,
                    selectedDate = defaultSelectedPeriod?.startDate,
                    selectedPeriodId = defaultSelectedPeriod?.id,
                    calendarRecommendation = recommendation,
                )
            }.onFailure {
                calendarUiState = calendarUiState.copy(
                    isLoading = false,
                    isError = true,
                    calendarRecommendation = null,
                )
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}
