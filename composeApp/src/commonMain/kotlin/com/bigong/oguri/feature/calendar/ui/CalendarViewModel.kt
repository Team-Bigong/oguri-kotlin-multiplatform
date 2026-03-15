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
        loadInitialCalendar()
    }

    fun updateLeaveDays(leaveDays: Int) {
        if (leaveDays <= 0) {
            return
        }
        calendarUiState = calendarUiState.copy(leaveDays = leaveDays)
        fetchCalendarRecommendation(
            year = calendarUiState.selectedYear,
            month = calendarUiState.selectedMonth,
            leaveDays = leaveDays,
        )
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
            leaveDays = calendarUiState.leaveDays,
        )
    }

    fun onDateClick(
        date: LocalDate,
        onOpenPeriodDetail: (String, String) -> Unit,
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
            onOpenPeriodDetail(
                matchedPeriod.startDate.toString(),
                matchedPeriod.endDate.toString(),
            )
            return
        }

        calendarUiState = calendarUiState.copy(
            selectedDate = date,
            selectedPeriodId = matchedPeriod.id,
        )
    }

    fun onPeriodClick(
        periodId: Long,
        onOpenPeriodDetail: (String, String) -> Unit,
    ) {
        if (calendarUiState.selectedPeriodId == periodId) {
            val selectedPeriodForNavigation =
                calendarUiState.calendarRecommendation?.periods?.firstOrNull { period: CalendarPeriod ->
                    period.id == periodId
                } ?: return
            onOpenPeriodDetail(
                selectedPeriodForNavigation.startDate.toString(),
                selectedPeriodForNavigation.endDate.toString(),
            )
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
            leaveDays = calendarUiState.leaveDays,
        )
    }

    private fun loadInitialCalendar() {
        viewModelScope.launch {
            calendarUiState = calendarUiState.copy(isLoading = true, isError = false)
            runCatching {
                withContext(Dispatchers.Default) {
                    getCalendarRecommendationUseCase.getPreferredDayOffCount()
                }
            }.onSuccess { preferredDayOffCount ->
                val resolvedLeaveDays = preferredDayOffCount.coerceAtLeast(1)
                calendarUiState = calendarUiState.copy(leaveDays = resolvedLeaveDays)
                fetchCalendarRecommendation(
                    year = calendarUiState.selectedYear,
                    month = calendarUiState.selectedMonth,
                    leaveDays = resolvedLeaveDays,
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

    private fun fetchCalendarRecommendation(
        year: Int,
        month: Int,
        leaveDays: Int,
    ) {
        viewModelScope.launch {
            calendarUiState = calendarUiState.copy(isLoading = true, isError = false)

            runCatching {
                withContext(Dispatchers.Default) {
                    getCalendarRecommendationUseCase(
                        year = year,
                        month = month,
                        dayOffCount = leaveDays,
                    )
                }
            }.onSuccess { recommendation ->
                val defaultSelectedPeriod = recommendation.periods.firstOrNull()
                calendarUiState = calendarUiState.copy(
                    isLoading = false,
                    isError = false,
                    leaveDays = leaveDays,
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
