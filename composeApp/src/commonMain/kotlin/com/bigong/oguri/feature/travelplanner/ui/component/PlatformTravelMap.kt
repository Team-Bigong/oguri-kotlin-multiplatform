package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapLine
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapScene
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_distance_kilometers
import org.jetbrains.compose.resources.stringResource

internal val travelConnectionColor: Color get() = Neutral40

@Composable
internal fun travelMapDistanceOutline(): Color =
    if (OguriTheme.isDarkTheme) OguriTheme.colors.surfaceCard else Color(0xFFEEF4F1)

internal const val TRAVEL_MAP_DISTANCE_OUTLINE_WIDTH: Float = 2.5f

@Composable
internal fun travelDistanceLabel(line: TravelMapLine): String =
    stringResource(Res.string.travel_distance_kilometers, line.displayDistance)

@Composable
internal expect fun PlatformTravelMap(
    scene: TravelMapScene,
    viewportRequest: Int,
    focusedVisitId: String?,
    onMarkerClick: (String) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier,
)
