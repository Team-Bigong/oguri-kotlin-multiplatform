package com.bigong.oguri.feature.login.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.navigation.MainNavigator
import com.bigong.oguri.core.navigation.RouteModel
import com.bigong.oguri.data.model.LoginProviderType
import com.bigong.oguri.feature.login.ui.LoginRoute

object LoginNavGraph {
    fun register(
        navGraphBuilder: NavGraphBuilder,
        navigator: MainNavigator,
        appGraph: AppGraph,
    ) {
        navGraphBuilder.composable<RouteModel.Login> {
            LoginRoute(
                onLoginClick = { loginProviderType: LoginProviderType ->
                    appGraph.userStateRepository.updateLoginProviderType(loginProviderType)
                    navigator.navigateToOnboarding()
                },
                onGuestBrowseClick = {
                    appGraph.userStateRepository.updateLoginProviderType(LoginProviderType.GUEST)
                    navigator.navHostController.navigate(RouteModel.Home) {
                        popUpTo(RouteModel.Login) {
                            inclusive = true
                        }
                    }
                },
            )
        }
    }
}
