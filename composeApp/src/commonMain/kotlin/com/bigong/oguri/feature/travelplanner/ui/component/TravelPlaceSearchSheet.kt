package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlaceAttribution
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapMarker
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapScene
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_add_again
import oguri.composeapp.generated.resources.travel_add_to_day
import oguri.composeapp.generated.resources.travel_add_visit
import oguri.composeapp.generated.resources.travel_add_without_date
import oguri.composeapp.generated.resources.travel_google_maps_attribution
import oguri.composeapp.generated.resources.travel_manual_add
import oguri.composeapp.generated.resources.travel_place_added
import oguri.composeapp.generated.resources.travel_reload
import oguri.composeapp.generated.resources.travel_search_city
import oguri.composeapp.generated.resources.travel_search_close
import oguri.composeapp.generated.resources.travel_search_empty
import oguri.composeapp.generated.resources.travel_search_places
import oguri.composeapp.generated.resources.travel_search_results
import oguri.composeapp.generated.resources.travel_show_existing_visit
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelPlaceSearchSheet(
    state: TravelPlannerUiState,
    plan: TravelPlan,
    onQuery: (String) -> Unit,
    onSelect: (TravelPlace?) -> Unit,
    onAdd: (TravelPlace, Int) -> Unit,
    onShowVisit: (TravelVisit) -> Unit,
    onDismiss: () -> Unit,
    onRetrySave: () -> Unit,
    onLeaveWithPendingSave: () -> Unit,
    onReload: () -> Unit,
    onManualAdd: () -> Unit,
) {
    val search = state.placeSearch ?: return
    val canDismiss by rememberUpdatedState(!state.isSaving && !state.canRetrySave)
    val canEdit = canDismiss && !state.isLoading && !state.needsLatestPlan && !state.isOffline
    val focusManager = LocalFocusManager.current
    val selected = search.selected
    val day = state.selectedDay
    val dates = plan.summary.details.dates
    val sheet =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { it != SheetValue.Hidden || canDismiss },
        )
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 16.dp)) {
            Text(stringResource(Res.string.travel_add_visit), style = MaterialTheme.typography.titleLarge)
            Text(travelDayLabel(dates, day), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = search.query,
                onValueChange = { if (it.length <= 120) onQuery(it) },
                label = { Text(stringResource(Res.string.travel_search_places)) },
                supportingText = { Text(stringResource(Res.string.travel_search_city, plan.summary.details.city)) },
                singleLine = true,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
            )
            if (search.isLoading || state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (search.failure != null) {
                TravelInlineFailure(search.failure, onRetry = if (canEdit) ({ onQuery(search.query) }) else null)
            } else if (!search.isLoading && search.query.trim().length >= 2 && search.results.isEmpty()) {
                Text(stringResource(Res.string.travel_search_empty), modifier = Modifier.padding(vertical = 8.dp))
            }
            state.failure?.let {
                if (state.needsLatestPlan) {
                    TravelInlineFailure(it, onReload, stringResource(Res.string.travel_reload))
                } else {
                    TravelInlineFailure(it, onRetry = onRetrySave.takeIf { state.canRetrySave })
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (selected == null) {
                    items(search.results, key = TravelPlace::id) { place ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canEdit) {
                                    focusManager.clearFocus()
                                    onSelect(place)
                                }.padding(vertical = 8.dp),
                        ) {
                            Text(place.name, style = MaterialTheme.typography.titleMedium)
                            place.address?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            if (place.id == search.lastAddedPlaceId) Text(stringResource(Res.string.travel_place_added))
                        }
                        HorizontalDivider()
                    }
                } else {
                    item {
                        val existing = plan.visits.firstOrNull { it.dayNumber == day && it.details.googlePlaceId == selected.id }
                        TextButton(
                            onClick = { onSelect(null) },
                            enabled = canEdit,
                        ) { Text(stringResource(Res.string.travel_search_results)) }
                        Text(selected.name, style = MaterialTheme.typography.titleMedium)
                        selected.address?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        val coordinates = selected.coordinates
                        if (coordinates != null) {
                            var available by remember(selected.id) { mutableStateOf(true) }
                            if (available) {
                                key(selected.id) {
                                    PlatformTravelMap(
                                        scene =
                                            TravelMapScene(
                                                listOf(TravelMapMarker(selected.id, coordinates, null, false)),
                                                emptyList(),
                                                null,
                                            ),
                                        viewportRequest = 0,
                                        focusedVisitId = null,
                                        onMarkerClick = {},
                                        onUnavailable = { available = false },
                                        modifier = Modifier.fillMaxWidth().height(144.dp),
                                    )
                                }
                            }
                        }
                        if (existing != null) {
                            Button(onClick = { onShowVisit(existing) }, enabled = canEdit) {
                                Text(stringResource(Res.string.travel_show_existing_visit))
                            }
                            TextButton(onClick = { onAdd(selected, day) }, enabled = canEdit) {
                                Text(stringResource(Res.string.travel_add_again))
                            }
                        } else {
                            Button(onClick = { onAdd(selected, day) }, enabled = canEdit) {
                                val label =
                                    if (day == TravelPlan.UNASSIGNED_DAY) {
                                        stringResource(Res.string.travel_add_without_date)
                                    } else {
                                        stringResource(Res.string.travel_add_to_day, day)
                                    }
                                Text(label)
                            }
                        }
                        if (day != TravelPlan.UNASSIGNED_DAY) {
                            TextButton(onClick = { onAdd(selected, TravelPlan.UNASSIGNED_DAY) }, enabled = canEdit) {
                                Text(stringResource(Res.string.travel_add_without_date))
                            }
                        }
                    }
                }
            }
            if (search.results.isNotEmpty()) {
                GooglePlacesAttribution(
                    selected?.attributions ?: search.results.flatMap(TravelPlace::attributions),
                )
            }
            if (state.canRetrySave && state.pendingSaveStored && !state.isSaving) {
                TravelLeavePendingSave(onLeaveWithPendingSave)
            }
            Row {
                TextButton(onClick = onManualAdd, enabled = canEdit) { Text(stringResource(Res.string.travel_manual_add)) }
                TextButton(onClick = onDismiss, enabled = canDismiss) { Text(stringResource(Res.string.travel_search_close)) }
            }
        }
    }
}

@Composable
internal fun GooglePlacesAttribution(attributions: List<TravelPlaceAttribution>) {
    val uriHandler = LocalUriHandler.current
    val textColor = if (com.bigong.oguri.core.designsystem.OguriTheme.isDarkTheme) Color.White else Color(0xFF5E5E5E)
    Column(Modifier.fillMaxWidth().heightIn(min = 24.dp).padding(vertical = 4.dp)) {
        Text(
            stringResource(Res.string.travel_google_maps_attribution),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            softWrap = false,
        )
        attributions.distinct().forEach { attribution ->
            val uri = attribution.providerUri
            if (uri == null) {
                Text(attribution.provider, style = MaterialTheme.typography.bodySmall)
            } else {
                TextButton(onClick = { runCatching { uriHandler.openUri(uri) } }) { Text(attribution.provider) }
            }
        }
    }
}
