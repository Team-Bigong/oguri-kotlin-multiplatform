package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.bigong.oguri.core.analytics.OguriAnalyticsEvent
import com.bigong.oguri.core.analytics.OguriAnalyticsProperty
import com.bigong.oguri.core.analytics.trackOguriEvent
import com.bigong.oguri.core.deeplink.buildPeriodDetailDeepLink
import com.bigong.oguri.core.platform.SharePayload
import com.bigong.oguri.core.platform.shareContent
import com.bigong.oguri.core.ui.component.LoginRequiredDialog
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.perioddetail.ui.model.PeriodDetailSideEffect
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.share_button_open_in_app
import oguri.composeapp.generated.resources.share_default_fallback_message
import oguri.composeapp.generated.resources.share_default_fallback_url
import oguri.composeapp.generated.resources.share_period_description
import oguri.composeapp.generated.resources.share_title
import oguri.composeapp.generated.resources.snackbar_home_deleted
import oguri.composeapp.generated.resources.snackbar_home_saved
import org.jetbrains.compose.resources.stringResource

@Composable
fun PeriodDetailRoute(
    periodDetailViewModelProvider: Provider<PeriodDetailViewModel>,
    snackbarHostState: SnackbarHostState,
    startDate: String,
    endDate: String,
    onBackClick: () -> Unit,
    onLoginRequired: () -> Unit,
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
    val recommendationSavedMessage = stringResource(Res.string.snackbar_home_saved)
    val recommendationDeletedMessage = stringResource(Res.string.snackbar_home_deleted)
    var isLoginRequiredDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(startDate, endDate) {
        periodDetailViewModel.loadPeriodDetail(
            startDate = startDate,
            endDate = endDate,
        )
    }
    LaunchedEffect(periodDetailViewModel) {
        periodDetailViewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                PeriodDetailSideEffect.RecommendationSaved -> {
                    snackbarHostState.showOguriSnackbar(
                        message = recommendationSavedMessage,
                        type = OguriSnackBarType.SUCCESS,
                    )
                }
                PeriodDetailSideEffect.RecommendationDeleted -> {
                    snackbarHostState.showOguriSnackbar(
                        message = recommendationDeletedMessage,
                        type = OguriSnackBarType.INFO,
                    )
                }
                PeriodDetailSideEffect.LoginRequired -> {
                    isLoginRequiredDialogVisible = true
                }
            }
        }
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

    if (isLoginRequiredDialogVisible) {
        LoginRequiredDialog(
            onDismissRequest = {
                isLoginRequiredDialogVisible = false
            },
            onLoginClick = {
                isLoginRequiredDialogVisible = false
                onLoginRequired()
            },
        )
    }
}
