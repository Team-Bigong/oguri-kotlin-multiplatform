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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.feature.calendar.ui.CalendarRoute
import com.bigong.oguri.feature.home.ui.HomeRoute
import com.bigong.oguri.feature.login.ui.LoginRoute
import com.bigong.oguri.feature.mypage.ui.MyPageRoute
import com.bigong.oguri.feature.perioddetail.ui.PeriodDetailRoute
import com.bigong.oguri.feature.placedetail.ui.PlaceDetailRoute
import com.bigong.oguri.feature.splash.ui.SplashRoute
import com.bigong.oguri.feature.webdocument.ui.WebDocumentRoute

private const val NAVIGATION_FADE_DURATION_MILLIS = 180
private const val NAVIGATION_SLIDE_DURATION_MILLIS = 240

@Composable
fun MainNavHost(
    appGraph: AppGraph,
    navigator: MainNavigator,
    snackbarHostState: SnackbarHostState,
    contentPaddingValues: PaddingValues,
    onLoggedOut: () -> Unit,
    onLoginCompleted: () -> Unit,
) {
    val homeViewModel =
        remember {
            appGraph.homeViewModelProvider()
        }
    val calendarViewModel =
        remember {
            appGraph.calendarViewModelProvider()
        }
    val myPageViewModel =
        remember {
            appGraph.myPageViewModelProvider()
        }

    NavHost(
        navController = navigator.navHostController,
        startDestination = RouteModel.Splash,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(contentPaddingValues),
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                initialOffsetX = { fullWidth -> fullWidth },
            ) + fadeIn(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                targetOffsetX = { fullWidth -> -fullWidth / 3 },
            ) + fadeOut(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
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
            SplashRoute(onSplashCompleted = navigator::navigateToLogin)
        }
        composable<RouteModel.Login> {
            LoginRoute(
                loginViewModelProvider = appGraph.loginViewModelProvider,
                snackbarHostState = snackbarHostState,
                onLoginCompleted = onLoginCompleted,
                onAppleLoginClick = onLoginCompleted,
                onGuestBrowseClick = onLoginCompleted,
            )
        }
        composable<RouteModel.Home> {
            HomeRoute(
                homeViewModel = homeViewModel,
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
            )
        }
        composable<RouteModel.Calendar> {
            CalendarRoute(
                calendarViewModel = calendarViewModel,
                snackbarHostState = snackbarHostState,
                onOpenPeriodDetail = navigator::navigateToPeriodDetail,
            )
        }
        composable<RouteModel.MyPage> {
            MyPageRoute(
                myPageViewModel = myPageViewModel,
                snackbarHostState = snackbarHostState,
                onOpenSuggestion = { navigator.navigateToWebDocument(WebDocumentType.SUGGESTION) },
                onOpenTermsOfService = { navigator.navigateToWebDocument(WebDocumentType.TERMS_OF_SERVICE) },
                onOpenPrivacyPolicy = { navigator.navigateToWebDocument(WebDocumentType.PRIVACY_POLICY) },
                onLoggedOut = onLoggedOut,
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
