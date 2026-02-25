package com.bigong.oguri.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.platform.PlatformBackGestureContainer
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.util.extension.noRippleClickable
import dev.zacsweers.metro.createGraph
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.navigation_back_press_exit_message
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val BottomNavigationCornerRadius = 20.dp
private val BottomNavigationHorizontalPadding = 16.dp
private val BottomNavigationVerticalPadding = 12.dp
private val BottomNavigationItemVerticalPadding = 12.dp
private val BottomNavigationItemHorizontalPadding = 8.dp
private val SnackbarTopPadding = 12.dp
private const val BOTTOM_NAVIGATION_SELECTED_ALPHA: Float = 0.16f
private const val BOTTOM_NAVIGATION_UNSELECTED_ALPHA: Float = 0.06f
private val ExitBackPressWindow = 2.seconds

@Composable
fun NavDisplay(
    snackbarHostState: SnackbarHostState,
    onExitApp: () -> Unit = {},
) {
    OguriTheme {
        val navigator: MainNavigator = rememberMainNavigator()
        val appGraph: AppGraph = remember { createGraph<AppGraph>() }
        val currentDestination: NavDestination? = navigator.currentDestination()
        val coroutineScope = rememberCoroutineScope()
        val exitSnackbarMessage: String = stringResource(Res.string.navigation_back_press_exit_message)
        var lastMainBackPressedMark by remember { mutableStateOf<TimeMark?>(null) }
        val shouldShowBottomNavigation: Boolean =
            RouteModels.bottomNavigationDestinations.any { destination: BottomNavigationDestination ->
                isBottomNavigationDestinationSelected(currentDestination, destination)
            }
        val isOnMainTabRoot: Boolean = isMainTabRootDestination(currentDestination)

        LaunchedEffect(currentDestination?.route) {
            if (!isOnMainTabRoot) {
                lastMainBackPressedMark = null
            }
        }

        PlatformBackGestureContainer(
            enabled = !isOnMainTabRoot,
            onBack = { navigator.popBackStack() },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (shouldShowBottomNavigation) {
                            BottomNavigationBar(
                                currentDestination = currentDestination,
                                onDestinationClick = { destination: BottomNavigationDestination ->
                                    navigator.navigateToBottomNavigationDestination(destination)
                                },
                            )
                        }
                    },
                    snackbarHost = {},
                ) { contentPaddingValues ->
                    MainNavHost(
                        appGraph = appGraph,
                        navigator = navigator,
                        contentPaddingValues = contentPaddingValues,
                        snackbarHostState = snackbarHostState,
                    )
                }

                TopInjectedSnackbarHost(
                    snackbarHostState = snackbarHostState,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = SnackbarTopPadding)
                            .padding(horizontal = 16.dp),
                )
            }
        }

        key(currentDestination?.route, isOnMainTabRoot) {
            PlatformBackHandler(enabled = isOnMainTabRoot) {
                val nowMark: TimeMark = TimeSource.Monotonic.markNow()
                val previousMark: TimeMark? = lastMainBackPressedMark
                val isWithinExitWindow: Boolean = previousMark != null && previousMark.elapsedNow() < ExitBackPressWindow

                if (isWithinExitWindow) {
                    onExitApp()
                    return@PlatformBackHandler
                }

                lastMainBackPressedMark = nowMark
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = exitSnackbarMessage)
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentDestination: NavDestination?,
    onDestinationClick: (BottomNavigationDestination) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .padding(
                    horizontal = BottomNavigationHorizontalPadding,
                    vertical = BottomNavigationVerticalPadding,
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(BottomNavigationCornerRadius),
                    ).padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RouteModels.bottomNavigationDestinations.forEach { destination: BottomNavigationDestination ->
                val isSelected: Boolean =
                    currentDestination?.hierarchy?.any { navDestination: NavDestination ->
                        val routeText: String = navDestination.route ?: return@any false
                        routeText == destination.routeSerialName || routeText.startsWith(destination.routeSerialName)
                    } == true
                val containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = BOTTOM_NAVIGATION_SELECTED_ALPHA)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = BOTTOM_NAVIGATION_UNSELECTED_ALPHA)
                    }
                val contentColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .background(
                                color = containerColor,
                                shape = RoundedCornerShape(14.dp),
                            ).noRippleClickable(onClick = { onDestinationClick(destination) })
                            .padding(
                                horizontal = BottomNavigationItemHorizontalPadding,
                                vertical = BottomNavigationItemVerticalPadding,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(destination.labelResource),
                        color = contentColor,
                        style = OguriTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

private fun isBottomNavigationDestinationSelected(
    currentDestination: NavDestination?,
    destination: BottomNavigationDestination,
): Boolean {
    return currentDestination?.hierarchy?.any { navDestination: NavDestination ->
        val routeText: String = navDestination.route ?: return@any false
        routeText == destination.routeSerialName || routeText.startsWith(destination.routeSerialName)
    } == true
}

private fun isMainTabRootDestination(currentDestination: NavDestination?): Boolean {
    val currentRouteText: String = currentDestination?.route ?: return false
    return RouteModels.bottomNavigationDestinations.any { destination: BottomNavigationDestination ->
        currentRouteText == destination.routeSerialName
    }
}

@Composable
private fun TopInjectedSnackbarHost(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(14.dp),
            )
        },
    )
}
