package com.bigong.oguri.feature.placedetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import dev.zacsweers.metro.Provider

@Composable
fun PlaceDetailRoute(
    placeDetailViewModelProvider: Provider<PlaceDetailViewModel>,
    placeId: Long,
    startDate: String?,
    endDate: String?,
    onBackClick: () -> Unit,
    onPlaceClick: (Long) -> Unit,
) {
    val placeDetailViewModel = remember {
        placeDetailViewModelProvider()
    }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(placeId, startDate, endDate) {
        placeDetailViewModel.loadPlaceDetail(
            placeId = placeId,
            startDate = startDate,
            endDate = endDate,
        )
    }

    PlaceDetailScreen(
        placeDetailUiState = placeDetailViewModel.placeDetailUiState,
        onBackClick = onBackClick,
        onRetryClick = {
            placeDetailViewModel.loadPlaceDetail(
                placeId = placeId,
                startDate = startDate,
                endDate = endDate,
            )
        },
        onShareClick = {},
        onSaveToggleClick = placeDetailViewModel::toggleSaved,
        onUrlClick = { destinationUrl: String ->
            uriHandler.openUri(destinationUrl)
        },
        onPlaceClick = onPlaceClick,
    )
}
