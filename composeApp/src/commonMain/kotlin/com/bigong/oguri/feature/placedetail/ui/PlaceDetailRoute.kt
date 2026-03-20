package com.bigong.oguri.feature.placedetail.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.deeplink.buildPlaceDetailDeepLink
import com.bigong.oguri.core.platform.SharePayload
import com.bigong.oguri.core.platform.shareContent
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.placedetail.ui.model.PlaceDetailSideEffect
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.share_button_open_in_app
import oguri.composeapp.generated.resources.share_default_fallback_message
import oguri.composeapp.generated.resources.share_default_fallback_url
import oguri.composeapp.generated.resources.share_place_description
import oguri.composeapp.generated.resources.share_place_title
import oguri.composeapp.generated.resources.snackbar_place_deleted
import oguri.composeapp.generated.resources.snackbar_place_saved
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
    onPhotoClick: (List<String>, Int) -> Unit,
) {
    val placeDetailViewModel =
        remember {
            placeDetailViewModelProvider()
        }
    val placeDetailUiState = placeDetailViewModel.uiState.collectAsStateWithLifecycle().value
    val placeSavedMessage = stringResource(Res.string.snackbar_place_saved)
    val placeDeletedMessage = stringResource(Res.string.snackbar_place_deleted)
    val sharePlaceTitleTemplate = stringResource(Res.string.share_place_title)
    val sharePlaceDescription = stringResource(Res.string.share_place_description)
    val shareButtonTitle = stringResource(Res.string.share_button_open_in_app)
    val shareFallbackMessage = stringResource(Res.string.share_default_fallback_message)
    val shareFallbackUrl = stringResource(Res.string.share_default_fallback_url)
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
            val placeDetail = placeDetailUiState.placeDetail ?: return@PlaceDetailScreen
            val shareTitle = sharePlaceTitleTemplate.replace("%1\$s", placeDetail.city)
            val deepLinkUrl =
                buildPlaceDetailDeepLink(
                    placeId = placeId,
                    startDate = startDate,
                    endDate = endDate,
                )
            shareContent(
                payload =
                    SharePayload(
                        title = shareTitle,
                        description = sharePlaceDescription,
                        imageUrl = placeDetail.thumbnailUrls.firstOrNull().orEmpty(),
                        deepLinkUrl = deepLinkUrl,
                        buttonTitle = shareButtonTitle,
                        fallbackMessage = shareFallbackMessage,
                        fallbackUrl = shareFallbackUrl,
                    ),
            )
        },
        onSaveToggleClick = placeDetailViewModel::toggleSaved,
        onUrlClick = { destinationUrl ->
            uriHandler.openUri(destinationUrl)
        },
        onPlaceClick = onPlaceClick,
        onPhotoClick = onPhotoClick,
    )
}
