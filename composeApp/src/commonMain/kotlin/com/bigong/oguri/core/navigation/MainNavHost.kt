package com.bigong.oguri.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.feature.calendar.ui.CalendarRoute
import com.bigong.oguri.feature.home.ui.HomeRoute
import com.bigong.oguri.feature.login.ui.LoginRoute
import com.bigong.oguri.feature.mypage.ui.MyPageRoute
import com.bigong.oguri.feature.placedetail.ui.PlaceDetailRoute
import com.bigong.oguri.feature.splash.ui.SplashRoute

private const val NAVIGATION_FADE_DURATION_MILLIS: Int = 180

@Composable
fun MainNavHost(
    appGraph: AppGraph,
    navigator: MainNavigator,
    contentPaddingValues: PaddingValues,
) {
    NavHost(
        navController = navigator.navHostController,
        startDestination = RouteModel.Splash,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(contentPaddingValues),
        enterTransition = { fadeIn(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS)) },
        exitTransition = { fadeOut(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS)) },
        popExitTransition = { fadeOut(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS)) },
    ) {
        composable<RouteModel.Splash> {
            SplashRoute(onSplashCompleted = navigator::navigateToLogin)
        }
        composable<RouteModel.Login> {
            LoginRoute(
                onKakaoLoginClick = navigator::navigateToHomeFromLogin,
                onAppleLoginClick = navigator::navigateToHomeFromLogin,
                onGuestBrowseClick = navigator::navigateToHomeFromLogin,
            )
        }
        composable<RouteModel.Home> {
            HomeRoute(
                homeViewModelProvider = appGraph.homeViewModelProvider,
                onPlaceClick = navigator::navigateToPlaceDetail,
            )
        }
        composable<RouteModel.Calendar> {
            CalendarRoute()
        }
        composable<RouteModel.MyPage> {
            MyPageRoute()
        }
        composable<RouteModel.PlaceDetail> { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RouteModel.PlaceDetail>()
            PlaceDetailRoute(
                placeDetailViewModelProvider = appGraph.placeDetailViewModelProvider,
                placeId = route.placeId,
                onBackClick = { navigator.popBackStack() },
                onPlaceClick = navigator::navigateToPlaceDetail,
            )
        }
    }
}
