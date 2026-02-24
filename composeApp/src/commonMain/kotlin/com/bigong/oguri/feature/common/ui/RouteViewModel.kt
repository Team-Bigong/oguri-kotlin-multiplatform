package com.bigong.oguri.feature.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class RouteViewModel : ViewModel() {
    protected val routeViewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    open fun disposeRouteViewModel() {
        routeViewModelScope.cancel()
    }
}

@Composable
fun <T : RouteViewModel> rememberRouteViewModel(
    vararg keys: Any?,
    factory: () -> T,
): T {
    val routeViewModel: T = remember(*keys) {
        factory()
    }
    DisposableEffect(routeViewModel) {
        onDispose {
            routeViewModel.disposeRouteViewModel()
        }
    }
    return routeViewModel
}
