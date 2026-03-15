package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bigong.oguri.domain.usecase.DeleteRecommendationUseCase
import com.bigong.oguri.domain.usecase.GetCalendarPeriodDetailUseCase
import com.bigong.oguri.domain.usecase.GetRecommendPeriodListUseCase
import com.bigong.oguri.domain.usecase.SaveRecommendationUseCase
import com.bigong.oguri.feature.perioddetail.ui.model.PeriodDetailUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_USER_COUNTRY: String = "대한민국"

@Inject
class PeriodDetailViewModel(
    private val getCalendarPeriodDetailUseCase: GetCalendarPeriodDetailUseCase,
    private val getRecommendPeriodListUseCase: GetRecommendPeriodListUseCase,
    private val saveRecommendationUseCase: SaveRecommendationUseCase,
    private val deleteRecommendationUseCase: DeleteRecommendationUseCase,
) : ViewModel() {
    private val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var periodDetailUiState: PeriodDetailUiState by mutableStateOf(PeriodDetailUiState())
        private set

    fun loadPeriodDetail(
        startDate: String,
        endDate: String,
    ) {
        viewModelScope.launch {
            periodDetailUiState = periodDetailUiState.copy(isLoading = true, isError = false)

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
                            getRecommendPeriodListUseCase(userCountry = DEFAULT_USER_COUNTRY)
                        }
                    }.getOrDefault(emptyList())
                        .any { recommendPeriod ->
                            recommendPeriod.startDate == periodDetail.startDate &&
                                recommendPeriod.endDate == periodDetail.endDate &&
                                recommendPeriod.dayOffCount == periodDetail.dayOffCount &&
                                recommendPeriod.isSaved
                        }

                periodDetailUiState = periodDetailUiState.copy(
                    isLoading = false,
                    isError = false,
                    periodDetail = periodDetail,
                    isSaved = isInitiallySaved,
                )
            }.onFailure {
                periodDetailUiState = periodDetailUiState.copy(
                    isLoading = false,
                    isError = true,
                    periodDetail = null,
                    isSaved = false,
                )
            }
        }
    }

    fun toggleSavedRecommendation() {
        val periodDetail = periodDetailUiState.periodDetail ?: return
        val previousSavedState = periodDetailUiState.isSaved
        val nextSavedState = !previousSavedState

        periodDetailUiState = periodDetailUiState.copy(isSaved = nextSavedState)

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    if (nextSavedState) {
                        saveRecommendationUseCase(
                            startDate = periodDetail.startDate,
                            endDate = periodDetail.endDate,
                            dayOffCount = periodDetail.dayOffCount,
                        )
                    } else {
                        deleteRecommendationUseCase(
                            startDate = periodDetail.startDate,
                            endDate = periodDetail.endDate,
                            dayOffCount = periodDetail.dayOffCount,
                        )
                    }
                }
            }.onFailure {
                periodDetailUiState = periodDetailUiState.copy(isSaved = previousSavedState)
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}
