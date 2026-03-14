package com.bigong.oguri.feature.home.ui.model

import com.bigong.oguri.domain.model.RecommendPeriod

data class HomeUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val selectedRank: Int = 1,
    val savedRankSet: Set<Int> = emptySet(),
    val recommendPeriods: List<RecommendPeriod> = emptyList(),
)
