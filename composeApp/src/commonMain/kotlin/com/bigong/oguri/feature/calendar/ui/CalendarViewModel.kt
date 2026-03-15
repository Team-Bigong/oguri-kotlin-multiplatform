package com.bigong.oguri.feature.calendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.model.CalendarPeriod
import com.bigong.oguri.domain.usecase.GetCalendarRecommendationUseCase
import com.bigong.oguri.feature.calendar.ui.model.CalendarSideEffect
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

@Inject
class CalendarViewModel(
    private val getCalendarRecommendationUseCase: GetCalendarRecommendationUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<CalendarUiState> = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    private val _sideEffect: MutableSharedFlow<CalendarSideEffect> = MutableSharedFlow(extraBufferCapacity = 1)
    val sideEffect: SharedFlow<CalendarSideEffect> = _sideEffect.asSharedFlow()

    init {
        loadInitialCalendar()
    }

    fun updateLeaveDays(leaveDays: Int) {
        if (leaveDays <= 0) {
            return
        }
        _uiState.update { currentUiState: CalendarUiState ->
            currentUiState.copy(leaveDays = leaveDays)
        }
        fetchCalendarRecommendation(
            year = uiState.value.selectedYear,
            month = uiState.value.selectedMonth,
            leaveDays = leaveDays,
        )
    }

    fun updateYearMonth(
        year: Int,
        month: Int,
    ) {
        _uiState.update { currentUiState: CalendarUiState ->
            currentUiState.copy(
                selectedYear = year,
                selectedMonth = month,
            )
        }
        fetchCalendarRecommendation(
            year = year,
            month = month,
            leaveDays = uiState.value.leaveDays,
        )
    }

    fun onDateClick(
        date: LocalDate,
    ) {
        val matchedPeriod =
            uiState.value.calendarRecommendation?.periods?.firstOrNull { period: CalendarPeriod ->
                date in period.startDate..period.endDate
            }
        if (matchedPeriod == null) {
            return
        }

        val isSameDateTapped = uiState.value.selectedDate == date
        val isSamePeriodTapped = matchedPeriod.id == uiState.value.selectedPeriodId
        if (isSameDateTapped && isSamePeriodTapped) {
            _sideEffect.tryEmit(
                CalendarSideEffect.NavigateToPeriodDetail(
                    startDate = matchedPeriod.startDate.toString(),
                    endDate = matchedPeriod.endDate.toString(),
                ),
            )
            return
        }

        _uiState.update { currentUiState: CalendarUiState ->
            currentUiState.copy(
                selectedDate = date,
                selectedPeriodId = matchedPeriod.id,
            )
        }
    }

    fun onPeriodClick(
        periodId: Long,
    ) {
        if (uiState.value.selectedPeriodId == periodId) {
            val selectedPeriodForNavigation =
                uiState.value.calendarRecommendation?.periods?.firstOrNull { period: CalendarPeriod ->
                    period.id == periodId
                } ?: return
            _sideEffect.tryEmit(
                CalendarSideEffect.NavigateToPeriodDetail(
                    startDate = selectedPeriodForNavigation.startDate.toString(),
                    endDate = selectedPeriodForNavigation.endDate.toString(),
                ),
            )
            return
        }

        val selectedPeriod =
            uiState.value.calendarRecommendation?.periods?.firstOrNull { period: CalendarPeriod ->
                period.id == periodId
            } ?: return

        _uiState.update { currentUiState: CalendarUiState ->
            currentUiState.copy(
                selectedPeriodId = periodId,
                selectedDate = selectedPeriod.startDate,
            )
        }
    }

    fun retry() {
        fetchCalendarRecommendation(
            year = uiState.value.selectedYear,
            month = uiState.value.selectedMonth,
            leaveDays = uiState.value.leaveDays,
        )
    }

    private fun loadInitialCalendar() {
        viewModelScope.launch {
            _uiState.update { currentUiState: CalendarUiState ->
                currentUiState.copy(isLoading = true, isError = false)
            }
            runCatching {
                withContext(Dispatchers.Default) {
                    getCalendarRecommendationUseCase.getPreferredDayOffCount()
                }
            }.onSuccess { preferredDayOffCount ->
                val resolvedLeaveDays = preferredDayOffCount.coerceAtLeast(1)
                _uiState.update { currentUiState: CalendarUiState ->
                    currentUiState.copy(leaveDays = resolvedLeaveDays)
                }
                fetchCalendarRecommendation(
                    year = uiState.value.selectedYear,
                    month = uiState.value.selectedMonth,
                    leaveDays = resolvedLeaveDays,
                )
            }.onFailure {
                _uiState.update { currentUiState: CalendarUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = true,
                        calendarRecommendation = null,
                    )
                }
            }
        }
    }

    private fun fetchCalendarRecommendation(
        year: Int,
        month: Int,
        leaveDays: Int,
    ) {
        viewModelScope.launch {
            _uiState.update { currentUiState: CalendarUiState ->
                currentUiState.copy(isLoading = true, isError = false)
            }

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
                _uiState.update { currentUiState: CalendarUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = false,
                        leaveDays = leaveDays,
                        selectedYear = recommendation.year,
                        selectedMonth = recommendation.month,
                        selectedDate = defaultSelectedPeriod?.startDate,
                        selectedPeriodId = defaultSelectedPeriod?.id,
                        calendarRecommendation = recommendation,
                    )
                }
            }.onFailure {
                _uiState.update { currentUiState: CalendarUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = true,
                        calendarRecommendation = null,
                    )
                }
            }
        }
    }
}
