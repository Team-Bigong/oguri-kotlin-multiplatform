package com.bigong.oguri.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.feature.calendar.navigation.CalendarNavGraph
import com.bigong.oguri.feature.home.navigation.HomeNavGraph
import com.bigong.oguri.feature.login.navigation.LoginNavGraph
import com.bigong.oguri.feature.mypage.navigation.MyPageNavGraph
import com.bigong.oguri.feature.onboarding.navigation.OnboardingNavGraph
import com.bigong.oguri.feature.splash.navigation.SplashNavGraph
import com.bigong.oguri.feature.strategy.navigation.StrategyNavGraph
import com.bigong.oguri.feature.support.navigation.SupportNavGraph

@Composable
fun MainNavHost(
    appGraph: AppGraph,
    navigator: MainNavigator,
    contentPaddingValues: PaddingValues,
    snackbarHostState: SnackbarHostState,
) {
    NavHost(
        navController = navigator.navHostController,
        startDestination = RouteModel.Splash,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPaddingValues),
    ) {
        SplashNavGraph.register(navGraphBuilder = this, navigator = navigator)
        LoginNavGraph.register(navGraphBuilder = this, navigator = navigator, appGraph = appGraph)
        OnboardingNavGraph.register(navGraphBuilder = this, navigator = navigator, appGraph = appGraph)
        HomeNavGraph.register(
            navGraphBuilder = this,
            navigator = navigator,
            appGraph = appGraph,
            snackbarHostState = snackbarHostState,
        )
        StrategyNavGraph.register(
            navGraphBuilder = this,
            navigator = navigator,
            appGraph = appGraph,
            snackbarHostState = snackbarHostState,
        )
        CalendarNavGraph.register(navGraphBuilder = this, appGraph = appGraph)
        MyPageNavGraph.register(
            navGraphBuilder = this,
            navigator = navigator,
            appGraph = appGraph,
            snackbarHostState = snackbarHostState,
        )
        SupportNavGraph.register(navGraphBuilder = this, snackbarHostState = snackbarHostState)
    }
}
