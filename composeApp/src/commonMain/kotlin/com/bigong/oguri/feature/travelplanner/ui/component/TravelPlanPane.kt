package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapScene
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_retry
import oguri.composeapp.generated.resources.ic_travel_chevron_down
import oguri.composeapp.generated.resources.ic_travel_chevron_up
import oguri.composeapp.generated.resources.ic_travel_map
import oguri.composeapp.generated.resources.travel_map_fit
import oguri.composeapp.generated.resources.travel_map_hide
import oguri.composeapp.generated.resources.travel_map_sequence
import oguri.composeapp.generated.resources.travel_map_show
import oguri.composeapp.generated.resources.travel_map_list_available
import oguri.composeapp.generated.resources.travel_map_unavailable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val MAP_EXPAND_MILLIS = 250
private const val MAP_COLLAPSE_MILLIS = 200

@Composable
internal fun TravelPlanPane(
    scene: TravelMapScene,
    viewportRequest: Int,
    focusedVisitId: String?,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hideMap: Boolean = false,
    status: @Composable () -> Unit,
    content: @Composable ColumnScope.(Boolean) -> Unit,
) {
    val density = LocalDensity.current
    var mapExpanded by rememberSaveable { mutableStateOf(true) }
    var statusHeight by remember(density) { mutableIntStateOf(0) }
    var legendHeight by remember(density) { mutableIntStateOf(with(density) { 32.dp.roundToPx() }) }
    var mapUnavailable by remember { mutableStateOf(false) }
    var mapAttempt by remember { mutableIntStateOf(0) }
    BoxWithConstraints(modifier) {
        val chromeHeight = with(density) {
            (statusHeight + if (scene.lines.isNotEmpty()) legendHeight else 0).toDp()
        }
        val maxMapHeight = (maxHeight - chromeHeight - (112 * density.fontScale).dp).coerceAtLeast(0.dp)
        val canShowMap = !hideMap && maxMapHeight >= 120.dp
        val showMap = canShowMap && mapExpanded
        val mapHeight = ((maxWidth - TravelMapHorizontalPadding * 2).coerceAtLeast(0.dp) / TRAVEL_MAP_ASPECT_RATIO)
            .coerceAtMost(maxMapHeight)
        Column(Modifier.fillMaxSize()) {
            HorizontalDivider(color = OguriTheme.colors.borderSubtle)
            Spacer(Modifier.height(16.dp))
            if (canShowMap) {
                AnimatedVisibility(
                    visible = mapExpanded,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(MAP_EXPAND_MILLIS, easing = LinearOutSlowInEasing),
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(MAP_COLLAPSE_MILLIS, easing = FastOutLinearInEasing),
                    ),
                ) {
                    Column {
                        if (mapUnavailable) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = TravelMapHorizontalPadding).height(mapHeight)
                                .background(OguriTheme.colors.backgroundTertiary, RoundedCornerShape(8.dp))
                                .border(1.dp, OguriTheme.colors.borderSubtle, RoundedCornerShape(8.dp)),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(stringResource(Res.string.travel_map_unavailable), style = OguriTheme.typography.cardSubtitle)
                                Text(stringResource(Res.string.travel_map_list_available), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TravelTextButton(enabled = showMap, onClick = { mapAttempt++; mapUnavailable = false }) {
                                        Text(stringResource(Res.string.travel_retry))
                                    }
                                    TravelTextButton(enabled = showMap, onClick = { mapExpanded = false }) {
                                        Text(stringResource(Res.string.travel_map_hide))
                                    }
                                }
                            }
                        } else {
                            val shape = RoundedCornerShape(8.dp)
                            Box(Modifier.fillMaxWidth().padding(horizontal = TravelMapHorizontalPadding).height(mapHeight)
                                .clip(shape).border(1.dp, OguriTheme.colors.borderSubtle, shape)
                            ) {
                                key(mapAttempt) {
                                    PlatformTravelMap(
                                        scene = scene,
                                        viewportRequest = viewportRequest,
                                        focusedVisitId = focusedVisitId,
                                        onMarkerClick = onMarkerClick,
                                        onUnavailable = { mapUnavailable = true },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                Box(
                                    Modifier.align(Alignment.TopEnd).padding(end = 8.dp).heightIn(min = 44.dp)
                                        .noRippleClickable(enabled = showMap, onClick = { mapExpanded = false }),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        Modifier.heightIn(min = 30.dp).background(OguriTheme.colors.surfaceCard, CircleShape)
                                            .border(1.dp, OguriTheme.colors.borderSubtle, CircleShape).padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(stringResource(Res.string.travel_map_hide), style = travelTextStyle(12, 14), color = OguriTheme.colors.textSecondary)
                                        Icon(painterResource(Res.drawable.ic_travel_chevron_up), null, Modifier.size(14.dp), tint = OguriTheme.colors.textSecondary)
                                    }
                                }
                            }
                            if (scene.lines.isNotEmpty()) {
                                Text(
                                    stringResource(Res.string.travel_map_sequence),
                                    modifier = Modifier.fillMaxWidth().onSizeChanged { legendHeight = it.height }
                                        .padding(horizontal = 24.dp, vertical = 4.dp),
                                    color = OguriTheme.colors.textSecondary,
                                    style = OguriTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !mapExpanded,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(MAP_COLLAPSE_MILLIS, easing = FastOutLinearInEasing),
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(MAP_EXPAND_MILLIS, easing = LinearOutSlowInEasing),
                    ),
                ) {
                    val shape = RoundedCornerShape(8.dp)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(min = 44.dp)
                            .background(OguriTheme.colors.surfaceCard, shape)
                            .border(1.dp, OguriTheme.colors.borderSubtle, shape)
                            .noRippleClickable(enabled = !showMap, onClick = { mapExpanded = true }),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(painterResource(Res.drawable.ic_travel_map), null, Modifier.size(18.dp), tint = OguriTheme.colors.mint)
                        Text(
                            stringResource(Res.string.travel_map_show),
                            style = travelTextStyle(13, 16), color = OguriTheme.colors.textSecondary,
                        )
                        Icon(painterResource(Res.drawable.ic_travel_chevron_down), null, Modifier.size(16.dp), tint = OguriTheme.colors.textSecondary)
                    }
                }
            }
            Column(Modifier.fillMaxWidth().onSizeChanged { statusHeight = it.height }) {
                status()
            }
            content(showMap && !mapUnavailable)
        }
    }
}
