package com.bigong.oguri.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Stable
class MainNavigator(
    val navHostController: NavHostController,
) {
    @Composable
    fun currentDestination(): NavDestination? {
        val navBackStackEntryState = navHostController.currentBackStackEntryAsState()
        return navBackStackEntryState.value?.destination
    }

    fun navigateToLogin() {
        navHostController.navigate(RouteModel.Login)
    }

    fun navigateToOnboarding() {
        navHostController.navigate(RouteModel.Onboarding)
    }

    fun navigateToStrategyDetail(
        strategyIdentifier: String,
    ) {
        navHostController.navigate(RouteModel.StrategyDetail(strategyIdentifier = strategyIdentifier))
    }

    fun navigateToStrategyCalendar() {
        navHostController.navigate(RouteModel.StrategyCalendar)
    }

    fun navigateToSupportInquiryType() {
        navHostController.navigate(RouteModel.SupportInquiryType)
    }

    fun navigateToBottomNavigationDestination(
        destination: BottomNavigationDestination,
    ) {
        navigateToMainTabRoot(destination.routeModel)
    }

    fun popBackStack(): Boolean {
        return navHostController.popBackStack()
    }

    private fun navigateToMainTabRoot(
        routeModel: RouteModel,
    ) {
        navHostController.navigate(routeModel) {
            launchSingleTop = true
            restoreState = false
            popUpTo(navHostController.graph.id) {
                inclusive = false
                saveState = false
            }
        }
    }
}

@Composable
fun rememberMainNavigator(): MainNavigator {
    val navHostController: NavHostController = rememberNavController()
    return remember(navHostController) {
        MainNavigator(navHostController = navHostController)
    }
}
