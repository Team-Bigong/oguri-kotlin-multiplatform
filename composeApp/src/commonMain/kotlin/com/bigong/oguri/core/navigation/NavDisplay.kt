package com.bigong.oguri.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import com.bigong.oguri.core.ad.initializeAdMob
import com.bigong.oguri.core.ad.preloadAppOpenAd
import com.bigong.oguri.core.ad.showAppOpenAdIfAvailable
import com.bigong.oguri.core.analytics.initializeOguriAnalytics
import com.bigong.oguri.core.deeplink.parseAppDeepLinkRoute
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
import com.bigong.oguri.data.local.provideDisplayThemeModeLocalDataSource
import com.bigong.oguri.data.local.provideTokenLocalDataSource
import com.bigong.oguri.domain.model.DisplayThemeMode
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.bottom_navigation_calendar
import oguri.composeapp.generated.resources.bottom_navigation_home
import oguri.composeapp.generated.resources.bottom_navigation_my
import oguri.composeapp.generated.resources.navigation_back_press_exit_message
import oguri.composeapp.generated.resources.snackbar_login_success
import oguri.composeapp.generated.resources.snackbar_logout_completed
import oguri.composeapp.generated.resources.snackbar_withdraw_completed
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
        val onBottomNavigationReselected: (BottomNavigationDestination) -> Unit = { destination ->
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

                else -> {
                    Unit
                }
            }
        }

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
                    onBottomNavigationReselected(destination)
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
                                        onBottomNavigationReselected(destination)
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
