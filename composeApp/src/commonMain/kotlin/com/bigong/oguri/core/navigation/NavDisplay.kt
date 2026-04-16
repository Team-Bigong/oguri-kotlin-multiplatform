package com.bigong.oguri.core.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.bigong.oguri.core.ad.initializeAdMob
import com.bigong.oguri.core.ad.preloadAppOpenAd
import com.bigong.oguri.core.ad.showAppOpenAdIfAvailable
import com.bigong.oguri.core.analytics.initializeOguriAnalytics
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
import com.bigong.oguri.core.platform.isNativeBottomNavigationEnabled
import com.bigong.oguri.core.platform.nativeBottomNavigationSelectionFlow
import com.bigong.oguri.core.platform.notifyNativeBottomNavigationState
import com.bigong.oguri.core.ui.component.OguriSnackBarHost
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.core.util.HapticType
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.util.extension.perform
import com.bigong.oguri.data.local.provideDisplayThemeModeLocalDataSource
import com.bigong.oguri.data.local.provideTokenLocalDataSource
import com.bigong.oguri.domain.model.DisplayThemeMode
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.bottom_navigation_calendar
import oguri.composeapp.generated.resources.bottom_navigation_home
import oguri.composeapp.generated.resources.bottom_navigation_my
import oguri.composeapp.generated.resources.navigation_back_press_exit_message
import oguri.composeapp.generated.resources.snackbar_login_success
import oguri.composeapp.generated.resources.snackbar_logout_completed
import oguri.composeapp.generated.resources.snackbar_withdraw_completed
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val EXIT_BACK_PRESS_WINDOW = 2.seconds

