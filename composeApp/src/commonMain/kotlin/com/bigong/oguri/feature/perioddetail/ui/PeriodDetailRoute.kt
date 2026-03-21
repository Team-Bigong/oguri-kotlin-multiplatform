package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.bigong.oguri.core.analytics.OguriAnalyticsEvent
import com.bigong.oguri.core.analytics.OguriAnalyticsProperty
import com.bigong.oguri.core.analytics.trackOguriEvent
import com.bigong.oguri.core.deeplink.buildPeriodDetailDeepLink
import com.bigong.oguri.core.platform.SharePayload
import com.bigong.oguri.core.platform.shareContent
import dev.zacsweers.metro.Provider
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.share_button_open_in_app
import oguri.composeapp.generated.resources.share_default_fallback_message
import oguri.composeapp.generated.resources.share_default_fallback_url
import oguri.composeapp.generated.resources.share_period_description
import oguri.composeapp.generated.resources.share_title
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
    val pagedPlaces = periodDetailViewModel.pagedPlaces.collectAsLazyPagingItems()
    val shareTitle = stringResource(Res.string.share_title)
    val shareButtonTitle = stringResource(Res.string.share_button_open_in_app)
    val sharePeriodDescriptionTemplate = stringResource(Res.string.share_period_description)
    val shareFallbackMessage = stringResource(Res.string.share_default_fallback_message)
    val shareFallbackUrl = stringResource(Res.string.share_default_fallback_url)

    LaunchedEffect(startDate, endDate) {
        periodDetailViewModel.loadPeriodDetail(
            startDate = startDate,
            endDate = endDate,
        )
    }

    PeriodDetailScreen(
        periodDetailUiState = periodDetailUiState,
        pagedPlaces = pagedPlaces,
        onRetryClick = {
            periodDetailViewModel.loadPeriodDetail(
                startDate = startDate,
                endDate = endDate,
            )
        },
        onBackClick = onBackClick,
        onShareClick = {
            val periodDetail = periodDetailUiState.periodDetail ?: return@PeriodDetailScreen
            trackOguriEvent(
                eventName = OguriAnalyticsEvent.SHARE_CLICKED,
                eventProperties =
                    mapOf(
                        OguriAnalyticsProperty.SHARE_TYPE to "period",
                        OguriAnalyticsProperty.START_DATE to startDate,
                        OguriAnalyticsProperty.END_DATE to endDate,
                        OguriAnalyticsProperty.LEAVE_DAYS to periodDetail.dayOffCount.toString(),
                    ),
            )
            val deepLinkUrl =
                buildPeriodDetailDeepLink(
                    startDate = startDate,
                    endDate = endDate,
                )
            val sharePeriodDescription =
                sharePeriodDescriptionTemplate
                    .replace("%1\$d", periodDetail.dayOffCount.toString())
                    .replace("%2\$d", periodDetail.totalTripCount.toString())
            shareContent(
                payload =
                    SharePayload(
                        title = shareTitle,
                        description = sharePeriodDescription,
                        imageUrl =
                            periodDetail.places
                                .firstOrNull()
                                ?.thumbnailUrl
                                .orEmpty(),
                        deepLinkUrl = deepLinkUrl,
                        buttonTitle = shareButtonTitle,
                        fallbackMessage = shareFallbackMessage,
                        fallbackUrl = shareFallbackUrl,
                    ),
            )
        },
        onSaveToggleClick = periodDetailViewModel::toggleSavedRecommendation,
        onPlaceClick = onPlaceClick,
    )
}
