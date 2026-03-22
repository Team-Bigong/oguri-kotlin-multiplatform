package com.bigong.oguri.feature.placedetail.ui.model

sealed interface PlaceDetailSideEffect {
    data object PlaceSaved : PlaceDetailSideEffect

    data object PlaceDeleted : PlaceDetailSideEffect

    data object LoginRequired : PlaceDetailSideEffect
}
