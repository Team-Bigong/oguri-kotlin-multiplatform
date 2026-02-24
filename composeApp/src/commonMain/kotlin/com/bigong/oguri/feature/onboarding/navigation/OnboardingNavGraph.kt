package com.bigong.oguri.feature.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.navigation.MainNavigator
import com.bigong.oguri.core.navigation.RouteModel
import com.bigong.oguri.feature.onboarding.ui.OnboardingRoute

object OnboardingNavGraph {
    fun register(
        navGraphBuilder: NavGraphBuilder,
        navigator: MainNavigator,
        appGraph: AppGraph,
    ) {
        navGraphBuilder.composable<RouteModel.Onboarding> {
            OnboardingRoute(
                userStateRepository = appGraph.userStateRepository,
                onCalculateStrategyClick = {
                    navigator.navHostController.navigate(RouteModel.Home) {
                        popUpTo(RouteModel.Onboarding) {
                            inclusive = true
                        }
                    }
                },
            )
        }
    }
}
