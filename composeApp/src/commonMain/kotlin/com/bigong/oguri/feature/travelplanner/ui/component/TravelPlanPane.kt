package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapScene
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.ic_arrow_up
import oguri.composeapp.generated.resources.travel_list_expand
import oguri.composeapp.generated.resources.travel_map_expand
import oguri.composeapp.generated.resources.travel_map_fit
import oguri.composeapp.generated.resources.travel_map_sequence
import oguri.composeapp.generated.resources.travel_map_show
import oguri.composeapp.generated.resources.travel_map_unavailable
import oguri.composeapp.generated.resources.travel_pane_list
import oguri.composeapp.generated.resources.travel_pane_map
import oguri.composeapp.generated.resources.travel_pane_split
import org.jetbrains.compose.resources.painterResource
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
    var savedMode by rememberSaveable { mutableIntStateOf(-1) }
    var controlsHeight by remember(density) { mutableIntStateOf(with(density) { 48.dp.roundToPx() }) }
    var handleHeight by remember(density) { mutableIntStateOf(with(density) { 48.dp.roundToPx() }) }
    var statusHeight by remember(density) { mutableIntStateOf(0) }
    var legendHeight by remember(density) { mutableIntStateOf(with(density) { 32.dp.roundToPx() }) }
    var mapUnavailable by remember { mutableStateOf(false) }
    var mapAttempt by remember { mutableIntStateOf(0) }
    BoxWithConstraints(modifier) {
        val chromeHeight =
            with(density) {
                (controlsHeight + handleHeight + statusHeight + if (scene.lines.isNotEmpty()) legendHeight else 0).toDp()
            }
        val maxMapHeight = (maxHeight - chromeHeight - (96 * density.fontScale).dp).coerceAtLeast(0.dp)
        val canShowMap = maxMapHeight >= 120.dp
        val defaultMode = if (density.fontScale > 1.3f || !canShowMap) LIST else SPLIT
        val mode = if (canShowMap) savedMode.takeIf { it in LIST..MAP } ?: defaultMode else LIST
        val showMap = mode != LIST
        val mapHeight = if (mode == MAP) maxMapHeight else 180.dp.coerceAtMost(maxMapHeight)
        Column(Modifier.fillMaxSize()) {
            if (showMap) {
                Column(Modifier.fillMaxWidth().onSizeChanged { controlsHeight = it.height }) {
                    if (mapUnavailable) {
                        Text(stringResource(Res.string.travel_map_unavailable), Modifier.padding(horizontal = 16.dp))
                        TextButton(onClick = {
                            mapAttempt++
                            mapUnavailable = false
                        }) {
                            Text(stringResource(Res.string.common_retry_button))
                        }
                    } else {
                        TextButton(onClick = onFitMap, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text(stringResource(Res.string.travel_map_fit))
                        }
                    }
                }
                if (!mapUnavailable) {
                    key(mapAttempt) {
                        PlatformTravelMap(
                            scene = scene,
                            viewportRequest = viewportRequest,
                            focusedVisitId = focusedVisitId,
                            onMarkerClick = onMarkerClick,
                            onUnavailable = { mapUnavailable = true },
                            modifier = Modifier.fillMaxWidth().height(mapHeight),
                        )
                    }
                }
            }
            if (showMap && !mapUnavailable && scene.lines.isNotEmpty()) {
                Text(
                    stringResource(Res.string.travel_map_sequence),
                    modifier =
                        Modifier.fillMaxWidth().onSizeChanged { legendHeight = it.height }.padding(
                            horizontal = 16.dp,
                            vertical = 4.dp,
                        ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            TravelPaneHandle(
                mode = mode,
                canShowMap = canShowMap,
                onMode = { savedMode = it },
                modifier = Modifier.onSizeChanged { handleHeight = maxOf(handleHeight, it.height) },
            )
            Column(Modifier.fillMaxWidth().onSizeChanged { statusHeight = it.height }) {
                status()
            }
            content()
        }
    }
}

@Composable
private fun TravelPaneHandle(
    mode: Int,
    canShowMap: Boolean,
    onMode: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateLabel =
        stringResource(
            when (mode) {
                LIST -> Res.string.travel_pane_list
                MAP -> Res.string.travel_pane_map
                else -> Res.string.travel_pane_split
            },
        )
    val onStep by rememberUpdatedState<(Int) -> Unit> { step -> onMode((mode + step).coerceIn(LIST, MAP)) }
    val density = LocalDensity.current
    var dragged by remember { mutableFloatStateOf(0f) }
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(enabled = mode > LIST, onClick = { onStep(-1) }, modifier = Modifier.size(48.dp)) {
            Icon(painterResource(Res.drawable.ic_arrow_up), stringResource(Res.string.travel_list_expand))
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics { stateDescription = stateLabel }
                    .clickable(enabled = canShowMap, role = Role.Button) { onMode(if (mode == LIST) SPLIT else LIST) }
                    .pointerInput(canShowMap, density) {
                        if (canShowMap) {
                            detectVerticalDragGestures(
                                onDragStart = { dragged = 0f },
                                onDragCancel = { dragged = 0f },
                                onDragEnd = {
                                    val threshold = with(density) { 32.dp.toPx() }
                                    if (dragged <= -threshold) {
                                        onStep(-1)
                                    } else if (dragged >= threshold) {
                                        onStep(1)
                                    }
                                    dragged = 0f
                                },
                            ) { change, delta ->
                                change.consume()
                                dragged += delta
                            }
                        }
                    }.padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Box(Modifier.size(width = 32.dp, height = 4.dp).background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)))
            Text(
                stringResource(if (mode == LIST) Res.string.travel_map_show else Res.string.travel_list_expand),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        IconButton(enabled = canShowMap && mode < MAP, onClick = { onStep(1) }, modifier = Modifier.size(48.dp)) {
            Icon(painterResource(Res.drawable.ic_arrow_up), stringResource(Res.string.travel_map_expand), Modifier.rotate(180f))
        }
    }
}

private const val LIST: Int = 0
private const val SPLIT: Int = 1
private const val MAP: Int = 2
