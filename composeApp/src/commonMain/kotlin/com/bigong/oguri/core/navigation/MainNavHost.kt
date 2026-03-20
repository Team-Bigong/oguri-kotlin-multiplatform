package com.bigong.oguri.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.feature.calendar.ui.CalendarRoute
import com.bigong.oguri.feature.calendar.ui.CalendarViewModel
import com.bigong.oguri.feature.home.ui.HomeRoute
import com.bigong.oguri.feature.home.ui.HomeViewModel
import com.bigong.oguri.feature.login.ui.LoginRoute
import com.bigong.oguri.feature.mypage.ui.MyPageRoute
import com.bigong.oguri.feature.mypage.ui.MyPageViewModel
import com.bigong.oguri.feature.onboarding.ui.OnboardingRoute
import com.bigong.oguri.feature.perioddetail.ui.PeriodDetailRoute
import com.bigong.oguri.feature.photodetail.ui.PhotoDetailRoute
import com.bigong.oguri.feature.placedetail.ui.PlaceDetailRoute
import com.bigong.oguri.feature.splash.ui.SplashRoute
import com.bigong.oguri.feature.webdocument.ui.WebDocumentRoute

private const val NAVIGATION_FADE_DURATION_MILLIS = 180
private const val NAVIGATION_SLIDE_DURATION_MILLIS = 240

