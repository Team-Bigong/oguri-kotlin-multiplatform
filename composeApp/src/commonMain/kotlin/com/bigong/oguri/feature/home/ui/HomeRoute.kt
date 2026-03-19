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
import com.bigong.oguri.core.ui.component.LoginRequiredDialog
import com.bigong.oguri.core.ui.component.OguriSnackBarType
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
    onPlaceClick: (Long, String?, String?) -> Unit,
    onPeriodClick: (String, String) -> Unit,
    onMoveToCalendarClick: () -> Unit,
    onLoginRequired: () -> Unit,
) {
    val homeUiState = homeViewModel.uiState.collectAsStateWithLifecycle().value
    val recommendationSavedMessage = stringResource(Res.string.snackbar_home_saved)
    val recommendationDeletedMessage = stringResource(Res.string.snackbar_home_deleted)
    val uriHandler = LocalUriHandler.current
    var isLoginRequiredDialogVisible by remember { mutableStateOf(false) }

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
    LaunchedEffect(Unit) {
        homeViewModel.refreshRecommendPeriods()
    }

    HomeScreen(
        homeUiState = homeUiState,
        onRankSelected = homeViewModel::selectRank,
        onSavedChanged = homeViewModel::toggleSaved,
        onRetryClick = homeViewModel::loadRecommendPeriods,
        onAdvertisementClick = { destinationUrl ->
            uriHandler.openUri(destinationUrl)
        },
        onPlaceClick = onPlaceClick,
        onPeriodClick = onPeriodClick,
        onMoveToCalendarClick = onMoveToCalendarClick,
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
