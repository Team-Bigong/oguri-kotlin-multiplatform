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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.network.providePlatformHttpClientEngineFactory
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.data.remote.model.HomeImageUrlCollection
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.navigation_back_press_exit_message
import oguri.composeapp.generated.resources.url_activity_1
import oguri.composeapp.generated.resources.url_activity_2
import oguri.composeapp.generated.resources.url_activity_3
import oguri.composeapp.generated.resources.url_hotel_1
import oguri.composeapp.generated.resources.url_hotel_2
import oguri.composeapp.generated.resources.url_plane_1
import oguri.composeapp.generated.resources.url_plane_2
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val BottomNavigationCornerRadius = 20.dp
private val BottomNavigationHorizontalPadding = 12.dp
private val BottomNavigationVerticalPadding = 8.dp
private val SnackbarTopPadding = 10.dp
private const val BOTTOM_NAVIGATION_SELECTED_ALPHA: Float = 0.14f
private val EXIT_BACK_PRESS_WINDOW = 2.seconds

@Composable
fun NavDisplay(
    snackbarHostState: SnackbarHostState,
    onExitApp: () -> Unit = {},
) {
    OguriTheme {
        val navigator: MainNavigator = rememberMainNavigator()
        val currentDestination: NavDestination? = navigator.currentDestination()
        val homeImageUrlCollection: HomeImageUrlCollection = HomeImageUrlCollection(
            hotelImageUrls = listOf(
                stringResource(Res.string.url_hotel_1),
                stringResource(Res.string.url_hotel_2),
            ),
            planeImageUrls = listOf(
                stringResource(Res.string.url_plane_1),
                stringResource(Res.string.url_plane_2),
            ),
            activityImageUrls = listOf(
                stringResource(Res.string.url_activity_1),
                stringResource(Res.string.url_activity_2),
                stringResource(Res.string.url_activity_3),
            ),
        )
        val appGraph: AppGraph = remember(homeImageUrlCollection) {
            val appGraphFactory = createGraphFactory<AppGraph.Factory>()
            val httpClient = HttpClient(providePlatformHttpClientEngineFactory()) {
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
                homeImageUrlCollection = homeImageUrlCollection,
            )
        }
        val coroutineScope = rememberCoroutineScope()
        val exitSnackbarMessage: String = stringResource(Res.string.navigation_back_press_exit_message)
        var lastMainBackPressedMark by remember { mutableStateOf<TimeMark?>(null) }
        val shouldShowBottomNavigation: Boolean =
            RouteModels.bottomNavigationDestinations.any { destination: BottomNavigationDestination ->
                isBottomNavigationDestinationSelected(currentDestination = currentDestination, destination = destination)
            }
        val isOnMainTabRoot: Boolean = isMainTabRootDestination(currentDestination)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                )
            }

            TopInjectedSnackbarHost(
                snackbarHostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = SnackbarTopPadding)
                    .padding(horizontal = 16.dp),
            )
        }

        key(currentDestination?.route, isOnMainTabRoot) {
            PlatformBackHandler(enabled = isOnMainTabRoot) {
                val nowMark: TimeMark = TimeSource.Monotonic.markNow()
                val previousMark: TimeMark? = lastMainBackPressedMark
                val isWithinExitWindow: Boolean = previousMark != null && previousMark.elapsedNow() < EXIT_BACK_PRESS_WINDOW

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

private fun isBottomNavigationDestinationSelected(
    currentDestination: NavDestination?,
    destination: BottomNavigationDestination,
): Boolean {
    return currentDestination?.hierarchy?.any { navDestination: NavDestination ->
        val routeText: String = navDestination.route ?: return@any false
        routeText == destination.routeSerialName || routeText.startsWith(destination.routeSerialName)
    } == true
}

@Composable
private fun BottomNavigationBar(
    currentDestination: NavDestination?,
    onDestinationClick: (BottomNavigationDestination) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(
                horizontal = BottomNavigationHorizontalPadding,
                vertical = BottomNavigationVerticalPadding,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BottomNavigationCornerRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f))
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            RouteModels.bottomNavigationDestinations.forEach { destination: BottomNavigationDestination ->
                val isSelected: Boolean = currentDestination?.hierarchy?.any { navDestination: NavDestination ->
                    val routeText: String = navDestination.route ?: return@any false
                    routeText == destination.routeSerialName || routeText.startsWith(destination.routeSerialName)
                } == true

                val tintColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                val backgroundColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = BOTTOM_NAVIGATION_SELECTED_ALPHA)
                } else {
                    androidx.compose.ui.graphics.Color.Transparent
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(backgroundColor)
                        .noRippleClickable(onClick = { onDestinationClick(destination) })
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Image(
                        painter = painterResource(destination.iconResource),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
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