@Composable
fun MainNavHost(
    appGraph: AppGraph,
    homeViewModelProvider: () -> HomeViewModel,
    calendarViewModelProvider: () -> CalendarViewModel,
    myPageViewModelProvider: () -> MyPageViewModel,
    navigator: MainNavigator,
    snackbarHostState: SnackbarHostState,
    contentPaddingValues: PaddingValues,
    onLoggedOut: () -> Unit,
    onWithdrawCompleted: () -> Unit,
) {
    NavHost(
        navController = navigator.navHostController,
        startDestination = RouteModel.Splash,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(contentPaddingValues),
        enterTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route
            val initialTabIndex = bottomNavigationTabIndex(routeText = initialRoute)
            val targetTabIndex = bottomNavigationTabIndex(routeText = targetRoute)
            val isBottomNavigationTabTransition = initialTabIndex != null && targetTabIndex != null

            if (isBottomNavigationTabTransition) {
                val isForward = targetTabIndex > initialTabIndex
                slideInHorizontally(
                    animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                    initialOffsetX = { fullWidth -> if (isForward) fullWidth else -fullWidth },
                ) + fadeIn(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
            } else {
                slideInHorizontally(
                    animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                    initialOffsetX = { fullWidth -> fullWidth },
                ) + fadeIn(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
            }
        },
        exitTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route
            val initialTabIndex = bottomNavigationTabIndex(routeText = initialRoute)
            val targetTabIndex = bottomNavigationTabIndex(routeText = targetRoute)
            val isBottomNavigationTabTransition = initialTabIndex != null && targetTabIndex != null

            if (isBottomNavigationTabTransition) {
                val isForward = targetTabIndex > initialTabIndex
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                    targetOffsetX = { fullWidth -> if (isForward) -fullWidth else fullWidth },
                ) + fadeOut(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
            } else {
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                ) + fadeOut(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
            }
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
            ) + fadeIn(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                targetOffsetX = { fullWidth -> fullWidth },
            ) + fadeOut(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
        },
    ) {
        composable<RouteModel.Splash> {
            SplashRoute(
                splashViewModelProvider = appGraph.splashViewModelProvider,
                onNavigateToLogin = navigator::navigateToLoginFromSplash,
                onNavigateToOnboarding = navigator::navigateToOnboardingFromSplash,
                onNavigateToHome = navigator::navigateToHomeFromSplash,
            )
        }
        composable<RouteModel.Login> {
            LoginRoute(
                loginViewModelProvider = appGraph.loginViewModelProvider,
                snackbarHostState = snackbarHostState,
                onLoginCompleted = { isOnboardingCompleted ->
                    if (isOnboardingCompleted) {
                        navigator.navigateToHomeFromLogin()
                    } else {
                        navigator.navigateToOnboardingFromLogin()
                    }
                },
                onGuestBrowseClick = navigator::navigateToHomeFromLogin,
            )
        }
        composable<RouteModel.Onboarding> {
            OnboardingRoute(
                onboardingViewModelProvider = appGraph.onboardingViewModelProvider,
                snackbarHostState = snackbarHostState,
                onBackClick = navigator::navigateToLogin,
                onHomeClick = navigator::navigateToHomeFromOnboarding,
                onOpenWebDocument = navigator::navigateToWebDocument,
            )
        }
        composable<RouteModel.Home> {
            HomeRoute(
                homeViewModel = homeViewModelProvider(),
                snackbarHostState = snackbarHostState,
                onPlaceClick = navigator::navigateToPlaceDetail,
                onPeriodClick = navigator::navigateToPeriodDetail,
                onMoveToCalendarClick = {
                    val calendarDestination =
                        bottomNavigationDestinations.first { destination ->
                            destination.routeModel == RouteModel.Calendar
                        }
                    navigator.navigateToBottomNavigationDestination(calendarDestination)
                },
                onLoginRequired = navigator::navigateToLogin,
            )
        }
        composable<RouteModel.Calendar> {
            CalendarRoute(
                calendarViewModel = calendarViewModelProvider(),
                snackbarHostState = snackbarHostState,
                onOpenPeriodDetail = navigator::navigateToPeriodDetail,
            )
        }
        composable<RouteModel.MyPage> {
            MyPageRoute(
                myPageViewModel = myPageViewModelProvider(),
                snackbarHostState = snackbarHostState,
                onOpenSuggestion = { navigator.navigateToWebDocument(WebDocumentType.SUGGESTION) },
                onOpenTermsOfService = { navigator.navigateToWebDocument(WebDocumentType.TERMS_OF_SERVICE) },
                onOpenPrivacyPolicy = { navigator.navigateToWebDocument(WebDocumentType.PRIVACY_POLICY) },
                onLoggedOut = onLoggedOut,
                onWithdrawCompleted = onWithdrawCompleted,
                onLoginRequired = navigator::navigateToLogin,
                onPeriodClick = navigator::navigateToPeriodDetail,
                onSavedPlaceClick = { placeId ->
                    navigator.navigateToPlaceDetail(
                        placeId = placeId,
                        startDate = null,
                        endDate = null,
                    )
                },
            )
        }
        composable<RouteModel.PlaceDetail> { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RouteModel.PlaceDetail>()
            PlaceDetailRoute(
                placeDetailViewModelProvider = appGraph.placeDetailViewModelProvider,
                snackbarHostState = snackbarHostState,
                placeId = route.placeId,
                startDate = route.startDate,
                endDate = route.endDate,
                onBackClick = { navigator.popBackStack() },
                onPlaceClick = { targetPlaceId ->
                    navigator.navigateToPlaceDetail(
                        placeId = targetPlaceId,
                        startDate = route.startDate,
                        endDate = route.endDate,
                    )
                },
                onPhotoClick = { imageUrls, initialPage ->
                    navigator.navigateToPhotoDetail(
                        imageUrls = imageUrls,
                        initialPage = initialPage,
                    )
                },
            )
        }
        composable<RouteModel.PhotoDetail> { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RouteModel.PhotoDetail>()
            PhotoDetailRoute(
                imageUrls = route.imageUrls,
                initialPage = route.initialPage,
                onBackClick = { navigator.popBackStack() },
            )
        }
        composable<RouteModel.PeriodDetail> { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RouteModel.PeriodDetail>()
            PeriodDetailRoute(
                periodDetailViewModelProvider = appGraph.periodDetailViewModelProvider,
                startDate = route.startDate,
                endDate = route.endDate,
                onBackClick = { navigator.popBackStack() },
                onPlaceClick = { placeId ->
                    navigator.navigateToPlaceDetail(
                        placeId = placeId,
                        startDate = route.startDate,
                        endDate = route.endDate,
                    )
                },
            )
        }
        composable<RouteModel.WebDocument> { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RouteModel.WebDocument>()
            WebDocumentRoute(
                documentType = route.documentType,
                onBackClick = { navigator.popBackStack() },
            )
        }
    }
}

private fun bottomNavigationTabIndex(routeText: String?): Int? {
    if (routeText.isNullOrBlank()) {
        return null
    }
    val index =
        bottomNavigationDestinations.indexOfFirst { destination ->
            routeText == destination.routeSerialName || routeText.startsWith(destination.routeSerialName)
        }
    return if (index >= 0) index else null
}
