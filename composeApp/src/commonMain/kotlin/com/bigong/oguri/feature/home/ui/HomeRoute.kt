package com.bigong.oguri.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.data.repository.UserStateRepository
import com.bigong.oguri.feature.common.ui.rememberRouteViewModel
import com.bigong.oguri.feature.home.ui.model.HomeUiState

@Composable
fun HomeRoute(
    annualLeaveStrategyRepository: AnnualLeaveStrategyRepository,
    userStateRepository: UserStateRepository,
    onStrategyDetailClick: (String) -> Unit,
    onShowSnackbarClick: () -> Unit,
) {
    val homeViewModel: HomeViewModel = rememberRouteViewModel(
        annualLeaveStrategyRepository,
        userStateRepository,
    ) {
        HomeViewModel(
            annualLeaveStrategyRepository = annualLeaveStrategyRepository,
            userStateRepository = userStateRepository,
        )
    }
    val homeUiState: HomeUiState by homeViewModel.homeUiStateFlow.collectAsState()

    HomeScreen(
        homeUiState = homeUiState,
        onRetryClick = homeViewModel::refresh,
        onStrategyDetailClick = onStrategyDetailClick,
        onShowSnackbarClick = onShowSnackbarClick,
    )
}
