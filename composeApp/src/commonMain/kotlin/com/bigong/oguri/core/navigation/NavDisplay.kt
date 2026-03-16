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
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.core.network.providePlatformHttpClientEngineFactory
import com.bigong.oguri.core.platform.PlatformBackGestureContainer
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.ui.component.OguriSnackBarHost
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.data.local.provideTokenLocalDataSource
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.navigation_back_press_exit_message
import oguri.composeapp.generated.resources.snackbar_logout_completed
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
                val httpClient =
                    HttpClient(providePlatformHttpClientEngineFactory()) {
                        expectSuccess = true
                        install(DefaultRequest) {
                            val accessToken = AuthTokenStore.getAccessToken()
                            if (!accessToken.isNullOrBlank()) {
                                header(HttpHeaders.Authorization, "Bearer $accessToken")
                            }
                        }
                        install(ContentNegotiation) {
                            json(
                                Json {
                                    ignoreUnknownKeys = true
                                },
                            )
                        }
                    }
                appGraphFactory.create(
                    httpClient = httpClient,
                )
            }
        val coroutineScope = rememberCoroutineScope()
        val exitSnackbarMessage = stringResource(Res.string.navigation_back_press_exit_message)
        val logoutCompletedMessage = stringResource(Res.string.snackbar_logout_completed)
        var lastMainBackPressedMark by remember { mutableStateOf<TimeMark?>(null) }
        val shouldShowBottomNavigation =
            RouteModels.bottomNavigationDestinations.any { destination ->
                isBottomNavigationDestinationSelected(currentDestination = currentDestination, destination = destination)
            }
        val isOnMainTabRoot = isMainTabRootDestination(currentDestination)

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
            RouteModels.bottomNavigationDestinations.forEach { destination ->
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
    return RouteModels.bottomNavigationDestinations.any { destination ->
        currentRouteText == destination.routeSerialName
    }
}
