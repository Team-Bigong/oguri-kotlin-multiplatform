package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapScene
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

interface TravelMapHost {
    fun render(
        scene: TravelMapScene,
        viewportRequest: Int,
        focusedVisitId: String?,
    )

    fun dispose()
}

fun interface TravelMapHostFactory {
    fun attach(
        container: UIView,
        onMarkerClick: (String) -> Unit,
        onUnavailable: () -> Unit,
    ): TravelMapHost
}

private var factory: TravelMapHostFactory? = null

fun installTravelMapHostFactory(value: TravelMapHostFactory) {
    factory = value
}

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformTravelMap(
    scene: TravelMapScene,
    viewportRequest: Int,
    focusedVisitId: String?,
    onMarkerClick: (String) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier,
) {
    val hostFactory = factory
    if (hostFactory == null) {
        LaunchedEffect(Unit) { onUnavailable() }
        return
    }
    val latestClick = rememberUpdatedState(onMarkerClick)
    val latestUnavailable = rememberUpdatedState(onUnavailable)
    val container = remember { UIView() }
    val host =
        remember(container) {
            hostFactory.attach(container, { latestClick.value(it) }, { latestUnavailable.value() })
        }
    DisposableEffect(host) {
        onDispose { host.dispose() }
    }
    LaunchedEffect(host, scene, viewportRequest, focusedVisitId) {
        host.render(scene, viewportRequest, focusedVisitId)
    }
    UIKitView(factory = { container }, modifier = modifier)
}
