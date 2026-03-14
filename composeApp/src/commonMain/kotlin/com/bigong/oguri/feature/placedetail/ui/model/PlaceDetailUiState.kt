package com.bigong.oguri.feature.placedetail.ui.model

import com.bigong.oguri.domain.model.PlaceDetail

data class PlaceDetailUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val placeDetail: PlaceDetail? = null,
    val isSaved: Boolean = false,
)
