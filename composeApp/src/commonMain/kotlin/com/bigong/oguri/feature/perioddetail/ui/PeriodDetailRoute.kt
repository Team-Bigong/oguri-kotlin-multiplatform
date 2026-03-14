package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.runtime.Composable

@Composable
fun PeriodDetailRoute(
    periodId: Long,
    onBackClick: () -> Unit,
) {
    PeriodDetailScreen(
        periodId = periodId,
        onBackClick = onBackClick,
    )
}
