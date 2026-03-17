package com.bigong.oguri.feature.placedetail.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.deeplink.buildPlaceDetailDeepLink
import com.bigong.oguri.core.platform.shareText
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.placedetail.ui.model.PlaceDetailSideEffect
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_place_deleted
import oguri.composeapp.generated.resources.snackbar_place_saved
import oguri.composeapp.generated.resources.share_place_detail_message
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlaceDetailRoute(
    placeDetailViewModelProvider: Provider<PlaceDetailViewModel>,
    snackbarHostState: SnackbarHostState,
    placeId: Long,
    startDate: String?,
    endDate: String?,
    onBackClick: () -> Unit,
    onPlaceClick: (Long) -> Unit,
) {
    val placeDetailViewModel =
        remember {
            placeDetailViewModelProvider()
        }
    val placeDetailUiState = placeDetailViewModel.uiState.collectAsStateWithLifecycle().value
    val placeSavedMessage = stringResource(Res.string.snackbar_place_saved)
    val placeDeletedMessage = stringResource(Res.string.snackbar_place_deleted)
    val sharePlaceDetailMessageTemplate = stringResource(Res.string.share_place_detail_message)
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(placeId, startDate, endDate) {
        placeDetailViewModel.loadPlaceDetail(
            placeId = placeId,
            startDate = startDate,
            endDate = endDate,
        )
    }
    LaunchedEffect(placeDetailViewModel) {
        placeDetailViewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                PlaceDetailSideEffect.Saved -> {
                    snackbarHostState.showOguriSnackbar(
                        message = placeSavedMessage,
                        type = OguriSnackBarType.SUCCESS,
                    )
                }
                PlaceDetailSideEffect.Deleted -> {
                    snackbarHostState.showOguriSnackbar(
                        message = placeDeletedMessage,
                        type = OguriSnackBarType.INFO,
                    )
                }
            }
        }
    }

    PlaceDetailScreen(
        placeDetailUiState = placeDetailUiState,
        onBackClick = onBackClick,
        onRetryClick = {
            placeDetailViewModel.loadPlaceDetail(
                placeId = placeId,
                startDate = startDate,
                endDate = endDate,
            )
        },
        onShareClick = {
            val placeName = placeDetailUiState.placeDetail?.city ?: return@PlaceDetailScreen
            val deepLinkUrl =
                buildPlaceDetailDeepLink(
                    placeId = placeId,
                    startDate = startDate,
                    endDate = endDate,
                )
            val shareMessage =
                sharePlaceDetailMessageTemplate
                    .replace("%1\$s", placeName)
                    .replace("%2\$s", deepLinkUrl)
            shareText(shareMessage)
        },
        onSaveToggleClick = placeDetailViewModel::toggleSaved,
        onUrlClick = { destinationUrl ->
            uriHandler.openUri(destinationUrl)
        },
        onPlaceClick = onPlaceClick,
    )
}
