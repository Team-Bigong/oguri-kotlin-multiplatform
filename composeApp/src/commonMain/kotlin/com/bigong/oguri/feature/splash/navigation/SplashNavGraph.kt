package com.bigong.oguri.feature.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bigong.oguri.core.navigation.MainNavigator
import com.bigong.oguri.core.navigation.RouteModel
import com.bigong.oguri.feature.splash.ui.SplashRoute

object SplashNavGraph {
    fun register(
        navGraphBuilder: NavGraphBuilder,
        navigator: MainNavigator,
    ) {
        navGraphBuilder.composable<RouteModel.Splash> {
            SplashRoute(
                onStartClick = {
                    navigator.navHostController.navigate(RouteModel.Login) {
                        popUpTo(RouteModel.Splash) {
                            inclusive = true
                        }
                    }
                },
            )
        }
    }
}
