package com.bigong.oguri.feature.home.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.navigation.MainNavigator
import com.bigong.oguri.core.navigation.RouteModel
import com.bigong.oguri.feature.home.ui.HomeRoute
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_home_top_test
import org.jetbrains.compose.resources.stringResource

object HomeNavGraph {
    fun register(
        navGraphBuilder: NavGraphBuilder,
        navigator: MainNavigator,
        appGraph: AppGraph,
        snackbarHostState: SnackbarHostState,
    ) {
        navGraphBuilder.composable<RouteModel.Home> {
            HomeNavEntry(
                navigator = navigator,
                appGraph = appGraph,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

@Composable
private fun HomeNavEntry(
    navigator: MainNavigator,
    appGraph: AppGraph,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()
    val homeSnackbarMessage: String = stringResource(Res.string.snackbar_home_top_test)

    HomeRoute(
        annualLeaveStrategyRepository = appGraph.annualLeaveStrategyRepository,
        userMvpStateRepository = appGraph.userMvpStateRepository,
        onStrategyDetailClick = navigator::navigateToStrategyDetail,
        onShowSnackbarClick = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = homeSnackbarMessage)
            }
        },
    )
}
