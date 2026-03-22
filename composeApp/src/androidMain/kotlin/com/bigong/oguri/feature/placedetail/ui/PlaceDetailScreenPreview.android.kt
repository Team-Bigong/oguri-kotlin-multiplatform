package com.bigong.oguri.feature.placedetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.previewPlaceDetail
import com.bigong.oguri.feature.placedetail.ui.model.PlaceDetailUiState

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun PlaceDetailScreenPreview() {
    OguriTheme {
        PlaceDetailScreen(
            placeDetailUiState =
                PlaceDetailUiState(
                    isLoading = false,
                    isError = false,
                    placeDetail = previewPlaceDetail,
                    isSaved = false,
                ),
            onBackClick = {},
            onRetryClick = {},
            onShareClick = {},
            onSaveToggleClick = {},
            onExperienceClick = { _, _ -> },
            onFlightClick = {},
            onPlaceClick = {},
            onPhotoClick = { _, _ -> },
        )
    }
}
