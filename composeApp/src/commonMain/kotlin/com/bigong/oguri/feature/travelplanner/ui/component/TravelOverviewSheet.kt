package com.bigong.oguri.feature.travelplanner.ui.component

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
import com.bigong.oguri.feature.travelplanner.ui.model.TravelOverviewState
import com.bigong.oguri.feature.travelplanner.ui.model.travelDayColor
import com.bigong.oguri.feature.travelplanner.ui.model.travelOverviewScene
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_retry_button
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
        remember(plan.visits, state.places, plan.summary.details.city) {
            travelOverviewScene(plan.visits, state.places, TravelCity.centerOf(plan.summary.details.city))
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Neutral0,
    ) {
        Column(Modifier.fillMaxHeight(0.92f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.travel_overview), style = OguriTheme.typography.sectionTitle)
            Row {
                TravelTextButton(onClick = { showMap = !showMap }) {
                    Text(
                        stringResource(if (showMap) Res.string.travel_map_hide else Res.string.travel_map_show),
                        color = OguriTheme.colors.textPrimary,
                    )
                }
                if (showMap && !mapUnavailable && scene.markers.isNotEmpty()) {
                    TravelTextButton(onClick = { viewportRequest++ }) {
                        Text(stringResource(Res.string.travel_overview_map_fit), color = OguriTheme.colors.textPrimary)
                    }
                }
            }
            if (showMap) {
                if (mapUnavailable) {
                    Text(stringResource(Res.string.travel_map_unavailable))
                    TravelTextButton(onClick = { mapUnavailable = false }) { Text(stringResource(Res.string.common_retry_button)) }
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
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                    )
                    if (scene.markers.isNotEmpty()) {
                        Text(stringResource(Res.string.travel_overview_legend), style = OguriTheme.typography.bodySmall)
                    }
                }
            }
            if (state.loading) TravelLoading(Modifier.fillMaxWidth())
            if (state.incomplete) {
                Row {
                    Text(stringResource(Res.string.travel_place_details_unavailable), Modifier.weight(1f))
                    TravelTextButton(onClick = onRetry) { Text(stringResource(Res.string.common_retry_button)) }
                }
            }
            LazyColumn(Modifier.weight(1f)) {
                val groups = plan.visits.groupBy { it.dayNumber }
                if (groups.isEmpty()) item { Text(stringResource(Res.string.travel_empty_day)) }
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
                            Row(
                                modifier = Modifier.padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(10.dp).background(Color(travelDayColor(day)), CircleShape))
                                Text(travelDayLabel(dates, day), style = OguriTheme.typography.cardSubtitle)
                            }
                        }
                        items(visits, key = { it.id }) { visit ->
                            TravelTextButton(onClick = { onSelectVisit(visit.id) }, modifier = Modifier.fillMaxWidth()) {
                                Text(title(visit), modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                if (state.places.isNotEmpty()) item { GooglePlacesAttribution(state.places.values.flatMap { it.attributions }) }
            }
            if (canAdd) {
                Box {
                    TravelOutlineButton(onClick = { chooseDay = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(addDay?.let { travelDayLabel(dates, it) } ?: stringResource(Res.string.travel_choose_day))
                    }
                    DropdownMenu(expanded = chooseDay, onDismissRequest = { chooseDay = false }) {
                        for (day in TravelPlan.UNASSIGNED_DAY..dates.dayCount) {
                            DropdownMenuItem(
                                text = { Text(travelDayLabel(dates, day)) },
                                onClick = {
                                    addDay = day
                                    chooseDay = false
                                },
                            )
                        }
                    }
                }
                TravelPrimaryButton(enabled = addDay != null, onClick = { addDay?.let(onAdd) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.travel_add_visit))
                }
            }
            TravelTextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.travel_cancel)) }
        }
    }
}
