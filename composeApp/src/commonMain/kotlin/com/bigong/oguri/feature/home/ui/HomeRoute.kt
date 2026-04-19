package com.bigong.oguri.feature.home.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.analytics.OguriAnalyticsEvent
import com.bigong.oguri.core.analytics.OguriAnalyticsProperty
import com.bigong.oguri.core.analytics.trackOguriEvent
import com.bigong.oguri.core.ui.component.LoginRequiredDialog
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.PreloadNetworkImages
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.home.ui.model.HomeSideEffect
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_home_deleted
import oguri.composeapp.generated.resources.snackbar_home_saved
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState,
    scrollToTopTrigger: Int,
    onPlaceClick: (Long, String?, String?) -> Unit,
    onPeriodClick: (String, String) -> Unit,
    onMoveToCalendarClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onLoginRequired: () -> Unit,
) {
    val homeUiState = homeViewModel.uiState.collectAsStateWithLifecycle().value
    val recommendationSavedMessage = stringResource(Res.string.snackbar_home_saved)
    val recommendationDeletedMessage = stringResource(Res.string.snackbar_home_deleted)
    val uriHandler = LocalUriHandler.current
    var isLoginRequiredDialogVisible by remember { mutableStateOf(false) }
    val currentPeriodPlacesImageUrls =
        homeUiState.recommendPeriods
            .firstOrNull { recommendPeriod ->
                recommendPeriod.rank == homeUiState.selectedRank
            }?.places
            ?.map { place ->
                place.thumbnailUrl
            }.orEmpty()

    PreloadNetworkImages(imageUrls = currentPeriodPlacesImageUrls)

    LaunchedEffect(homeViewModel) {
        homeViewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                HomeSideEffect.RecommendationSaved -> {
                    snackbarHostState.showOguriSnackbar(
                        message = recommendationSavedMessage,
                        type = OguriSnackBarType.SUCCESS,
                    )
                }

                HomeSideEffect.RecommendationDeleted -> {
                    snackbarHostState.showOguriSnackbar(
                        message = recommendationDeletedMessage,
                        type = OguriSnackBarType.INFO,
                    )
                }

                HomeSideEffect.LoginRequired -> {
                    isLoginRequiredDialogVisible = true
                }
            }
        }
    }
    HomeScreen(
        homeUiState = homeUiState,
        onRankSelected = { selectedRank ->
            trackOguriEvent(
                eventName = OguriAnalyticsEvent.HOME_RANK_TOGGLE_CLICKED,
                eventProperties =
                    mapOf(
                        OguriAnalyticsProperty.RANK to selectedRank.toString(),
                    ),
            )
            homeViewModel.selectRank(selectedRank)
        },
        onSavedChanged = homeViewModel::toggleSaved,
        onRetryClick = homeViewModel::loadRecommendPeriods,
        onAdvertisementClick = { advertisement, advertisementIndex ->
            trackOguriEvent(
                eventName = OguriAnalyticsEvent.HOME_ADVERTISEMENT_CLICKED,
                eventProperties =
                    mapOf(
                        OguriAnalyticsProperty.AD_INDEX to advertisementIndex.toString(),
                        OguriAnalyticsProperty.AD_PLATFORM to advertisement.platform.name,
                        OguriAnalyticsProperty.AD_URL to advertisement.url,
                        OguriAnalyticsProperty.RANK to homeUiState.selectedRank.toString(),
                    ),
            )
            uriHandler.openUri(advertisement.url)
        },
        onPlaceClick = onPlaceClick,
        onPeriodClick = onPeriodClick,
        onMoveToCalendarClick = onMoveToCalendarClick,
        onSearchClick = onSearchClick,
        scrollToTopTrigger = scrollToTopTrigger,
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
