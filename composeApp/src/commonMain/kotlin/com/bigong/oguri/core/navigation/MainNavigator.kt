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

    fun navigateToBottomNavigationDestination(destination: BottomNavigationDestination) {
        navHostController.navigate(destination.routeModel) {
            launchSingleTop = true
            restoreState = false
            popUpTo(navHostController.graph.id) {
                inclusive = false
                saveState = false
            }
        }
    }

    fun navigateToLogin() {
        navHostController.navigate(RouteModel.Login) {
            launchSingleTop = true
            popUpTo(navHostController.graph.id) {
                inclusive = false
            }
        }
    }

    fun navigateToHomeFromLogin() {
        navHostController.navigate(RouteModel.Home) {
            launchSingleTop = true
            popUpTo(RouteModel.Login) {
                inclusive = true
            }
        }
    }

    fun navigateToPlaceDetail(placeId: Long) {
        navHostController.navigate(RouteModel.PlaceDetail(placeId = placeId))
    }

    fun navigateToPeriodDetail(periodId: Long) {
        navHostController.navigate(RouteModel.PeriodDetail(periodId = periodId))
    }

    fun popBackStack(): Boolean {
        return navHostController.popBackStack()
    }
}

@Composable
fun rememberMainNavigator(): MainNavigator {
    val navHostController: NavHostController = rememberNavController()
    return remember(navHostController) {
        MainNavigator(navHostController = navHostController)
    }
}
