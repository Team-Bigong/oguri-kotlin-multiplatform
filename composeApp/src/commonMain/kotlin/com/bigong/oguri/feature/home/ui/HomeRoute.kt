package com.bigong.oguri.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Provider

@Composable
fun HomeRoute(
    homeViewModelProvider: Provider<HomeViewModel>,
) {
    val homeViewModel: HomeViewModel = remember(homeViewModelProvider) {
        homeViewModelProvider()
    }

    HomeScreen(
        homeUiState = homeViewModel.homeUiState,
        onRankSelected = homeViewModel::selectRank,
        onSavedChanged = homeViewModel::toggleSaved,
        onRetryClick = homeViewModel::loadRecommendPeriods,
        onAdvertisementClick = homeViewModel::openAdvertisement,
    )
}
