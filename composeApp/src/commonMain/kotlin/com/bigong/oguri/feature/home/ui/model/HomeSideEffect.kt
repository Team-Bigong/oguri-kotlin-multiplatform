package com.bigong.oguri.feature.home.ui.model

sealed interface HomeSideEffect {
    data object RecommendationSaved : HomeSideEffect

    data object RecommendationDeleted : HomeSideEffect
}
