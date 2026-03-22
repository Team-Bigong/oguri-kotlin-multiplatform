package com.bigong.oguri.feature.perioddetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bigong.oguri.core.util.extension.isUnauthorized
import com.bigong.oguri.domain.model.CalendarPeriodDetail
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.usecase.CalculateDDayUseCase
import com.bigong.oguri.domain.usecase.DeleteRecommendationUseCase
import com.bigong.oguri.domain.usecase.GetCalendarPeriodDetailUseCase
import com.bigong.oguri.domain.usecase.GetMyPageInfoUseCase
import com.bigong.oguri.domain.usecase.SaveRecommendationUseCase
import com.bigong.oguri.feature.perioddetail.ui.model.PeriodDetailSideEffect
import com.bigong.oguri.feature.perioddetail.ui.model.PeriodDetailUiState
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Inject
@OptIn(ExperimentalCoroutinesApi::class)
class PeriodDetailViewModel(
    private val getCalendarPeriodDetailUseCase: GetCalendarPeriodDetailUseCase,
    private val getMyPageInfoUseCase: GetMyPageInfoUseCase,
    private val saveRecommendationUseCase: SaveRecommendationUseCase,
    private val deleteRecommendationUseCase: DeleteRecommendationUseCase,
    private val calculateDDayUseCase: CalculateDDayUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PeriodDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<PeriodDetailSideEffect>(extraBufferCapacity = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    private val queryFlow = MutableStateFlow(PeriodDetailPagingQuery())
    private var lastResolvedPeriodDetail: CalendarPeriodDetail? = null

    val pagedPlaces: Flow<PagingData<Place>> =
        queryFlow
            .flatMapLatest { query: PeriodDetailPagingQuery ->
                if (query.startDate.isBlank() || query.endDate.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(
                        config =
                            PagingConfig(
                                pageSize = PERIOD_DETAIL_PAGE_SIZE,
                                initialLoadSize = PERIOD_DETAIL_PAGE_SIZE,
                                prefetchDistance = 2,
                                enablePlaceholders = false,
                            ),
                        pagingSourceFactory = {
                            PeriodDetailPlacePagingSource(
                                getCalendarPeriodDetailUseCase = getCalendarPeriodDetailUseCase,
                                startDate = query.startDate,
                                endDate = query.endDate,
                                pageSize = PERIOD_DETAIL_PAGE_SIZE,
                                onFirstPageResolved = { periodDetail: CalendarPeriodDetail ->
                                    lastResolvedPeriodDetail = periodDetail
                                    resolveHeader(periodDetail)
                                },
                            )
                        },
                    ).flow
                }
            }.cachedIn(viewModelScope)

    fun loadPeriodDetail(
        startDate: String,
        endDate: String,
    ) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                isLoading = true,
                isError = false,
                periodDetail = null,
                isSaved = false,
                dDay = 0,
            )
        }
        lastResolvedPeriodDetail = null
        val query: PeriodDetailPagingQuery = queryFlow.value
        queryFlow.value =
            query.copy(
                startDate = startDate,
                endDate = endDate,
                requestVersion = query.requestVersion + 1,
            )
    }

    fun refreshPeriodDetailSilently() {
        val query: PeriodDetailPagingQuery = queryFlow.value
        if (query.startDate.isBlank() || query.endDate.isBlank()) {
            return
        }
        queryFlow.value =
            query.copy(
                requestVersion = query.requestVersion + 1,
            )
    }

    fun toggleSavedRecommendation() {
        val periodDetail: CalendarPeriodDetail = uiState.value.periodDetail ?: return
        val previousSavedState: Boolean = uiState.value.isSaved
        val nextSavedState: Boolean = !previousSavedState

        _uiState.update { currentUiState ->
            currentUiState.copy(isSaved = nextSavedState)
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    if (nextSavedState) {
                        saveRecommendationUseCase(
                            startDate = periodDetail.startDate,
                            endDate = periodDetail.endDate,
                            dayOffCount = periodDetail.dayOffCount,
                            totalTripCount = periodDetail.totalTripCount,
                        )
                    } else {
                        deleteRecommendationUseCase(
                            startDate = periodDetail.startDate,
                            endDate = periodDetail.endDate,
                            dayOffCount = periodDetail.dayOffCount,
                            totalTripCount = periodDetail.totalTripCount,
                        )
                    }
                }
            }.onSuccess {
                _sideEffect.tryEmit(
                    if (nextSavedState) {
                        PeriodDetailSideEffect.RecommendationSaved
                    } else {
                        PeriodDetailSideEffect.RecommendationDeleted
                    },
                )
            }.onFailure {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isSaved = previousSavedState)
                }
                if (it.isUnauthorized()) {
                    _sideEffect.tryEmit(PeriodDetailSideEffect.LoginRequired)
                }
            }
        }
    }

    private fun resolveHeader(periodDetail: CalendarPeriodDetail) {
        viewModelScope.launch {
            val todayDate =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            val isInitiallySaved =
                runCatching {
                    withContext(Dispatchers.Default) {
                        getMyPageInfoUseCase().selectedPeriods
                    }
                }.getOrDefault(emptyList())
                    .any { selectedPeriod ->
                        selectedPeriod.startDate == periodDetail.startDate &&
                            selectedPeriod.endDate == periodDetail.endDate &&
                            selectedPeriod.dayOffCount == periodDetail.dayOffCount
                    }

            _uiState.update { currentUiState ->
                currentUiState.copy(
                    isLoading = false,
                    isError = false,
                    periodDetail = periodDetail,
                    isSaved = isInitiallySaved,
                    dDay = calculateDDayUseCase(todayDate = todayDate, targetDate = periodDetail.startDate),
                )
            }
        }
    }

    private data class PeriodDetailPagingQuery(
        val startDate: String = "",
        val endDate: String = "",
        val requestVersion: Int = 0,
    )

    private companion object {
        private const val PERIOD_DETAIL_PAGE_SIZE = 10
    }
}
