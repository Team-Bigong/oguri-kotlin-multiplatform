package com.bigong.oguri.feature.perioddetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.usecase.DeleteRecommendationUseCase
import com.bigong.oguri.domain.usecase.GetCalendarPeriodDetailUseCase
import com.bigong.oguri.domain.usecase.GetMyPageInfoUseCase
import com.bigong.oguri.domain.usecase.SaveRecommendationUseCase
import com.bigong.oguri.feature.perioddetail.ui.model.PeriodDetailUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class PeriodDetailViewModel(
    private val getCalendarPeriodDetailUseCase: GetCalendarPeriodDetailUseCase,
    private val getMyPageInfoUseCase: GetMyPageInfoUseCase,
    private val saveRecommendationUseCase: SaveRecommendationUseCase,
    private val deleteRecommendationUseCase: DeleteRecommendationUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PeriodDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadPeriodDetail(
        startDate: String,
        endDate: String,
    ) {
        viewModelScope.launch {
            _uiState.update { currentUiState ->
                currentUiState.copy(isLoading = true, isError = false)
            }

            runCatching {
                withContext(Dispatchers.Default) {
                    getCalendarPeriodDetailUseCase(
                        startDate = startDate,
                        endDate = endDate,
                    )
                }
            }.onSuccess { periodDetail ->
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
                    )
                }
            }.onFailure {
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = true,
                        periodDetail = null,
                        isSaved = false,
                    )
                }
            }
        }
    }

    fun toggleSavedRecommendation() {
        val periodDetail = uiState.value.periodDetail ?: return
        val previousSavedState = uiState.value.isSaved
        val nextSavedState = !previousSavedState

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
            }.onFailure {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isSaved = previousSavedState)
                }
            }
        }
    }
}
