package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import com.bigong.oguri.core.designsystem.LightOguriColorPalette
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import androidx.compose.ui.viewinterop.UIKitView
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapScene
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

data class TravelMapPinStyle(
    val outlineArgb: Int,
    val labelSize: Float,
    val selectedLabelSize: Float,
    val labelFontUri: String,
    val selectedLabelFontUri: String,
)

data class TravelMapDistanceStyle(
    val textArgb: Int,
    val outlineArgb: Int,
    val connectionArgb: Int,
    val fontSize: Float,
    val fontUri: String,
    val outlineWidth: Float,
)

interface TravelMapHost {
    fun render(
        scene: TravelMapScene,
        viewportRequest: Int,
        focusedVisitId: String?,
        pinStyle: TravelMapPinStyle,
        distanceLabels: List<String>,
        distanceStyle: TravelMapDistanceStyle,
        darkTheme: Boolean,
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

@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
@Composable
internal actual fun PlatformTravelMap(
    scene: TravelMapScene,
    viewportRequest: Int,
    focusedVisitId: String?,
    onMarkerClick: (String) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier,
) {
    val pinStyle = TravelMapPinStyle(
        LightOguriColorPalette.surfaceCard.toArgb(),
        OguriTheme.typography.labelMedium.fontSize.value * LocalDensity.current.fontScale,
        OguriTheme.typography.labelLarge.fontSize.value * LocalDensity.current.fontScale,
        Res.getUri("font/pretendard_medium.ttf"),
        Res.getUri("font/pretendard_semi_bold.ttf"),
    )
    val distanceStyle = TravelMapDistanceStyle(
        textArgb = OguriTheme.colors.textTertiary.toArgb(),
        outlineArgb = travelMapBackground().toArgb(),
        connectionArgb = travelConnectionColor.toArgb(),
        fontSize = OguriTheme.typography.caption.fontSize.value * LocalDensity.current.fontScale,
        fontUri = Res.getUri("font/pretendard_regular.ttf"),
        outlineWidth = TRAVEL_MAP_DISTANCE_OUTLINE_WIDTH,
    )
    val distanceLabels = scene.lines.map { travelDistanceLabel(it) }
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
    val darkTheme = OguriTheme.isDarkTheme
    LaunchedEffect(host, scene, viewportRequest, focusedVisitId, pinStyle, distanceLabels, distanceStyle, darkTheme) {
        host.render(scene, viewportRequest, focusedVisitId, pinStyle, distanceLabels, distanceStyle, darkTheme)
    }
    UIKitView(factory = { container }, modifier = modifier)
}
