package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapScene
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.travel_map_fit
import oguri.composeapp.generated.resources.travel_map_hide
import oguri.composeapp.generated.resources.travel_map_sequence
import oguri.composeapp.generated.resources.travel_map_show
import oguri.composeapp.generated.resources.travel_map_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelPlanPane(
    scene: TravelMapScene,
    viewportRequest: Int,
    focusedVisitId: String?,
    onMarkerClick: (String) -> Unit,
    onFitMap: () -> Unit,
    modifier: Modifier = Modifier,
    status: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    var mapExpanded by rememberSaveable { mutableStateOf(true) }
    var controlsHeight by remember(density) { mutableIntStateOf(with(density) { 48.dp.roundToPx() }) }
    var statusHeight by remember(density) { mutableIntStateOf(0) }
    var legendHeight by remember(density) { mutableIntStateOf(with(density) { 32.dp.roundToPx() }) }
    var mapUnavailable by remember { mutableStateOf(false) }
    var mapAttempt by remember { mutableIntStateOf(0) }
    BoxWithConstraints(modifier) {
        val chromeHeight = with(density) {
            (controlsHeight + statusHeight + if (scene.lines.isNotEmpty()) legendHeight else 0).toDp()
        }
        val maxMapHeight = (maxHeight - chromeHeight - (112 * density.fontScale).dp).coerceAtLeast(0.dp)
        val canShowMap = maxMapHeight >= 120.dp
        val showMap = canShowMap && mapExpanded
        val mapHeight = (maxHeight * 0.48f).coerceAtLeast(120.dp).coerceAtMost(maxMapHeight)
        Column(Modifier.fillMaxSize()) {
            if (showMap) {
                if (mapUnavailable) {
                    Text(stringResource(Res.string.travel_map_unavailable), Modifier.padding(horizontal = 16.dp))
                    TravelTextButton(onClick = {
                        mapAttempt++
                        mapUnavailable = false
                    }, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(stringResource(Res.string.common_retry_button))
                    }
                } else {
                    key(mapAttempt) {
                        PlatformTravelMap(
                            scene = scene,
                            viewportRequest = viewportRequest,
                            focusedVisitId = focusedVisitId,
                            onMarkerClick = onMarkerClick,
                            onUnavailable = { mapUnavailable = true },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(mapHeight)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }
                    if (scene.lines.isNotEmpty()) {
                        Text(
                            stringResource(Res.string.travel_map_sequence),
                            modifier = Modifier.fillMaxWidth().onSizeChanged { legendHeight = it.height }
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            color = OguriTheme.colors.textSecondary,
                            style = OguriTheme.typography.bodySmall,
                        )
                    }
                }
            }
            if (canShowMap) {
                Row(
                    modifier = Modifier.fillMaxWidth().onSizeChanged { controlsHeight = it.height }.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TravelTextButton(onClick = { mapExpanded = !mapExpanded }) {
                        Text(
                            stringResource(if (showMap) Res.string.travel_map_hide else Res.string.travel_map_show),
                            color = OguriTheme.colors.textPrimary,
                        )
                    }
                    if (showMap && !mapUnavailable && scene.markers.isNotEmpty()) {
                        TravelTextButton(onClick = onFitMap) { Text(stringResource(Res.string.travel_map_fit)) }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().onSizeChanged { statusHeight = it.height }) {
                status()
            }
            content()
        }
    }
}
