package com.bigong.oguri.feature.home.ui

import com.bigong.oguri.data.model.HomeStrategyRecommendation
import com.bigong.oguri.data.model.UserState
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.data.repository.UserStateRepository
import com.bigong.oguri.feature.common.ui.RouteViewModel
import com.bigong.oguri.feature.home.ui.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val annualLeaveStrategyRepository: AnnualLeaveStrategyRepository,
    private val userStateRepository: UserStateRepository,
) : RouteViewModel() {
    private val mutableHomeUiStateFlow: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    val homeUiStateFlow: StateFlow<HomeUiState> = mutableHomeUiStateFlow.asStateFlow()

    init {
        routeViewModelScope.launch {
            userStateRepository.userStateFlow.collect { latestUserState: UserState ->
                mutableHomeUiStateFlow.update { previousUiState: HomeUiState ->
                    previousUiState.copy(userState = latestUserState)
                }
            }
        }
        refresh()
    }

    fun refresh() {
        routeViewModelScope.launch {
            mutableHomeUiStateFlow.update { previousUiState: HomeUiState ->
                previousUiState.copy(isLoading = true, isError = false)
            }
            runCatching {
                annualLeaveStrategyRepository.getHomeStrategyRecommendation()
            }.onSuccess { homeStrategyRecommendation: HomeStrategyRecommendation ->
                mutableHomeUiStateFlow.update { previousUiState: HomeUiState ->
                    previousUiState.copy(
                        isLoading = false,
                        isError = false,
                        recommendation = homeStrategyRecommendation,
                    )
                }
            }.onFailure {
                mutableHomeUiStateFlow.update { previousUiState: HomeUiState ->
                    previousUiState.copy(
                        isLoading = false,
                        isError = true,
                        recommendation = null,
                    )
                }
            }
        }
    }
}
