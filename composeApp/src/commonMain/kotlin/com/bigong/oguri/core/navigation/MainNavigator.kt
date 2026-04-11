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

    fun navigateToLoginAndClearBackStack() {
        navHostController.navigate(RouteModel.Login) {
            launchSingleTop = true
            restoreState = false
            popUpTo(navHostController.graph.id) {
                inclusive = true
                saveState = false
            }
        }
    }

    fun navigateToLoginFromSplash() {
        navHostController.navigate(RouteModel.Login) {
            launchSingleTop = true
            popUpTo(RouteModel.Splash) {
                inclusive = true
            }
        }
    }

    fun navigateToHomeFromSplash() {
        navHostController.navigate(RouteModel.Home) {
            launchSingleTop = true
            popUpTo(RouteModel.Splash) {
                inclusive = true
            }
        }
    }

    fun navigateToOnboardingFromSplash() {
        navHostController.navigate(RouteModel.Onboarding) {
            launchSingleTop = true
            popUpTo(RouteModel.Splash) {
                inclusive = true
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

    fun navigateToOnboardingFromLogin() {
        navHostController.navigate(RouteModel.Onboarding) {
            launchSingleTop = true
            popUpTo(RouteModel.Login) {
                inclusive = true
            }
        }
    }

    fun navigateToHomeFromOnboarding() {
        navHostController.navigate(RouteModel.Home) {
            launchSingleTop = true
            popUpTo(RouteModel.Onboarding) {
                inclusive = true
            }
        }
    }

    fun navigateToPlaceDetail(
        placeId: Long,
        startDate: String?,
        endDate: String?,
    ) {
        navHostController.navigate(
            RouteModel.PlaceDetail(
                placeId = placeId,
                startDate = startDate,
                endDate = endDate,
            ),
        )
    }

    fun navigateToPeriodDetail(
        startDate: String,
        endDate: String,
    ) {
        navHostController.navigate(
            RouteModel.PeriodDetail(
                startDate = startDate,
                endDate = endDate,
            ),
        )
    }

    fun navigateToPhotoDetail(
        imageUrls: List<String>,
        initialPage: Int,
    ) {
        navHostController.navigate(
            RouteModel.PhotoDetail(
                imageUrls = imageUrls,
                initialPage = initialPage,
            ),
        )
    }

    fun navigateToWebDocument(documentType: WebDocumentType) {
        navHostController.navigate(RouteModel.WebDocument(documentType = documentType.name))
    }

    fun navigateToDisplaySettings() {
        navHostController.navigate(RouteModel.DisplaySettings)
    }

    fun navigateToRouteModel(routeModel: RouteModel) {
        when (routeModel) {
            is RouteModel.PlaceDetail -> {
                navigateToPlaceDetail(
                    placeId = routeModel.placeId,
                    startDate = routeModel.startDate,
                    endDate = routeModel.endDate,
                )
            }

            is RouteModel.PeriodDetail -> {
                navigateToPeriodDetail(
                    startDate = routeModel.startDate,
                    endDate = routeModel.endDate,
                )
            }

            is RouteModel.PhotoDetail -> {
                navigateToPhotoDetail(
                    imageUrls = routeModel.imageUrls,
                    initialPage = routeModel.initialPage,
                )
            }

            is RouteModel.WebDocument -> {
                navHostController.navigate(routeModel)
            }

            RouteModel.Onboarding,
            RouteModel.Home,
            RouteModel.Calendar,
            RouteModel.MyPage,
            RouteModel.DisplaySettings,
            RouteModel.Login,
            RouteModel.Splash,
            -> {
                navHostController.navigate(routeModel)
            }
        }
    }

    fun popBackStack(): Boolean = navHostController.popBackStack()
}

@Composable
fun rememberMainNavigator(): MainNavigator {
    val navHostController = rememberNavController()
    return remember(navHostController) {
        MainNavigator(navHostController = navHostController)
    }
}
