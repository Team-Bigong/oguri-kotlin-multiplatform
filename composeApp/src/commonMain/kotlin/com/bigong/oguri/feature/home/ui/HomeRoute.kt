package com.bigong.oguri.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import dev.zacsweers.metro.Provider

@Composable
fun HomeRoute(
    homeViewModelProvider: Provider<HomeViewModel>,
    onPlaceClick: (Long) -> Unit,
) {
    val homeViewModel: HomeViewModel = remember {
        homeViewModelProvider()
    }
    val uriHandler = LocalUriHandler.current

    HomeScreen(
        homeUiState = homeViewModel.homeUiState,
        onRankSelected = homeViewModel::selectRank,
        onSavedChanged = homeViewModel::toggleSaved,
        onRetryClick = homeViewModel::loadRecommendPeriods,
        onAdvertisementClick = { destinationUrl: String ->
            uriHandler.openUri(destinationUrl)
        },
        onPlaceClick = onPlaceClick,
    )
}
