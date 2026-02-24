package com.bigong.oguri.feature.mypage.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.navigation.MainNavigator
import com.bigong.oguri.core.navigation.RouteModel
import com.bigong.oguri.feature.mypage.ui.MyPageRoute
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.mypage_snackbar_dummy_action
import oguri.composeapp.generated.resources.mypage_snackbar_logout
import oguri.composeapp.generated.resources.mypage_snackbar_pro_toggled_off
import oguri.composeapp.generated.resources.mypage_snackbar_pro_toggled_on
import org.jetbrains.compose.resources.stringResource

object MyPageNavGraph {
    fun register(
        navGraphBuilder: NavGraphBuilder,
        navigator: MainNavigator,
        appGraph: AppGraph,
        snackbarHostState: SnackbarHostState,
    ) {
        navGraphBuilder.composable<RouteModel.MyPage> {
            MyPageNavEntry(
                navigator = navigator,
                appGraph = appGraph,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

@Composable
private fun MyPageNavEntry(
    navigator: MainNavigator,
    appGraph: AppGraph,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()
    val dummyActionMessage: String = stringResource(Res.string.mypage_snackbar_dummy_action)
    val proEnabledMessage: String = stringResource(Res.string.mypage_snackbar_pro_toggled_on)
    val proDisabledMessage: String = stringResource(Res.string.mypage_snackbar_pro_toggled_off)
    val logoutMessage: String = stringResource(Res.string.mypage_snackbar_logout)

    MyPageRoute(
        userStateRepository = appGraph.userStateRepository,
        onSupportInquiryClick = navigator::navigateToSupportInquiryType,
        onShowDummySnackbar = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = dummyActionMessage)
            }
        },
        onShowProToggleSnackbar = { isEnabled: Boolean ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = if (isEnabled) proEnabledMessage else proDisabledMessage)
            }
        },
        onShowLogoutSnackbar = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = logoutMessage)
            }
            navigator.navigateToLogin()
        },
    )
}
