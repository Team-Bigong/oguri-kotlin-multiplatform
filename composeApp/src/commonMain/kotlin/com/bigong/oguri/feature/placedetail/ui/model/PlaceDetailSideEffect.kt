package com.bigong.oguri.feature.placedetail.ui.model

sealed interface PlaceDetailSideEffect {
    data object Saved : PlaceDetailSideEffect

    data object Deleted : PlaceDetailSideEffect
}
