package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapScene

@Composable
internal expect fun PlatformTravelMap(
    scene: TravelMapScene,
    viewportRequest: Int,
    focusedVisitId: String?,
    onMarkerClick: (String) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier,
)
