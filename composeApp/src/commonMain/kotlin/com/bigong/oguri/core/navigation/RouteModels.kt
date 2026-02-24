package com.bigong.oguri.core.navigation

sealed interface RouteModel {
    val routePath: String

    data object Splash : RouteModel {
        override val routePath: String = "splash"
    }

    data object Login : RouteModel {
        override val routePath: String = "login"
    }

    data object Onboarding : RouteModel {
        override val routePath: String = "onboarding"
    }

    data object Home : RouteModel {
        override val routePath: String = "home"
    }

    data class StrategyDetail(
        val strategyIdentifier: String,
    ) : RouteModel {
        override val routePath: String = "strategy_detail/$strategyIdentifier"

        companion object {
            const val baseRoute: String = "strategy_detail"
            const val strategyIdentifierArgument: String = "strategyIdentifier"
            const val routePattern: String = "$baseRoute/{$strategyIdentifierArgument}"
        }
    }

    data object StrategyCalendar : RouteModel {
        override val routePath: String = "strategy_calendar"
    }

    data object MyPage : RouteModel {
        override val routePath: String = "my_page"
    }

    data object SupportInquiryType : RouteModel {
        override val routePath: String = "support_inquiry_type"
    }
}

data class BottomNavigationDestination(
    val routeModel: RouteModel,
    val labelText: String,
)

object RouteModels {
    val bottomNavigationDestinations: List<BottomNavigationDestination> =
        listOf(
            BottomNavigationDestination(
                routeModel = RouteModel.Home,
                labelText = "홈",
            ),
            BottomNavigationDestination(
                routeModel = RouteModel.StrategyCalendar,
                labelText = "캘린더",
            ),
            BottomNavigationDestination(
                routeModel = RouteModel.MyPage,
                labelText = "마이",
            ),
        )
}
