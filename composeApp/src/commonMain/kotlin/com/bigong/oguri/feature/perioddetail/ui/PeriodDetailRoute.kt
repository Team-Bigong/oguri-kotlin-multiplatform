package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

    LaunchedEffect(startDate, endDate) {
        periodDetailViewModel.loadPeriodDetail(
            startDate = startDate,
            endDate = endDate,
        )
    }

    PeriodDetailScreen(
        periodDetailUiState = periodDetailViewModel.periodDetailUiState,
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
