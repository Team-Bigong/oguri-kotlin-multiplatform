package com.bigong.oguri.core.navigation

import kotlin.reflect.KClass
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.bottom_navigation_calendar
import oguri.composeapp.generated.resources.bottom_navigation_home
import oguri.composeapp.generated.resources.bottom_navigation_my

sealed interface RouteModel {
    @Serializable
    data object Splash : RouteModel

    @Serializable
    data object Login : RouteModel

    @Serializable
    data object Onboarding : RouteModel

    @Serializable
    data object Home : RouteModel

    @Serializable
    data class StrategyDetail(
        val strategyIdentifier: String,
    ) : RouteModel

    @Serializable
    data object StrategyCalendar : RouteModel

    @Serializable
    data object MyPage : RouteModel

    @Serializable
    data object SupportInquiryType : RouteModel
}

data class BottomNavigationDestination(
    val routeModel: RouteModel,
    val routeClass: KClass<out RouteModel>,
    val routeSerialName: String,
    val labelResource: StringResource,
)

object RouteModels {
    val bottomNavigationDestinations: List<BottomNavigationDestination> =
        listOf(
            BottomNavigationDestination(
                routeModel = RouteModel.Home,
                routeClass = RouteModel.Home::class,
                routeSerialName = RouteModel.Home.serializer().descriptor.serialName,
                labelResource = Res.string.bottom_navigation_home,
            ),
            BottomNavigationDestination(
                routeModel = RouteModel.StrategyCalendar,
                routeClass = RouteModel.StrategyCalendar::class,
                routeSerialName = RouteModel.StrategyCalendar.serializer().descriptor.serialName,
                labelResource = Res.string.bottom_navigation_calendar,
            ),
            BottomNavigationDestination(
                routeModel = RouteModel.MyPage,
                routeClass = RouteModel.MyPage::class,
                routeSerialName = RouteModel.MyPage.serializer().descriptor.serialName,
                labelResource = Res.string.bottom_navigation_my,
            ),
        )
}
