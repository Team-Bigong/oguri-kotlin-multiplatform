package com.bigong.oguri.feature.calendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bigong.oguri.core.util.extension.isUnauthorized
import com.bigong.oguri.domain.usecase.DeleteRecommendationUseCase
import com.bigong.oguri.domain.usecase.GetCalendarRecommendationUseCase
import com.bigong.oguri.domain.usecase.ObservePreferredLeaveDaysChangesUseCase
import com.bigong.oguri.domain.usecase.ObserveRecommendationSavedChangesUseCase
import com.bigong.oguri.domain.usecase.SaveRecommendationUseCase
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import com.bigong.oguri.feature.calendar.ui.model.CalendarSideEffect
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import com.bigong.oguri.feature.calendar.ui.model.createRecommendationPeriodKey
import com.bigong.oguri.feature.calendar.ui.model.toRecommendationPeriodKey
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Inject
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val getCalendarRecommendationUseCase: GetCalendarRecommendationUseCase,
    private val calculateDDayUseCase: com.bigong.oguri.domain.usecase.CalculateDDayUseCase,
    private val saveRecommendationUseCase: SaveRecommendationUseCase,
    private val deleteRecommendationUseCase: DeleteRecommendationUseCase,
    private val observeRecommendationSavedChangesUseCase: ObserveRecommendationSavedChangesUseCase,
    private val observePreferredLeaveDaysChangesUseCase: ObservePreferredLeaveDaysChangesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<CalendarSideEffect>(extraBufferCapacity = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    private val pagingQueryFlow = MutableStateFlow(CalendarPagingQuery())
    private val periodCardById = MutableStateFlow<Map<Long, CalendarPeriodCardUiModel>>(emptyMap())

    val pagedPeriodCards: Flow<PagingData<CalendarPeriodCardUiModel>> =
        pagingQueryFlow
            .flatMapLatest { query: CalendarPagingQuery ->
                if (query.year <= 0) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(
                        config =
                            PagingConfig(
                                pageSize = CALENDAR_PAGE_SIZE,
                                initialLoadSize = CALENDAR_PAGE_SIZE,
                                prefetchDistance = CALENDAR_PREFETCH_DISTANCE,
                                enablePlaceholders = false,
                            ),
                        pagingSourceFactory = {
                            CalendarRecommendationPagingSource(
                                getCalendarRecommendationUseCase = getCalendarRecommendationUseCase,
                                calculateDDayUseCase = calculateDDayUseCase,
                                selectedYear = query.year,
                                selectedMonth = query.month,
                                dayOffCount = query.dayOffCount,
                                pageSize = CALENDAR_PAGE_SIZE,
                                onDayOffCountResolved = { resolvedDayOffCount: Int ->
                                    _uiState.update { currentUiState ->
                                        currentUiState.copy(leaveDays = resolvedDayOffCount)
                                    }
                                },
                                onPageLoaded = { loadedCards: List<CalendarPeriodCardUiModel> ->
                                    periodCardById.update { currentMap ->
                                        currentMap + loadedCards.associateBy { card -> card.id }
                                    }
                                    _uiState.update { currentUiState ->
                                        currentUiState.copy(
                                            isLeaveDaysRefreshing = false,
                                            expandedPeriodId = currentUiState.expandedPeriodId ?: loadedCards.firstOrNull()?.id,
                                        )
                                    }
                                },
                                onRefreshLoadFailed = {
                                    _uiState.update { currentUiState ->
                                        currentUiState.copy(isLeaveDaysRefreshing = false)
                                    }
                                },
                            )
                        },
                    ).flow
                }
            }.cachedIn(viewModelScope)

    init {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        _uiState.update { currentUiState ->
            currentUiState.copy(
                selectedYear = today.year,
                selectedMonth = null,
            )
        }
        pagingQueryFlow.value =
            CalendarPagingQuery(
                year = today.year,
                month = null,
                dayOffCount = _uiState.value.leaveDays,
                requestVersion = 0,
            )
        observeRecommendationSavedChanges()
        observePreferredLeaveDaysChanges()
    }

    fun updateLeaveDays(leaveDays: Int) {
        if (leaveDays <= 0 || leaveDays == uiState.value.leaveDays) {
            return
        }
        _uiState.update { currentUiState ->
            currentUiState.copy(
                leaveDays = leaveDays,
                isLeaveDaysRefreshing = true,
                expandedPeriodId = null,
                selectedDateByPeriodId = emptyMap(),
            )
        }
        periodCardById.value = emptyMap()
        _sideEffect.tryEmit(CalendarSideEffect.LeaveDaysUpdated)
        refreshByDayOffCount(dayOffCount = leaveDays)
    }

    fun updatePeriodFilter(
        year: Int,
        month: Int?,
    ) {
        val resolvedMonth = month?.takeIf { selectedMonth: Int -> selectedMonth in MONTH_MIN_VALUE..MONTH_MAX_VALUE }
        val currentUiState: CalendarUiState = uiState.value
        if (currentUiState.selectedYear == year && currentUiState.selectedMonth == resolvedMonth) {
            return
        }
        _uiState.update { previousUiState ->
            previousUiState.copy(
                selectedYear = year,
                selectedMonth = resolvedMonth,
                expandedPeriodId = null,
                selectedDateByPeriodId = emptyMap(),
                savedStateByPeriodKey = emptyMap(),
            )
        }
        periodCardById.value = emptyMap()
        refreshByPeriodFilter(year = year, month = resolvedMonth)
    }

    fun refreshWithPreferredLeaveDays(preferredLeaveDays: Int) {
        val normalizedLeaveDays = preferredLeaveDays.coerceAtLeast(1)
        if (normalizedLeaveDays == uiState.value.leaveDays) {
            return
        }
        _uiState.update { currentUiState ->
            currentUiState.copy(
                leaveDays = normalizedLeaveDays,
                isLeaveDaysRefreshing = true,
                expandedPeriodId = null,
                selectedDateByPeriodId = emptyMap(),
                savedStateByPeriodKey = emptyMap(),
            )
        }
        periodCardById.value = emptyMap()
        refreshByDayOffCount(dayOffCount = normalizedLeaveDays)
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
        val periodCard: CalendarPeriodCardUiModel = periodCardById.value[periodId] ?: return
        if (date !in periodCard.startDate..periodCard.endDate) {
            return
        }
        _uiState.update { currentUiState ->
            currentUiState.copy(
                selectedDateByPeriodId = currentUiState.selectedDateByPeriodId + (periodId to date),
            )
        }
        _sideEffect.tryEmit(
            CalendarSideEffect.NavigateToPeriodDetail(
                startDate = periodCard.startDate.toString(),
                endDate = periodCard.endDate.toString(),
            ),
        )
    }

    fun onDetailClick(periodId: Long) {
        val periodCard: CalendarPeriodCardUiModel = periodCardById.value[periodId] ?: return
        _sideEffect.tryEmit(
            CalendarSideEffect.NavigateToPeriodDetail(
                startDate = periodCard.startDate.toString(),
                endDate = periodCard.endDate.toString(),
            ),
        )
    }

    fun toggleSaved(periodId: Long) {
        val periodCard: CalendarPeriodCardUiModel = periodCardById.value[periodId] ?: return
        val periodKey: String = periodCard.toRecommendationPeriodKey()
        val previousSavedState = uiState.value.savedStateByPeriodKey[periodKey] ?: periodCard.isSaved
        val nextSavedState: Boolean = !previousSavedState

        _uiState.update { currentUiState ->
            currentUiState.copy(
                savedStateByPeriodKey =
                    currentUiState.savedStateByPeriodKey + (periodKey to nextSavedState),
            )
        }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    if (nextSavedState) {
                        saveRecommendationUseCase(
                            startDate = periodCard.startDate,
                            endDate = periodCard.endDate,
                            dayOffCount = periodCard.dayOffCount,
                            totalTripCount = periodCard.totalTripCount,
                        )
                    } else {
                        deleteRecommendationUseCase(
                            startDate = periodCard.startDate,
                            endDate = periodCard.endDate,
                            dayOffCount = periodCard.dayOffCount,
                            totalTripCount = periodCard.totalTripCount,
                        )
                    }
                }
            }.onSuccess {
                _sideEffect.tryEmit(
                    if (nextSavedState) {
                        CalendarSideEffect.RecommendationSaved
                    } else {
                        CalendarSideEffect.RecommendationDeleted
                    },
                )
            }.onFailure {
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        savedStateByPeriodKey =
                            currentUiState.savedStateByPeriodKey + (periodKey to previousSavedState),
                    )
                }
                if (it.isUnauthorized()) {
                    _sideEffect.tryEmit(CalendarSideEffect.LoginRequired)
                }
            }
        }
    }

    private fun observeRecommendationSavedChanges() {
        viewModelScope.launch {
            observeRecommendationSavedChangesUseCase().collect { change ->
                val periodKey =
                    createRecommendationPeriodKey(
                        startDate = change.startDate,
                        endDate = change.endDate,
                        dayOffCount = change.dayOffCount,
                        totalTripCount = change.totalTripCount,
                    )
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        savedStateByPeriodKey =
                            currentUiState.savedStateByPeriodKey + (periodKey to change.isSaved),
                    )
                }
            }
        }
    }

    private fun observePreferredLeaveDaysChanges() {
        viewModelScope.launch {
            observePreferredLeaveDaysChangesUseCase().collect { change ->
                val preferredLeaveDays: Int = change.preferredLeaveDays.coerceAtLeast(1)
                if (preferredLeaveDays == uiState.value.leaveDays) {
                    return@collect
                }
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        leaveDays = preferredLeaveDays,
                        isLeaveDaysRefreshing = true,
                        expandedPeriodId = null,
                        selectedDateByPeriodId = emptyMap(),
                        savedStateByPeriodKey = emptyMap(),
                    )
                }
                periodCardById.value = emptyMap()
                refreshByDayOffCount(dayOffCount = preferredLeaveDays)
            }
        }
    }

    private fun refreshByDayOffCount(dayOffCount: Int) {
        val query: CalendarPagingQuery = pagingQueryFlow.value
        pagingQueryFlow.value =
            query.copy(
                dayOffCount = dayOffCount,
                requestVersion = query.requestVersion + 1,
            )
    }

    private fun refreshByPeriodFilter(
        year: Int,
        month: Int?,
    ) {
        val query: CalendarPagingQuery = pagingQueryFlow.value
        pagingQueryFlow.value =
            query.copy(
                year = year,
                month = month,
                requestVersion = query.requestVersion + 1,
            )
    }

    private data class CalendarPagingQuery(
        val year: Int = 0,
        val month: Int? = null,
        val dayOffCount: Int? = null,
        val requestVersion: Int = 0,
    )

    private companion object {
        private const val CALENDAR_PAGE_SIZE = 10
        private const val CALENDAR_PREFETCH_DISTANCE = 6
        private const val MONTH_MIN_VALUE = 1
        private const val MONTH_MAX_VALUE = 12
    }
}