@Composable
fun NavDisplay(
    snackbarHostState: SnackbarHostState,
    onExitApp: () -> Unit = {},
) {
    val displayThemeModeLocalDataSource = remember { provideDisplayThemeModeLocalDataSource() }
    val initialDisplayThemeMode =
        remember(displayThemeModeLocalDataSource) {
            displayThemeModeLocalDataSource.initialize()
            displayThemeModeLocalDataSource.readDisplayThemeMode() ?: DisplayThemeMode.SYSTEM
        }
    var currentDisplayThemeMode by remember { mutableStateOf(initialDisplayThemeMode) }
    val isSystemDarkTheme = isSystemInDarkTheme()
    val isDarkTheme =
        when (currentDisplayThemeMode) {
            DisplayThemeMode.SYSTEM -> isSystemDarkTheme
            DisplayThemeMode.LIGHT -> false
            DisplayThemeMode.DARK -> true
        }

    OguriTheme(darkTheme = isDarkTheme) {
        val navigator = rememberMainNavigator()
        val currentDestination = navigator.currentDestination()
        var authSessionVersion by remember { mutableIntStateOf(0) }
        val appGraph =
            remember(authSessionVersion) {
                AuthTokenStore.initialize(localDataSource = provideTokenLocalDataSource())
                AuthTokenStore.bootstrapFromLocalDataSource()
                val appGraphFactory = createGraphFactory<AppGraph.Factory>()
                val httpClient = provideOguriHttpClient()
                appGraphFactory.create(
                    httpClient = httpClient,
                )
            }
        val homeViewModelLazy =
            remember(authSessionVersion) {
                lazy(LazyThreadSafetyMode.NONE) {
                    appGraph.homeViewModelProvider()
                }
            }
        val calendarViewModelLazy =
            remember(authSessionVersion) {
                lazy(LazyThreadSafetyMode.NONE) {
                    appGraph.calendarViewModelProvider()
                }
            }
        val myPageViewModelLazy =
            remember(authSessionVersion) {
                lazy(LazyThreadSafetyMode.NONE) {
                    appGraph.myPageViewModelProvider()
                }
            }
        val coroutineScope = rememberCoroutineScope()
        val exitSnackbarMessage = stringResource(Res.string.navigation_back_press_exit_message)
        val loginSuccessMessage = stringResource(Res.string.snackbar_login_success)
        val logoutCompletedMessage = stringResource(Res.string.snackbar_logout_completed)
        val withdrawCompletedMessage = stringResource(Res.string.snackbar_withdraw_completed)
        val homeTabLabel = stringResource(Res.string.bottom_navigation_home)
        val calendarTabLabel = stringResource(Res.string.bottom_navigation_calendar)
        val myPageTabLabel = stringResource(Res.string.bottom_navigation_my)
        val incomingDeepLinkUrl =
            appGraph.deepLinkStore.incomingUrl
                .collectAsState()
                .value
        var previousRouteText by remember { mutableStateOf<String?>(null) }
        var hasShownLaunchAppOpenAd by remember { mutableStateOf(false) }
        var lastMainBackPressedMark by remember { mutableStateOf<TimeMark?>(null) }
        var homeTabReselectTrigger by remember { mutableIntStateOf(0) }
        var calendarTabReselectTrigger by remember { mutableIntStateOf(0) }
        var myPageTabReselectTrigger by remember { mutableIntStateOf(0) }
        val nativeBottomNavigationEnabled = isNativeBottomNavigationEnabled()
        val hasPreviousBackStackEntry = navigator.navHostController.previousBackStackEntry != null
        val shouldShowBottomNavigation =
            !nativeBottomNavigationEnabled &&
                bottomNavigationDestinations.any { destination ->
                    isBottomNavigationDestinationSelected(currentDestination = currentDestination, destination = destination)
                }
        val shouldShowNativeBottomNavigation =
            nativeBottomNavigationEnabled &&
                bottomNavigationDestinations.any { destination ->
                    isBottomNavigationDestinationSelected(currentDestination = currentDestination, destination = destination)
                }
        val selectedBottomNavigationTabIndex = resolveSelectedBottomNavigationTabIndex(currentDestination)
        val selectedBottomNavigationColorArgb = colorToArgbLong(Neutral90)
        val unselectedBottomNavigationColorArgb = colorToArgbLong(Neutral40)
        val isOnMainTabRoot = isMainTabRootDestination(currentDestination)
        val isOnLoginRoute = isLoginRoute(currentDestination?.route)
        val shouldHandleDoubleBackToExit = isOnMainTabRoot || (isOnLoginRoute && !hasPreviousBackStackEntry)

        LaunchedEffect(incomingDeepLinkUrl) {
            val deepLinkUrl = incomingDeepLinkUrl ?: return@LaunchedEffect
            appGraph.deepLinkStore.clearConsumed(urlText = deepLinkUrl)

            val targetRoute = parseAppDeepLinkRoute(urlText = deepLinkUrl) ?: return@LaunchedEffect
            navigator.navigateToRouteModel(targetRoute)
        }
        LaunchedEffect(Unit) {
            initializeOguriAnalytics()
            initializeAdMob()
            preloadAppOpenAd()
        }
        LaunchedEffect(currentDestination?.route) {
            val currentRouteText = currentDestination?.route
            val previousRoute = previousRouteText
            if (currentRouteText != null && previousRoute != null) {
                if (isHomeRoute(currentRouteText) && !isHomeRoute(previousRoute)) {
                    if (homeViewModelLazy.isInitialized()) {
                        homeViewModelLazy.value.refreshRecommendPeriods()
                    }
                    if (!isOnboardingRoute(previousRoute) && !hasShownLaunchAppOpenAd) {
                        hasShownLaunchAppOpenAd = showAppOpenAdIfAvailable()
                    }
                }
                if (isMyPageRoute(currentRouteText) && !isMyPageRoute(previousRoute)) {
                    if (myPageViewModelLazy.isInitialized()) {
                        myPageViewModelLazy.value.refreshMyPageInfo()
                    }
                }
                if (isCalendarRoute(currentRouteText) && isMyPageRoute(previousRoute)) {
                    val preferredLeaveDays =
                        myPageViewModelLazy.value.uiState.value.myPageInfo
                            ?.preferredLeaveDays
                    if (preferredLeaveDays != null) {
                        calendarViewModelLazy.value.refreshWithPreferredLeaveDays(preferredLeaveDays)
                    }
                }
            } else if (currentRouteText != null && previousRoute == null && isHomeRoute(currentRouteText)) {
                if (!hasShownLaunchAppOpenAd) {
                    hasShownLaunchAppOpenAd = showAppOpenAdIfAvailable()
                }
            }
            previousRouteText = currentRouteText
        }
        LaunchedEffect(
            shouldShowNativeBottomNavigation,
            selectedBottomNavigationTabIndex,
            selectedBottomNavigationColorArgb,
            unselectedBottomNavigationColorArgb,
            homeTabLabel,
            calendarTabLabel,
            myPageTabLabel,
        ) {
            if (!nativeBottomNavigationEnabled) {
                return@LaunchedEffect
            }
            notifyNativeBottomNavigationState(
                isVisible = shouldShowNativeBottomNavigation,
                selectedTabIndex = selectedBottomNavigationTabIndex,
                selectedColorArgb = selectedBottomNavigationColorArgb,
                unselectedColorArgb = unselectedBottomNavigationColorArgb,
                homeTabLabel = homeTabLabel,
                calendarTabLabel = calendarTabLabel,
                myPageTabLabel = myPageTabLabel,
            )
        }
        LaunchedEffect(nativeBottomNavigationEnabled, currentDestination?.route) {
            if (!nativeBottomNavigationEnabled) {
                return@LaunchedEffect
            }
            nativeBottomNavigationSelectionFlow().collect { tabIndex ->
                val destination = bottomNavigationDestinations.getOrNull(tabIndex) ?: return@collect
                val isReselected =
                    isBottomNavigationDestinationSelected(
                        currentDestination = currentDestination,
                        destination = destination,
                    )
                if (isReselected) {
                    when (destination.routeModel) {
                        RouteModel.Home -> {
                            homeTabReselectTrigger += 1
                        }

                        RouteModel.Calendar -> {
                            calendarTabReselectTrigger += 1
                        }

                        RouteModel.MyPage -> {
                            myPageTabReselectTrigger += 1
                        }

                        else -> Unit
                    }
                    return@collect
                }
                navigator.navigateToBottomNavigationDestination(destination)
            }
        }

        PlatformBackGestureContainer(
            enabled = !shouldHandleDoubleBackToExit,
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
                                onDestinationClick = { destination, isReselected ->
                                    if (isReselected) {
                                        when (destination.routeModel) {
                                            RouteModel.Home -> {
                                                homeTabReselectTrigger += 1
                                            }

                                            RouteModel.Calendar -> {
                                                calendarTabReselectTrigger += 1
                                            }

                                            RouteModel.MyPage -> {
                                                myPageTabReselectTrigger += 1
                                            }

                                            else -> Unit
                                        }
                                        return@BottomNavigationBar
                                    }
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
                        homeTabReselectTrigger = homeTabReselectTrigger,
                        calendarTabReselectTrigger = calendarTabReselectTrigger,
                        myPageTabReselectTrigger = myPageTabReselectTrigger,
                        onLoginSucceeded = {
                            coroutineScope.launch {
                                snackbarHostState.showOguriSnackbar(
                                    message = loginSuccessMessage,
                                    type = OguriSnackBarType.SUCCESS,
                                )
                            }
                        },
                        onLoggedOut = {
                            authSessionVersion += 1
                            navigator.navigateToLoginAndClearBackStack()
                            coroutineScope.launch {
                                snackbarHostState.showOguriSnackbar(
                                    message = logoutCompletedMessage,
                                    type = OguriSnackBarType.INFO,
                                )
                            }
                        },
                        onWithdrawCompleted = {
                            authSessionVersion += 1
                            navigator.navigateToLoginAndClearBackStack()
                            coroutineScope.launch {
                                snackbarHostState.showOguriSnackbar(
                                    message = withdrawCompletedMessage,
                                    type = OguriSnackBarType.INFO,
                                )
                            }
                        },
                        currentDisplayThemeMode = currentDisplayThemeMode,
                        onDisplayThemeModeChange = { displayThemeMode ->
                            currentDisplayThemeMode = displayThemeMode
                            displayThemeModeLocalDataSource.writeDisplayThemeMode(displayThemeMode)
                        },
                    )
                }
            }
        }

        key(currentDestination?.route, shouldHandleDoubleBackToExit) {
            PlatformBackHandler(enabled = shouldHandleDoubleBackToExit) {
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

private fun resolveSelectedBottomNavigationTabIndex(currentDestination: NavDestination?): Int {
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

@Composable
private fun BottomNavigationBar(
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

private fun isMainTabRootDestination(currentDestination: NavDestination?): Boolean {
    val currentRouteText = currentDestination?.route ?: return false
    return bottomNavigationDestinations.any { destination ->
        currentRouteText == destination.routeSerialName
    }
}

private fun isHomeRoute(routeText: String): Boolean {
    val homeRouteSerialName =
        RouteModel.Home
            .serializer()
            .descriptor.serialName
    return routeText == homeRouteSerialName || routeText.startsWith(homeRouteSerialName)
}

private fun isMyPageRoute(routeText: String): Boolean {
    val myPageRouteSerialName =
        RouteModel.MyPage
            .serializer()
            .descriptor.serialName
    return routeText == myPageRouteSerialName || routeText.startsWith(myPageRouteSerialName)
}

private fun isCalendarRoute(routeText: String): Boolean {
    val calendarRouteSerialName =
        RouteModel.Calendar
            .serializer()
            .descriptor.serialName
    return routeText == calendarRouteSerialName || routeText.startsWith(calendarRouteSerialName)
}

private fun isOnboardingRoute(routeText: String): Boolean {
    val onboardingRouteSerialName =
        RouteModel.Onboarding
            .serializer()
            .descriptor.serialName
    return routeText == onboardingRouteSerialName || routeText.startsWith(onboardingRouteSerialName)
}

private fun colorToArgbLong(color: Color): Long {
    val alpha = (color.alpha * 255f).roundToInt().coerceIn(0, 255)
    val red = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val green = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val blue = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    val argbInt = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    return argbInt.toLong() and 0xFFFFFFFF
}

private fun isLoginRoute(routeText: String?): Boolean {
    val loginRouteText = routeText ?: return false
    val loginRouteSerialName =
        RouteModel.Login
            .serializer()
            .descriptor.serialName
    return loginRouteText == loginRouteSerialName || loginRouteText.startsWith(loginRouteSerialName)
}
