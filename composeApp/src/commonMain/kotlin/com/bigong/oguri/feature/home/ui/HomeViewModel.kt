package com.bigong.oguri.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Inject
import com.bigong.oguri.domain.usecase.GetRecommendPeriodListUseCase
import com.bigong.oguri.feature.home.ui.model.HomeUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class HomeViewModel(
    private val getRecommendPeriodsUseCase: GetRecommendPeriodListUseCase,
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
                    getRecommendPeriodsUseCase()
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
        val selectedRank = homeUiState.selectedRank
        val nextSavedRankSet = if (isChecked) {
            homeUiState.savedRankSet + selectedRank
        } else {
            homeUiState.savedRankSet - selectedRank
        }
        homeUiState = homeUiState.copy(savedRankSet = nextSavedRankSet)
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}
