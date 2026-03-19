package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.deeplink.buildPeriodDetailDeepLink
import com.bigong.oguri.core.platform.shareText
import dev.zacsweers.metro.Provider
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.share_period_detail_message
import org.jetbrains.compose.resources.stringResource

@Composable
fun PeriodDetailRoute(
    periodDetailViewModelProvider: Provider<PeriodDetailViewModel>,
    startDate: String,
    endDate: String,
    onBackClick: () -> Unit,
    onPlaceClick: (Long) -> Unit,
) {
    val periodDetailViewModel =
        remember {
            periodDetailViewModelProvider()
        }
    val periodDetailUiState = periodDetailViewModel.uiState.collectAsStateWithLifecycle().value
    val sharePeriodDetailMessageTemplate = stringResource(Res.string.share_period_detail_message)

    LaunchedEffect(startDate, endDate) {
        periodDetailViewModel.loadPeriodDetail(
            startDate = startDate,
            endDate = endDate,
        )
    }

    PeriodDetailScreen(
        periodDetailUiState = periodDetailUiState,
        onRetryClick = {
            periodDetailViewModel.loadPeriodDetail(
                startDate = startDate,
                endDate = endDate,
            )
        },
        onBackClick = onBackClick,
        onShareClick = {
            val deepLinkUrl =
                buildPeriodDetailDeepLink(
                    startDate = startDate,
                    endDate = endDate,
                )
            val shareMessage =
                sharePeriodDetailMessageTemplate
                    .replace("%1\$s", startDate)
                    .replace("%2\$s", endDate)
                    .replace("%3\$s", deepLinkUrl)
            shareText(shareMessage)
        },
        onSaveToggleClick = periodDetailViewModel::toggleSavedRecommendation,
        onPlaceClick = onPlaceClick,
    )
}
