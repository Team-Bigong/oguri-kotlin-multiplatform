package com.bigong.oguri.feature.home.ui.model

import com.bigong.oguri.data.model.HomeStrategyRecommendation
import com.bigong.oguri.data.model.UserState

data class HomeUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val userState: UserState = UserState(),
    val recommendation: HomeStrategyRecommendation? = null,
)
