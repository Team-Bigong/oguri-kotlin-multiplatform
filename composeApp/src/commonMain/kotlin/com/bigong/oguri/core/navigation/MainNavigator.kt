package com.bigong.oguri.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

internal const val TRAVEL_DAY_REQUEST: String = "travel-day-request"

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

    fun navigateToSearch() {
        navHostController.navigate(RouteModel.Search) {
            launchSingleTop = true
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

    fun navigateToCreateTravelPlan(
        destinationId: Long? = null,
        startDate: String? = null,
        endDate: String? = null,
    ) {
        navHostController.navigate(RouteModel.CreateTravelPlan(destinationId, startDate, endDate)) { launchSingleTop = true }
    }

    fun navigateToTravelInvite() {
        navHostController.navigate(RouteModel.TravelInvite) { launchSingleTop = true }
    }

    fun navigateToTravelMembers(
        planId: String,
        startDate: String,
        endDate: String,
    ) {
        navHostController.navigate(RouteModel.TravelMembers(planId, startDate, endDate)) { launchSingleTop = true }
    }

    fun showTravelDay(dayNumber: Int) {
        navHostController.previousBackStackEntry?.savedStateHandle?.set(TRAVEL_DAY_REQUEST, dayNumber)
        popBackStack()
    }

    fun openJoinedTravelPlan(planId: String) {
        navHostController.navigate(RouteModel.TravelPlans) {
            popUpTo<RouteModel.TravelInvite> { inclusive = true }
            launchSingleTop = true
        }
        navigateToTravelPlan(planId)
    }

    fun closeManagedTravelPlan() {
        navHostController.navigate(RouteModel.TravelPlans) {
            popUpTo<RouteModel.TravelPlan> { inclusive = true }
            launchSingleTop = true
        }
    }

    fun navigateToTravelLogin() {
        navHostController.navigate(RouteModel.TravelLogin) { launchSingleTop = true }
    }

    fun navigateToTravelOnboarding() {
        navHostController.navigate(RouteModel.TravelOnboarding) {
            launchSingleTop = true
            popUpTo<RouteModel.TravelLogin> { inclusive = true }
        }
    }

    fun openCalendarTab() {
        navigateToBottomNavigationDestination(bottomNavigationDestinations.first { it.routeModel == RouteModel.Calendar })
    }

    fun navigateToTravelPlan(
        planId: String,
        fromCreation: Boolean = false,
    ) {
        navHostController.navigate(RouteModel.TravelPlan(planId)) {
            launchSingleTop = true
            if (fromCreation) popUpTo<RouteModel.CreateTravelPlan> { inclusive = true }
        }
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

            is RouteModel.WebDocument,
            is RouteModel.TravelPlan,
            is RouteModel.TravelMembers,
            is RouteModel.CreateTravelPlan,
            -> {
                navHostController.navigate(routeModel)
            }

            RouteModel.Onboarding,
            RouteModel.Home,
            RouteModel.Search,
            RouteModel.Calendar,
            RouteModel.MyPage,
            RouteModel.DisplaySettings,
            RouteModel.TravelPlans,
            RouteModel.TravelLogin,
            RouteModel.TravelInvite,
            RouteModel.TravelOnboarding,
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
