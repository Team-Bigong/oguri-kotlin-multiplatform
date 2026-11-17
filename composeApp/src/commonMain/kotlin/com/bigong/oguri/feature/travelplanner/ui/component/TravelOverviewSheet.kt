package com.bigong.oguri.feature.travelplanner.ui.component

import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.ic_travel_close
import oguri.composeapp.generated.resources.travel_search_close
import oguri.composeapp.generated.resources.travel_place_count
import oguri.composeapp.generated.resources.travel_day_number
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.HorizontalDivider
import oguri.composeapp.generated.resources.travel_total_place_count
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
import com.bigong.oguri.feature.travelplanner.ui.model.TravelOverviewState
import com.bigong.oguri.feature.travelplanner.ui.model.travelDayColor
import com.bigong.oguri.feature.travelplanner.ui.model.travelMapLabelColor
import com.bigong.oguri.feature.travelplanner.ui.model.travelOverviewScene
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_retry
import oguri.composeapp.generated.resources.travel_add_visit
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_choose_day
import oguri.composeapp.generated.resources.travel_empty_day
import oguri.composeapp.generated.resources.travel_map_hide
import oguri.composeapp.generated.resources.travel_map_show
import oguri.composeapp.generated.resources.travel_map_unavailable
import oguri.composeapp.generated.resources.travel_overlapping_visits
import oguri.composeapp.generated.resources.travel_overview
import oguri.composeapp.generated.resources.travel_overview_legend
import oguri.composeapp.generated.resources.travel_overview_map_fit
import oguri.composeapp.generated.resources.travel_place_details_unavailable
import oguri.composeapp.generated.resources.travel_visit_fallback
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelOverviewSheet(
    plan: TravelPlan,
    state: TravelOverviewState,
    canAdd: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onSelectVisit: (String) -> Unit,
    onAdd: (Int) -> Unit,
) {
    val dates = plan.summary.details.dates
    val scene =
        remember(plan.visits, state.places, plan.summary.details) {
            val details = plan.summary.details
            travelOverviewScene(plan.visits, state.places, details.catalogCities[details.city]?.coordinates ?: TravelCity.centerOf(details.city))
        }
    val startWithMap = LocalDensity.current.fontScale <= 1.3f
    var showMap by remember { mutableStateOf(startWithMap) }
    var mapUnavailable by remember { mutableStateOf(false) }
    var viewportRequest by remember { mutableIntStateOf(0) }
    var initialFitDone by remember { mutableStateOf(false) }
    LaunchedEffect(state.loading) {
        if (!state.loading && !initialFitDone) {
            initialFitDone = true
            viewportRequest++
        }
    }
    var overlapping by remember(plan.revision) { mutableStateOf<List<String>>(emptyList()) }
    var addDay by remember(dates) { mutableStateOf<Int?>(null) }
    var chooseDay by remember { mutableStateOf(false) }
    val title: @Composable (TravelVisit) -> String = { visit ->
        state.places[visit.details.googlePlaceId]?.name ?: visit.details.name
            ?: stringResource(Res.string.travel_visit_fallback, plan.visits.indexOf(visit) + 1)
    }
    ModalBottomSheet(scrimColor = TravelSheetScrim,
        dragHandle = { TravelSheetHandle() },
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = OguriTheme.colors.backgroundSecondary,
        contentColor = OguriTheme.colors.textPrimary,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    ) {
        Column(Modifier.fillMaxHeight(0.92f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth().height(44.dp).padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.travel_overview), style = OguriTheme.typography.cardTitle)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(Res.string.travel_total_place_count, plan.visits.size), style = OguriTheme.typography.labelMedium, color = OguriTheme.colors.textTertiary)
                Spacer(Modifier.weight(1f))
                TravelIconButton(onClick = onDismiss) { androidx.compose.material3.Icon(org.jetbrains.compose.resources.painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close), Modifier.size(22.dp)) }
            }
            if (showMap) {
                if (mapUnavailable) {
                    Text(stringResource(Res.string.travel_map_unavailable))
                    TravelTextButton(onClick = { mapUnavailable = false }) { Text(stringResource(Res.string.travel_retry)) }
                } else {
                    PlatformTravelMap(
                        scene = scene,
                        viewportRequest = viewportRequest,
                        focusedVisitId = null,
                        onMarkerClick = { id ->
                            val coordinates = scene.markers.firstOrNull { it.id == id }?.coordinates
                            val ids = scene.markers.filter { it.coordinates == coordinates }.map { it.id }
                            onSelectVisit(id)
                        },
                        onUnavailable = { mapUnavailable = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(170.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, OguriTheme.colors.borderSubtle, RoundedCornerShape(8.dp)),
                    )
                    if (scene.markers.isNotEmpty()) {
                        androidx.compose.foundation.layout.FlowRow(Modifier.fillMaxWidth().heightIn(min = 22.dp).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            plan.visits.map { it.dayNumber }.distinct().sorted().forEach { day ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(Modifier.size(10.dp).background(Color(travelDayColor(day)), CircleShape))
                                    Text(stringResource(Res.string.travel_day_number, day), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                                }
                            }
                        }
                    }
                }
            }
            if (state.loading) TravelLoading(Modifier.fillMaxWidth())
            if (state.incomplete) {
                Row {
                    Text(stringResource(Res.string.travel_place_details_unavailable), Modifier.weight(1f))
                    TravelTextButton(onClick = onRetry) { Text(stringResource(Res.string.travel_retry)) }
                }
            }
            LazyColumn(Modifier.weight(1f)) {
                val groups = plan.visits.groupBy { it.dayNumber }
                if (groups.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.travel_empty_day),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                            color = OguriTheme.colors.textTertiary,
                            style = travelTextStyle(14, 21),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                groups.entries
                    .sortedBy {
                        if (it.key ==
                            TravelPlan.UNASSIGNED_DAY
                        ) {
                            dates.dayCount + 1
                        } else {
                            it.key
                        }
                    }.forEach { (day, visits) ->
                        item(key = "day-$day") {
                            HorizontalDivider(color = OguriTheme.colors.borderSubtle)
                            Row(Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(travelDayLabel(dates, day), Modifier.weight(1f), style = OguriTheme.typography.cardSubtitle)
                                Text(stringResource(Res.string.travel_place_count, visits.size), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                            }
                        }
                        items(visits, key = { it.id }) { visit ->
                            Row(Modifier.fillMaxWidth().noRippleClickable(onClick = { onSelectVisit(visit.id) }).height(50.dp).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                val dayColor = travelDayColor(day)
                                Box(Modifier.size(24.dp).background(Color(dayColor), CircleShape), contentAlignment = Alignment.Center) {
                                    Text((visits.indexOf(visit) + 1).toString(), style = OguriTheme.typography.labelMedium, color = travelMapLabelColor(dayColor))
                                }
                                Text(title(visit), Modifier.weight(1f), style = OguriTheme.typography.cardSubtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                visit.details.category?.let { Text(stringResource(it.label()), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary) }
                            }
                        }
                    }
                val attributions = state.places.values.flatMap { it.attributions }
                if (attributions.isNotEmpty() || ((!showMap || mapUnavailable) && state.places.isNotEmpty())) item {
                    Box(Modifier.padding(horizontal = 20.dp)) { GooglePlacesAttribution(attributions, includeGoogleBranding = !showMap || mapUnavailable) }
                }
            }
        }
    }
}
