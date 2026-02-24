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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable

private val BottomNavigationCornerRadius = 20.dp
private val BottomNavigationHorizontalPadding = 16.dp
private val BottomNavigationVerticalPadding = 12.dp
private val BottomNavigationItemVerticalPadding = 12.dp
private val BottomNavigationItemHorizontalPadding = 8.dp
private val SnackbarTopPadding = 12.dp
private const val BottomNavigationSelectedAlpha: Float = 0.16f
private const val BottomNavigationUnselectedAlpha: Float = 0.06f

@Composable
fun NavDisplay(
    snackbarHostState: SnackbarHostState,
) {
    OguriTheme {
        val navHostController = rememberNavController()
        val navBackStackEntry by navHostController.currentBackStackEntryAsState()
        val currentRoutePath: String? = navBackStackEntry?.destination?.route
        val shouldShowBottomNavigation: Boolean = RouteModels.bottomNavigationDestinations.any { destination ->
            destination.routeModel.routePath == currentRoutePath
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (shouldShowBottomNavigation) {
                        BottomNavigationBar(
                            currentRoutePath = currentRoutePath,
                            onDestinationClick = { destination ->
                                navHostController.navigateToBottomNavigationDestination(destination)
                            },
                        )
                    }
                },
                snackbarHost = {},
            ) { contentPaddingValues ->
                MainNavHost(
                    navHostController = navHostController,
                    contentPaddingValues = contentPaddingValues,
                    snackbarHostState = snackbarHostState,
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
    }
}

@Composable
private fun BottomNavigationBar(
    currentRoutePath: String?,
    onDestinationClick: (BottomNavigationDestination) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(
                horizontal = BottomNavigationHorizontalPadding,
                vertical = BottomNavigationVerticalPadding,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(BottomNavigationCornerRadius),
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RouteModels.bottomNavigationDestinations.forEach { destination ->
                val isSelected: Boolean = destination.routeModel.routePath == currentRoutePath
                val containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = BottomNavigationSelectedAlpha)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = BottomNavigationUnselectedAlpha)
                }
                val contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = containerColor,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .noRippleClickable(onClick = { onDestinationClick(destination) })
                        .padding(
                            horizontal = BottomNavigationItemHorizontalPadding,
                            vertical = BottomNavigationItemVerticalPadding,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = destination.labelText,
                        color = contentColor,
                        style = OguriTheme.typography.labelLarge,
                    )
                }
            }
        }
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
