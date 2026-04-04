package com.bigong.oguri.feature.perioddetail.ui.model

sealed interface PeriodDetailSideEffect {
    data object RecommendationSaved : PeriodDetailSideEffect

    data object RecommendationDeleted : PeriodDetailSideEffect

    data object LoginRequired : PeriodDetailSideEffect
}
