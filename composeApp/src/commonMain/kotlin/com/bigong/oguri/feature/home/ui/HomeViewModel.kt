package com.bigong.oguri.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bigong.oguri.domain.model.RecommendPeriod
import com.bigong.oguri.domain.usecase.DeleteRecommendationUseCase
import com.bigong.oguri.domain.usecase.GetRecommendPeriodListUseCase
import com.bigong.oguri.domain.usecase.SaveRecommendationUseCase
import com.bigong.oguri.feature.home.ui.model.HomeUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_HOME_USER_COUNTRY: String = "대한민국"

@Inject
class HomeViewModel(
    private val getRecommendPeriodsUseCase: GetRecommendPeriodListUseCase,
    private val saveRecommendationUseCase: SaveRecommendationUseCase,
    private val deleteRecommendationUseCase: DeleteRecommendationUseCase,
) : ViewModel() {
    private val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var homeUiState: HomeUiState by mutableStateOf(HomeUiState())
        private set

    init {
        loadRecommendPeriods()
    }

    fun loadRecommendPeriods() {
        viewModelScope.launch {
            homeUiState = homeUiState.copy(isLoading = true, isError = false)

            runCatching {
                withContext(Dispatchers.Default) {
                    getRecommendPeriodsUseCase(userCountry = DEFAULT_HOME_USER_COUNTRY)
                }
            }.onSuccess { recommendPeriods ->
                val selectedRank = recommendPeriods.firstOrNull()?.rank ?: 1
                val savedRanks = recommendPeriods.filter { period -> period.isSaved }.map { period -> period.rank }.toSet()
                homeUiState = homeUiState.copy(
                    isLoading = false,
                    isError = false,
                    selectedRank = selectedRank,
                    savedRankSet = savedRanks,
                    recommendPeriods = recommendPeriods,
                )
            }.onFailure {
                homeUiState = homeUiState.copy(
                    isLoading = false,
                    isError = true,
                    recommendPeriods = emptyList(),
                )
            }
        }
    }

    fun selectRank(rank: Int) {
        homeUiState = homeUiState.copy(selectedRank = rank)
    }

    fun toggleSaved(isChecked: Boolean) {
        val selectedPeriod =
            homeUiState.recommendPeriods.firstOrNull { period: RecommendPeriod ->
                period.rank == homeUiState.selectedRank
            } ?: return

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    if (isChecked) {
                        saveRecommendationUseCase(
                            startDate = selectedPeriod.startDate,
                            endDate = selectedPeriod.endDate,
                            dayOffCount = selectedPeriod.dayOffCount,
                        )
                    } else {
                        deleteRecommendationUseCase(
                            startDate = selectedPeriod.startDate,
                            endDate = selectedPeriod.endDate,
                            dayOffCount = selectedPeriod.dayOffCount,
                        )
                    }
                }
            }.onSuccess {
                val selectedRank = homeUiState.selectedRank
                val nextSavedRankSet = if (isChecked) {
                    homeUiState.savedRankSet + selectedRank
                } else {
                    homeUiState.savedRankSet - selectedRank
                }
                homeUiState = homeUiState.copy(savedRankSet = nextSavedRankSet)
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}
