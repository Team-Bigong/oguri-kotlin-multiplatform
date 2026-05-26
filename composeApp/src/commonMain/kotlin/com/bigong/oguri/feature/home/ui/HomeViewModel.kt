package com.bigong.oguri.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.core.util.extension.isUnauthorized
import com.bigong.oguri.domain.usecase.DeleteRecommendationUseCase
import com.bigong.oguri.domain.usecase.GetMonthlyTopPeriodListUseCase
import com.bigong.oguri.domain.usecase.GetRecommendPeriodListUseCase
import com.bigong.oguri.domain.usecase.GetWeeklyTopPlaceListUseCase
import com.bigong.oguri.domain.usecase.SaveRecommendationUseCase
import com.bigong.oguri.core.util.extension.isUnauthorized
import com.bigong.oguri.feature.home.ui.model.HomeSideEffect
import com.bigong.oguri.feature.home.ui.model.HomeUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_HOME_USER_COUNTRY = "대한민국"

@Inject
class HomeViewModel(
    private val getRecommendPeriodsUseCase: GetRecommendPeriodListUseCase,
    private val getWeeklyTopPlacesUseCase: GetWeeklyTopPlaceListUseCase,
    private val getMonthlyTopPeriodsUseCase: GetMonthlyTopPeriodListUseCase,
    private val saveRecommendationUseCase: SaveRecommendationUseCase,
    private val deleteRecommendationUseCase: DeleteRecommendationUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<HomeSideEffect>(extraBufferCapacity = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        loadRecommendPeriods()
        loadWeeklyTopPlaces()
        loadMonthlyTopPeriods()
    }

    fun loadRecommendPeriods() {
        fetchRecommendPeriods(showLoading = uiState.value.recommendPeriods.isEmpty())
    }

    fun refreshRecommendPeriods() {
        fetchRecommendPeriods(showLoading = false)
    }

    fun loadWeeklyTopPlaces() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    getWeeklyTopPlacesUseCase()
                }
            }.onSuccess { weeklyTopPlaces ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(weeklyTopPlaces = weeklyTopPlaces)
                }
            }
        }
    }

    fun loadMonthlyTopPeriods() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    getMonthlyTopPeriodsUseCase()
                }
            }.onSuccess { monthlyTopPeriods ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(monthlyTopPeriods = monthlyTopPeriods)
                }
            }
        }
    }

    private fun fetchRecommendPeriods(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isLoading = true, isError = false)
                }
            } else {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isError = false)
                }
            }

            runCatching {
                withContext(Dispatchers.Default) {
                    getRecommendPeriodsUseCase(userCountry = DEFAULT_HOME_USER_COUNTRY)
                }
            }.onSuccess { recommendPeriods ->
                val currentSelectedRank = uiState.value.selectedRank
                val selectedRank =
                    recommendPeriods
                        .firstOrNull { period ->
                            period.rank == currentSelectedRank
                        }?.rank ?: recommendPeriods.firstOrNull()?.rank ?: 1
                val savedRanks = recommendPeriods.filter { period -> period.isSaved }.map { period -> period.rank }.toSet()
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = false,
                        selectedRank = selectedRank,
                        savedRankSet = savedRanks,
                        recommendPeriods = recommendPeriods,
                    )
                }
            }.onFailure {
                _uiState.update { currentUiState ->
                    val hasExistingData = currentUiState.recommendPeriods.isNotEmpty()
                    if (hasExistingData) {
                        currentUiState.copy(
                            isLoading = false,
                            isError = false,
                        )
                    } else {
                        currentUiState.copy(
                            isLoading = false,
                            isError = true,
                            recommendPeriods = emptyList(),
                        )
                    }
                }
            }
        }
    }

    fun selectRank(rank: Int) {
        _uiState.update { currentUiState ->
            currentUiState.copy(selectedRank = rank)
        }
    }

    fun toggleSaved(isChecked: Boolean) {
        val selectedRank = uiState.value.selectedRank
        val previousSavedRankSet = uiState.value.savedRankSet
        val wasSaved = selectedRank in previousSavedRankSet
        val selectedPeriod =
            uiState.value.recommendPeriods.firstOrNull { period ->
                period.rank == selectedRank
            } ?: return

        val optimisticSavedRankSet =
            if (isChecked) {
                previousSavedRankSet + selectedRank
            } else {
                previousSavedRankSet - selectedRank
            }
        _uiState.update { currentUiState ->
            currentUiState.copy(savedRankSet = optimisticSavedRankSet)
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    if (isChecked) {
                        saveRecommendationUseCase(
                            startDate = selectedPeriod.startDate,
                            endDate = selectedPeriod.endDate,
                            dayOffCount = selectedPeriod.dayOffCount,
                            totalTripCount = selectedPeriod.totalTripCount,
                        )
                    } else {
                        deleteRecommendationUseCase(
                            startDate = selectedPeriod.startDate,
                            endDate = selectedPeriod.endDate,
                            dayOffCount = selectedPeriod.dayOffCount,
                            totalTripCount = selectedPeriod.totalTripCount,
                        )
                    }
                }
            }.onSuccess {
                _sideEffect.tryEmit(
                    if (isChecked) {
                        HomeSideEffect.RecommendationSaved
                    } else {
                        HomeSideEffect.RecommendationDeleted
                    },
                )
            }.onFailure {
                val restoredSavedRankSet =
                    if (wasSaved) {
                        uiState.value.savedRankSet + selectedRank
                    } else {
                        uiState.value.savedRankSet - selectedRank
                    }
                _uiState.update { currentUiState ->
                    currentUiState.copy(savedRankSet = restoredSavedRankSet)
                }
                if (it.isUnauthorized()) {
                    _sideEffect.tryEmit(HomeSideEffect.LoginRequired)
                }
            }
        }
    }
}
