package com.bigong.oguri.core.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.bigong.oguri.core.deeplink.parseAppDeepLinkRoute
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.core.network.provideOguriHttpClient
import com.bigong.oguri.core.platform.PlatformBackGestureContainer
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.ui.component.OguriSnackBarHost
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.core.util.HapticType
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.util.extension.perform
import com.bigong.oguri.data.local.provideTokenLocalDataSource
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.navigation_back_press_exit_message
import oguri.composeapp.generated.resources.snackbar_logout_completed
import oguri.composeapp.generated.resources.snackbar_withdraw_completed
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val EXIT_BACK_PRESS_WINDOW = 2.seconds

@Composable
fun NavDisplay(
    snackbarHostState: SnackbarHostState,
    onExitApp: () -> Unit = {},
) {
    OguriTheme {
        val navigator = rememberMainNavigator()
        val currentDestination = navigator.currentDestination()
        val appGraph =
            remember {
                AuthTokenStore.initialize(localDataSource = provideTokenLocalDataSource())
                AuthTokenStore.bootstrapFromLocalDataSource()
                val appGraphFactory = createGraphFactory<AppGraph.Factory>()
                val httpClient = provideOguriHttpClient()
                appGraphFactory.create(
                    httpClient = httpClient,
                )
            }
        val homeViewModelLazy =
            remember {
                lazy(LazyThreadSafetyMode.NONE) {
                    appGraph.homeViewModelProvider()
                }
            }
        val calendarViewModelLazy =
            remember {
                lazy(LazyThreadSafetyMode.NONE) {
                    appGraph.calendarViewModelProvider()
                }
            }
        val myPageViewModelLazy =
            remember {
                lazy(LazyThreadSafetyMode.NONE) {
                    appGraph.myPageViewModelProvider()
                }
            }
        val coroutineScope = rememberCoroutineScope()
        val exitSnackbarMessage = stringResource(Res.string.navigation_back_press_exit_message)
        val logoutCompletedMessage = stringResource(Res.string.snackbar_logout_completed)
        val withdrawCompletedMessage = stringResource(Res.string.snackbar_withdraw_completed)
        val incomingDeepLinkUrl =
            appGraph.deepLinkStore.incomingUrl
                .collectAsState()
                .value
        var previousRouteText by remember { mutableStateOf<String?>(null) }
        var lastMainBackPressedMark by remember { mutableStateOf<TimeMark?>(null) }
        val shouldShowBottomNavigation =
            bottomNavigationDestinations.any { destination ->
                isBottomNavigationDestinationSelected(currentDestination = currentDestination, destination = destination)
            }
        val isOnMainTabRoot = isMainTabRootDestination(currentDestination)

        LaunchedEffect(incomingDeepLinkUrl) {
            val deepLinkUrl = incomingDeepLinkUrl ?: return@LaunchedEffect
            appGraph.deepLinkStore.clearConsumed(urlText = deepLinkUrl)

            val targetRoute = parseAppDeepLinkRoute(urlText = deepLinkUrl) ?: return@LaunchedEffect
            navigator.navigateToRouteModel(targetRoute)
        }
        LaunchedEffect(currentDestination?.route) {
            val currentRouteText = currentDestination?.route
            val previousRoute = previousRouteText
            if (currentRouteText != null && previousRoute != null) {
                if (isHomeRoute(currentRouteText) && !isHomeRoute(previousRoute)) {
                    if (homeViewModelLazy.isInitialized()) {
                        homeViewModelLazy.value.refreshRecommendPeriods()
                    }
                }
                if (isMyPageRoute(currentRouteText) && !isMyPageRoute(previousRoute)) {
                    if (myPageViewModelLazy.isInitialized()) {
                        myPageViewModelLazy.value.refreshMyPageInfo()
                    }
                }
            }
            previousRouteText = currentRouteText
        }

        PlatformBackGestureContainer(
            enabled = !isOnMainTabRoot,
            onBack = { navigator.popBackStack() },
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Neutral5),
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (shouldShowBottomNavigation) {
                            BottomNavigationBar(
                                currentDestination = currentDestination,
                                onDestinationClick = { destination ->
                                    navigator.navigateToBottomNavigationDestination(destination)
                                },
                            )
                        }
                    },
                    snackbarHost = {
                        OguriSnackBarHost(
                            hostState = snackbarHostState,
                            hasBottomNavigation = shouldShowBottomNavigation,
                        )
                    },
                ) { contentPaddingValues ->
                    MainNavHost(
                        appGraph = appGraph,
                        homeViewModelProvider = { homeViewModelLazy.value },
                        calendarViewModelProvider = { calendarViewModelLazy.value },
                        myPageViewModelProvider = { myPageViewModelLazy.value },
                        navigator = navigator,
                        snackbarHostState = snackbarHostState,
                        contentPaddingValues = contentPaddingValues,
                        onLoggedOut = {
                            navigator.navigateToLogin()
                            coroutineScope.launch {
                                snackbarHostState.showOguriSnackbar(
                                    message = logoutCompletedMessage,
                                    type = OguriSnackBarType.INFO,
                                )
                            }
                        },
                        onWithdrawCompleted = {
                            navigator.navigateToLogin()
                            coroutineScope.launch {
                                snackbarHostState.showOguriSnackbar(
                                    message = withdrawCompletedMessage,
                                    type = OguriSnackBarType.INFO,
                                )
                            }
                        },
                    )
                }
            }
        }

        key(currentDestination?.route, isOnMainTabRoot) {
            PlatformBackHandler(enabled = isOnMainTabRoot) {
                val nowMark = TimeSource.Monotonic.markNow()
                val previousMark = lastMainBackPressedMark
                val isWithinExitWindow = previousMark != null && previousMark.elapsedNow() < EXIT_BACK_PRESS_WINDOW

                if (isWithinExitWindow) {
                    onExitApp()
                    return@PlatformBackHandler
                }

                lastMainBackPressedMark = nowMark
                coroutineScope.launch {
                    snackbarHostState.showOguriSnackbar(
                        message = exitSnackbarMessage,
                        type = OguriSnackBarType.INFO,
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
    return currentDestination?.hierarchy?.any { navDestination ->
        val routeText = navDestination.route ?: return@any false
        routeText == destination.routeSerialName || routeText.startsWith(destination.routeSerialName)
    } == true
}

@Composable
private fun BottomNavigationBar(
    currentDestination: NavDestination?,
    onDestinationClick: (BottomNavigationDestination) -> Unit,
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
                                    if (isSelected) {
                                        return@noRippleClickable
                                    }
                                    HapticType.Selection.perform()
                                    onDestinationClick(destination)
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

private fun isMainTabRootDestination(currentDestination: NavDestination?): Boolean {
    val currentRouteText = currentDestination?.route ?: return false
    return bottomNavigationDestinations.any { destination ->
        currentRouteText == destination.routeSerialName
    }
}

private fun isHomeRoute(routeText: String): Boolean {
    val homeRouteSerialName = RouteModel.Home.serializer().descriptor.serialName
    return routeText == homeRouteSerialName || routeText.startsWith(homeRouteSerialName)
}

private fun isMyPageRoute(routeText: String): Boolean {
    val myPageRouteSerialName = RouteModel.MyPage.serializer().descriptor.serialName
    return routeText == myPageRouteSerialName || routeText.startsWith(myPageRouteSerialName)
}
