package com.bigong.oguri.core.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.HapticType
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.util.extension.perform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun BottomNavigationBar(
    currentDestination: NavDestination?,
    onDestinationClick: (BottomNavigationDestination, Boolean) -> Unit,
) {
    Column {
        HorizontalDivider(thickness = 1.dp, color = Neutral20)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Neutral5)
                    .padding(top = 2.dp)
                    .padding(horizontal = 18.dp)
                    .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            bottomNavigationDestinations.forEach { destination ->
                val isSelected =
                    currentDestination?.hierarchy?.any { navDestination ->
                        val routeText = navDestination.route ?: return@any false
                        routeText == destination.routeSerialName || routeText.startsWith(destination.routeSerialName)
                    } == true

                val tintColor =
                    if (isSelected) {
                        Neutral90
                    } else {
                        Neutral40
                    }

                Column(
                    modifier =
                        Modifier
                            .noRippleClickable(
                                onClick = {
                                    HapticType.Selection.perform()
                                    onDestinationClick(destination, isSelected)
                                },
                            ).padding(horizontal = 18.dp, vertical = 10.dp)
                            .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Image(
                        painter = painterResource(destination.iconResource),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(tintColor),
                    )
                    Text(
                        text = stringResource(destination.labelResource),
                        color = tintColor,
                        style = OguriTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

internal fun isBottomNavigationDestinationSelected(
    currentDestination: NavDestination?,
    destination: BottomNavigationDestination,
): Boolean {
    return currentDestination?.hierarchy?.any { navDestination ->
        val routeText = navDestination.route ?: return@any false
        routeText == destination.routeSerialName || routeText.startsWith(destination.routeSerialName)
    } == true
}

internal fun resolveSelectedBottomNavigationTabIndex(currentDestination: NavDestination?): Int {
    val selectedDestinationIndex =
        bottomNavigationDestinations.indexOfFirst { destination ->
            isBottomNavigationDestinationSelected(
                currentDestination = currentDestination,
                destination = destination,
            )
        }
    if (selectedDestinationIndex >= 0) {
        return selectedDestinationIndex
    }
    return 0
}

internal fun isMainTabRootDestination(currentDestination: NavDestination?): Boolean {
    val currentRouteText = currentDestination?.route ?: return false
    return bottomNavigationDestinations.any { destination ->
        currentRouteText == destination.routeSerialName
    }
}

internal fun isHomeRoute(routeText: String): Boolean {
    val homeRouteSerialName =
        RouteModel.Home
            .serializer()
            .descriptor.serialName
    return routeText == homeRouteSerialName || routeText.startsWith(homeRouteSerialName)
}

internal fun isMyPageRoute(routeText: String): Boolean {
    val myPageRouteSerialName =
        RouteModel.MyPage
            .serializer()
            .descriptor.serialName
    return routeText == myPageRouteSerialName || routeText.startsWith(myPageRouteSerialName)
}

internal fun isCalendarRoute(routeText: String): Boolean {
    val calendarRouteSerialName =
        RouteModel.Calendar
            .serializer()
            .descriptor.serialName
    return routeText == calendarRouteSerialName || routeText.startsWith(calendarRouteSerialName)
}

internal fun colorToArgbLong(color: Color): Long {
    val alpha = (color.alpha * 255f).roundToInt().coerceIn(0, 255)
    val red = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val green = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val blue = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    val argbInt = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    return argbInt.toLong() and 0xFFFFFFFF
}

internal fun isLoginRoute(routeText: String?): Boolean {
    val loginRouteText = routeText ?: return false
    val loginRouteSerialName =
        RouteModel.Login
            .serializer()
            .descriptor.serialName
    return loginRouteText == loginRouteSerialName || loginRouteText.startsWith(loginRouteSerialName)
}
