package com.bigong.oguri.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalUriHandler
import dev.zacsweers.metro.Provider

@Composable
fun HomeRoute(
    homeViewModelProvider: Provider<HomeViewModel>,
    onPlaceClick: (Long, String?, String?) -> Unit,
    onPeriodClick: (String, String) -> Unit,
) {
    val homeViewModel: HomeViewModel = remember {
        homeViewModelProvider()
    }
    val homeUiState = homeViewModel.uiState.collectAsStateWithLifecycle().value
    val uriHandler = LocalUriHandler.current

    HomeScreen(
        homeUiState = homeUiState,
        onRankSelected = homeViewModel::selectRank,
        onSavedChanged = homeViewModel::toggleSaved,
        onRetryClick = homeViewModel::loadRecommendPeriods,
        onAdvertisementClick = { destinationUrl: String ->
            uriHandler.openUri(destinationUrl)
        },
        onPlaceClick = onPlaceClick,
        onPeriodClick = onPeriodClick,
    )
}
