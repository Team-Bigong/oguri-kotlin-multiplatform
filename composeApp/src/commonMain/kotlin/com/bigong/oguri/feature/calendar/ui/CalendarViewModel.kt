package com.bigong.oguri.feature.calendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.model.CalendarPeriod
import com.bigong.oguri.domain.model.CalendarRecommendation
import com.bigong.oguri.domain.usecase.CalculateDDayUseCase
import com.bigong.oguri.domain.usecase.GetCalendarRecommendationUseCase
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import com.bigong.oguri.feature.calendar.ui.model.CalendarSideEffect
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Inject
class CalendarViewModel(
    private val getCalendarRecommendationUseCase: GetCalendarRecommendationUseCase,
    private val calculateDDayUseCase: CalculateDDayUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<CalendarSideEffect>(extraBufferCapacity = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    private var currentPageIndex: Int = 0

    init {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        _uiState.update { currentUiState ->
            currentUiState.copy(
                selectedYear = today.year,
                selectedMonth = today.month.ordinal + 1,
            )
        }
        loadInitialCalendar()
    }

    fun updateLeaveDays(leaveDays: Int) {
        if (leaveDays <= 0 || leaveDays == uiState.value.leaveDays) {
            return
        }
        _uiState.update { currentUiState ->
            currentUiState.copy(leaveDays = leaveDays)
        }
        _sideEffect.tryEmit(CalendarSideEffect.LeaveDaysUpdated)
        refreshPagedRecommendations()
    }

    fun onCardClick(periodId: Long) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                expandedPeriodId = if (currentUiState.expandedPeriodId == periodId) null else periodId,
            )
        }
    }

    fun onCardDateClick(
        periodId: Long,
        date: LocalDate,
    ) {
        val period =
            uiState.value.periodCards.firstOrNull { card ->
                card.id == periodId
            } ?: return

        if (date !in period.startDate..period.endDate) {
            return
        }

        _uiState.update { currentUiState ->
            currentUiState.copy(
                selectedDateByPeriodId =
                    currentUiState.selectedDateByPeriodId +
                        (periodId to date),
            )
        }

        _sideEffect.tryEmit(
            CalendarSideEffect.NavigateToPeriodDetail(
                startDate = period.startDate.toString(),
                endDate = period.endDate.toString(),
            ),
        )
    }

    fun onDetailClick(periodId: Long) {
        val period =
            uiState.value.periodCards.firstOrNull { card ->
                card.id == periodId
            } ?: return

        _sideEffect.tryEmit(
            CalendarSideEffect.NavigateToPeriodDetail(
                startDate = period.startDate.toString(),
                endDate = period.endDate.toString(),
            ),
        )
    }

    fun loadNextPage() {
        val currentUiState = uiState.value
        if (!currentUiState.hasMorePage || currentUiState.isLoading || currentUiState.isLoadingNextPage) {
            return
        }
        fetchRecommendationPage(isInitial = false)
    }

    fun retry() {
        refreshPagedRecommendations()
    }

    private fun loadInitialCalendar() {
        viewModelScope.launch {
            _uiState.update { currentUiState ->
                currentUiState.copy(isLoading = true, isError = false)
            }

            runCatching {
                withContext(Dispatchers.Default) {
                    getCalendarRecommendationUseCase.getPreferredDayOffCount()
                }
            }.onSuccess { preferredDayOffCount ->
                val resolvedLeaveDays = preferredDayOffCount.coerceAtLeast(1)
                _uiState.update { currentUiState ->
                    currentUiState.copy(leaveDays = resolvedLeaveDays)
                }
                refreshPagedRecommendations()
            }.onFailure {
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = true,
                    )
                }
            }
        }
    }

    private fun refreshPagedRecommendations() {
        currentPageIndex = 0
        _uiState.update { currentUiState ->
            currentUiState.copy(
                isLoading = true,
                isError = false,
                hasMorePage = true,
                expandedPeriodId = null,
                periodCards = emptyList(),
                selectedDateByPeriodId = emptyMap(),
            )
        }
        fetchRecommendationPage(isInitial = true)
    }

    private fun fetchRecommendationPage(isInitial: Boolean) {
        viewModelScope.launch {
            val currentUiState = uiState.value
            _uiState.update { previousUiState ->
                previousUiState.copy(
                    isLoading = if (isInitial) true else previousUiState.isLoading,
                    isLoadingNextPage = if (isInitial) false else true,
                    isError = false,
                )
            }

            runCatching {
                withContext(Dispatchers.Default) {
                    val baseDate =
                        LocalDate.parse(
                            "${currentUiState.selectedYear}-${currentUiState.selectedMonth.toString().padStart(2, '0')}-01",
                        )
                    val targetDate = baseDate.plus(value = currentPageIndex, unit = DateTimeUnit.MONTH)
                    getCalendarRecommendationUseCase(
                        year = targetDate.year,
                        month = targetDate.month.ordinal + 1,
                        dayOffCount = currentUiState.leaveDays,
                    )
                }
            }.onSuccess { recommendation ->
                val appendedCards =
                    recommendation.toPeriodCards(pageIndex = currentPageIndex)
                val isLastPage = appendedCards.isEmpty() || currentPageIndex >= MAX_PAGE_INDEX

                _uiState.update { previousUiState ->
                    val mergedCards = if (isInitial) appendedCards else previousUiState.periodCards + appendedCards
                    val defaultExpandedId = previousUiState.expandedPeriodId ?: mergedCards.firstOrNull()?.id
                    val mergedSelectedDate =
                        previousUiState.selectedDateByPeriodId +
                            appendedCards.associate { periodCard ->
                                periodCard.id to periodCard.startDate
                            }

                    previousUiState.copy(
                        isLoading = false,
                        isLoadingNextPage = false,
                        isError = false,
                        hasMorePage = !isLastPage,
                        periodCards = mergedCards,
                        expandedPeriodId = defaultExpandedId,
                        selectedDateByPeriodId = mergedSelectedDate,
                    )
                }

                currentPageIndex += 1
            }.onFailure {
                _uiState.update { previousUiState ->
                    previousUiState.copy(
                        isLoading = false,
                        isLoadingNextPage = false,
                        isError = previousUiState.periodCards.isEmpty(),
                    )
                }
            }
        }
    }

    private fun CalendarRecommendation.toPeriodCards(pageIndex: Int): List<CalendarPeriodCardUiModel> =
        periods.mapIndexed { index: Int, period: CalendarPeriod ->
            val todayDate =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            val holidayNames =
                holidays
                    .filter { holiday ->
                        holiday.date in period.startDate..period.endDate
                    }.map { holiday -> holiday.name }

            CalendarPeriodCardUiModel(
                id = (pageIndex.toLong() * PAGE_ID_MULTIPLIER) + index + 1L,
                startDate = period.startDate,
                endDate = period.endDate,
                dDay = calculateDDayUseCase(todayDate = todayDate, targetDate = period.startDate),
                dayOffCount = leaveDays,
                totalTripCount = period.totalDayCount(),
                holidayNames = holidayNames,
                holidays = holidays,
            )
        }

    private fun CalendarPeriod.totalDayCount(): Int = (endDate.toEpochDays() - startDate.toEpochDays() + 1).toInt()

    private companion object {
        private const val PAGE_ID_MULTIPLIER = 1_000L
        private const val MAX_PAGE_INDEX = 11
    }
}
