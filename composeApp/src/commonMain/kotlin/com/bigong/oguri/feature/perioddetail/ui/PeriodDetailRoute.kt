package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metro.Provider

@Composable
fun PeriodDetailRoute(
    periodDetailViewModelProvider: Provider<PeriodDetailViewModel>,
    startDate: String,
    endDate: String,
    onBackClick: () -> Unit,
    onPlaceClick: (Long) -> Unit,
) {
    val periodDetailViewModel: PeriodDetailViewModel = remember {
        periodDetailViewModelProvider()
    }
    val periodDetailUiState = periodDetailViewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(startDate, endDate) {
        periodDetailViewModel.loadPeriodDetail(
            startDate = startDate,
            endDate = endDate,
        )
    }

    PeriodDetailScreen(
        periodDetailUiState = periodDetailUiState,
        onRetryClick = {
            periodDetailViewModel.loadPeriodDetail(
                startDate = startDate,
                endDate = endDate,
            )
        },
        onBackClick = onBackClick,
        onShareClick = {},
        onSaveToggleClick = periodDetailViewModel::toggleSavedRecommendation,
        onPlaceClick = onPlaceClick,
    )
}
