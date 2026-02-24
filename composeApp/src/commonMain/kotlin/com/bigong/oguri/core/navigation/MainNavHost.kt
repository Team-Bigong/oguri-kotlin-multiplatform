package com.bigong.oguri.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bigong.oguri.feature.calendar.ui.StrategyCalendarRoute
import com.bigong.oguri.feature.home.ui.HomeRoute
import com.bigong.oguri.feature.login.ui.LoginRoute
import com.bigong.oguri.feature.mypage.ui.MyPageRoute
import com.bigong.oguri.feature.onboarding.ui.OnboardingRoute
import com.bigong.oguri.feature.splash.ui.SplashRoute
import com.bigong.oguri.feature.strategy.ui.StrategyDetailRoute
import com.bigong.oguri.feature.support.ui.SupportInquiryTypeRoute
import kotlinx.coroutines.launch

private const val DefaultStrategyIdentifier: String = "2026-10-best"
private const val SupportInquiryFeatureSuggestionMessage: String = "기능 제안 메일 연결은 다음 단계에서 구현합니다."
private const val SupportInquiryBugReportMessage: String = "오류 신고 메일 연결은 다음 단계에서 구현합니다."
private const val SupportInquiryOtherMessage: String = "기타 문의 메일 연결은 다음 단계에서 구현합니다."

@Composable
fun MainNavHost(
    navHostController: NavHostController,
    contentPaddingValues: PaddingValues,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navHostController,
        startDestination = RouteModel.Splash.routePath,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPaddingValues),
    ) {
        composable(route = RouteModel.Splash.routePath) {
            SplashRoute(
                onStartClick = {
                    navHostController.navigate(RouteModel.Login.routePath) {
                        popUpTo(RouteModel.Splash.routePath) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(route = RouteModel.Login.routePath) {
            LoginRoute(
                onKakaoStartClick = {
                    navHostController.navigate(RouteModel.Onboarding.routePath)
                },
                onGuestBrowseClick = {
                    navHostController.navigate(RouteModel.Home.routePath) {
                        popUpTo(RouteModel.Login.routePath) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(route = RouteModel.Onboarding.routePath) {
            OnboardingRoute(
                onCalculateStrategyClick = {
                    navHostController.navigate(RouteModel.Home.routePath) {
                        popUpTo(RouteModel.Onboarding.routePath) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(route = RouteModel.Home.routePath) {
            HomeRoute(
                onStrategyDetailClick = {
                    navHostController.navigate(
                        RouteModel.StrategyDetail(
                            strategyIdentifier = DefaultStrategyIdentifier,
                        ).routePath,
                    )
                },
                onShowSnackbarClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = "홈에서 상단 스낵바 호출")
                    }
                },
            )
        }
        composable(
            route = RouteModel.StrategyDetail.routePattern,
            arguments = listOf(
                navArgument(RouteModel.StrategyDetail.strategyIdentifierArgument) {
                    type = NavType.StringType
                },
            ),
        ) { navBackStackEntry ->
            val strategyIdentifier: String =
                navBackStackEntry.arguments?.getString(RouteModel.StrategyDetail.strategyIdentifierArgument)
                    ?: DefaultStrategyIdentifier

            StrategyDetailRoute(
                strategyIdentifier = strategyIdentifier,
                onCalendarClick = {
                    navHostController.navigate(RouteModel.StrategyCalendar.routePath)
                },
            )
        }
        composable(route = RouteModel.StrategyCalendar.routePath) {
            StrategyCalendarRoute()
        }
        composable(route = RouteModel.MyPage.routePath) {
            MyPageRoute(
                onSupportInquiryClick = {
                    navHostController.navigate(RouteModel.SupportInquiryType.routePath)
                },
            )
        }
        composable(route = RouteModel.SupportInquiryType.routePath) {
            SupportInquiryTypeRoute(
                onFeatureSuggestionClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = SupportInquiryFeatureSuggestionMessage)
                    }
                },
                onBugReportClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = SupportInquiryBugReportMessage)
                    }
                },
                onOtherInquiryClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = SupportInquiryOtherMessage)
                    }
                },
            )
        }
    }
}

fun NavHostController.navigateToBottomNavigationDestination(
    destination: BottomNavigationDestination,
) {
    navigate(destination.routeModel.routePath) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
    }
}
