package com.bigong.oguri.feature.strategy.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.navigation.MainNavigator
import com.bigong.oguri.core.navigation.RouteModel
import com.bigong.oguri.feature.strategy.ui.StrategyDetailRoute
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.strategy_detail_snackbar_pdf_pro
import org.jetbrains.compose.resources.stringResource

object StrategyNavGraph {
    fun register(
        navGraphBuilder: NavGraphBuilder,
        navigator: MainNavigator,
        appGraph: AppGraph,
        snackbarHostState: SnackbarHostState,
    ) {
        navGraphBuilder.composable<RouteModel.StrategyDetail> { navBackStackEntry ->
            val strategyDetailRoute: RouteModel.StrategyDetail = navBackStackEntry.toRoute<RouteModel.StrategyDetail>()
            StrategyNavEntry(
                strategyIdentifier = strategyDetailRoute.strategyIdentifier,
                navigator = navigator,
                appGraph = appGraph,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

@Composable
private fun StrategyNavEntry(
    strategyIdentifier: String,
    navigator: MainNavigator,
    appGraph: AppGraph,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()
    val pdfProMessage: String = stringResource(Res.string.strategy_detail_snackbar_pdf_pro)

    StrategyDetailRoute(
        annualLeaveStrategyRepository = appGraph.annualLeaveStrategyRepository,
        strategyIdentifier = strategyIdentifier,
        onCalendarClick = navigator::navigateToStrategyCalendar,
        onSavePdfProClick = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = pdfProMessage)
            }
        },
    )
}
