package com.bigong.oguri.feature.strategy.ui.model

import com.bigong.oguri.data.model.StrategyDetailData

data class StrategyDetailUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val strategyDetailData: StrategyDetailData? = null,
)
