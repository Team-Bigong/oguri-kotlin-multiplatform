package com.bigong.oguri.core.navigation

import kotlin.reflect.KClass
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.bottom_navigation_calendar
import oguri.composeapp.generated.resources.bottom_navigation_home
import oguri.composeapp.generated.resources.bottom_navigation_my
import oguri.composeapp.generated.resources.ic_calendar
import oguri.composeapp.generated.resources.ic_home
import oguri.composeapp.generated.resources.ic_mypage

sealed interface RouteModel {
    @Serializable
    data object Splash : RouteModel

    @Serializable
    data object Login : RouteModel

    @Serializable
    data object Home : RouteModel

    @Serializable
    data object Calendar : RouteModel

    @Serializable
    data object MyPage : RouteModel

    @Serializable
    data class PlaceDetail(
        val placeId: Long,
    ) : RouteModel
}

data class BottomNavigationDestination(
    val routeModel: RouteModel,
    val routeClass: KClass<out RouteModel>,
    val routeSerialName: String,
    val labelResource: StringResource,
    val iconResource: DrawableResource,
)

object RouteModels {
    val bottomNavigationDestinations: List<BottomNavigationDestination> =
        listOf(
            BottomNavigationDestination(
                routeModel = RouteModel.Home,
                routeClass = RouteModel.Home::class,
                routeSerialName = RouteModel.Home.serializer().descriptor.serialName,
                labelResource = Res.string.bottom_navigation_home,
                iconResource = Res.drawable.ic_home,
            ),
            BottomNavigationDestination(
                routeModel = RouteModel.Calendar,
                routeClass = RouteModel.Calendar::class,
                routeSerialName = RouteModel.Calendar.serializer().descriptor.serialName,
                labelResource = Res.string.bottom_navigation_calendar,
                iconResource = Res.drawable.ic_calendar,
            ),
            BottomNavigationDestination(
                routeModel = RouteModel.MyPage,
                routeClass = RouteModel.MyPage::class,
                routeSerialName = RouteModel.MyPage.serializer().descriptor.serialName,
                labelResource = Res.string.bottom_navigation_my,
                iconResource = Res.drawable.ic_mypage,
            ),
        )
}
