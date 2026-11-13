package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.feature.travelplanner.ui.component.GooglePlacesAttribution
import com.bigong.oguri.feature.travelplanner.ui.component.PlatformTravelMap
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoadFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelVisitEditor
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.model.hasEnded
import com.bigong.oguri.feature.travelplanner.ui.model.travelMapScene
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_menu
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.travel_add_visit
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_day
import oguri.composeapp.generated.resources.travel_day_outside_trip
import oguri.composeapp.generated.resources.travel_empty_day
import oguri.composeapp.generated.resources.travel_find_next_holiday
import oguri.composeapp.generated.resources.travel_has_note
import oguri.composeapp.generated.resources.travel_list
import oguri.composeapp.generated.resources.travel_manage
import oguri.composeapp.generated.resources.travel_map_fit
import oguri.composeapp.generated.resources.travel_map_hide
import oguri.composeapp.generated.resources.travel_map_sequence
import oguri.composeapp.generated.resources.travel_map_show
import oguri.composeapp.generated.resources.travel_map_unavailable
import oguri.composeapp.generated.resources.travel_move_down
import oguri.composeapp.generated.resources.travel_move_up
import oguri.composeapp.generated.resources.travel_move_visit
import oguri.composeapp.generated.resources.travel_offline_snapshot
import oguri.composeapp.generated.resources.travel_overlapping_visits
import oguri.composeapp.generated.resources.travel_place_details_unavailable
import oguri.composeapp.generated.resources.travel_reload
import oguri.composeapp.generated.resources.travel_sign_in
import oguri.composeapp.generated.resources.travel_unassigned
import oguri.composeapp.generated.resources.travel_undo_visit
import oguri.composeapp.generated.resources.travel_visit_fallback
import oguri.composeapp.generated.resources.travel_visit_number
import oguri.composeapp.generated.resources.travel_visit_selected
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
internal fun TravelPlanScreen(
    state: TravelPlannerUiState,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onRetrySave: () -> Unit,
    onUndoVisit: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onOpenVisit: (TravelVisit?) -> Unit,
    onMoveVisit: (TravelVisit, Int) -> Unit,
    onReorderVisits: (List<String>) -> Unit,
    onDraftChange: (TravelVisitDraft?) -> Unit,
    onApplyVisit: () -> Unit,
    onRemoveVisit: () -> Unit,
    onSearchPlaces: () -> Unit,
    onMarkerClick: (String) -> Unit,
    onFitMap: () -> Unit,
    onFindNextHoliday: () -> Unit,
    onMembers: () -> Unit,
    onSignIn: () -> Unit,
    onRetryPlaces: () -> Unit,
) {
    val plan = state.plan
    val failure = state.failure
    val offlineMessage = stringResource(Res.string.travel_offline_snapshot)
    TravelPlannerPage(
        title = plan?.summary?.details?.title ?: stringResource(Res.string.travel_list),
        onBack = onBack,
        isSaving = state.isSaving,
        actions = {
            if (plan != null) {
                TextButton(onClick = onMembers, enabled = !state.isSaving && !state.canRetrySave) {
                    Text(stringResource(Res.string.travel_manage))
                }
            }
        },
    ) {
        if (plan == null) {
            if (failure == TravelPlanFailure.SIGN_IN_REQUIRED) {
                TravelInlineFailure(failure, onSignIn, stringResource(Res.string.travel_sign_in))
            } else if (failure != null) {
                TravelLoadFailure(failure, onReload)
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            return@TravelPlannerPage
        }

        val dates = plan.summary.details.dates
        val savedVisits = plan.visits.filter { it.dayNumber == state.selectedDay }
        var dragOrder by remember(state.selectedDay, plan.revision) { mutableStateOf<List<String>?>(null) }
        LaunchedEffect(state.failure) {
            if (state.failure != null) dragOrder = null
        }
        val visits =
            dragOrder?.let { order ->
                val byId = savedVisits.associateBy(TravelVisit::id)
                order.mapNotNull(byId::get)
            } ?: savedVisits
        val canEdit = !state.isSaving && !state.isLoading && !state.canRetrySave && !state.needsLatestPlan && !state.isOffline
        if (state.isOffline) {
            Text(offlineMessage, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
        val dayListState = rememberLazyListState()
        LaunchedEffect(state.selectedDay) {
            val index = if (state.selectedDay == TravelPlan.UNASSIGNED_DAY) dates.dayCount else state.selectedDay - 1
            if (dayListState.layoutInfo.visibleItemsInfo.none { it.index == index }) dayListState.scrollToItem(index)
        }

        LazyRow(
            state = dayListState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(dates.dayCount) { index ->
                val dayNumber = index + 1
                FilterChip(
                    selected = state.selectedDay == dayNumber,
                    onClick = { onSelectDay(dayNumber) },
                    enabled = !state.isSaving && !state.canRetrySave,
                    label = { Text(travelDayLabel(dates, dayNumber)) },
                )
            }
            item {
                FilterChip(
                    selected = state.selectedDay == TravelPlan.UNASSIGNED_DAY,
                    onClick = { onSelectDay(TravelPlan.UNASSIGNED_DAY) },
                    enabled = !state.isSaving && !state.canRetrySave,
                    label = { Text(travelDayLabel(dates, TravelPlan.UNASSIGNED_DAY)) },
                )
            }
        }

        val cityCenter = TravelCity.centerOf(plan.summary.details.city)
        val scene =
            remember(state.selectedDay, visits, state.dayPlaces, state.selectedVisitId, cityCenter) {
                travelMapScene(state.selectedDay, visits, state.dayPlaces, state.selectedVisitId, cityCenter)
            }
        val startWithMap = LocalDensity.current.fontScale <= 1.3f
        var showMap by rememberSaveable { mutableStateOf(startWithMap) }
        var mapUnavailable by remember { mutableStateOf(false) }
        var mapAttempt by remember { mutableStateOf(0) }
        val mapFocus = state.mapFocusedVisitId
        var overlappingVisits by remember(state.selectedDay) { mutableStateOf<List<String>>(emptyList()) }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            TextButton(onClick = { showMap = !showMap }) {
                Text(stringResource(if (showMap) Res.string.travel_map_hide else Res.string.travel_map_show))
            }
            if (showMap && !mapUnavailable) {
                TextButton(onClick = onFitMap) { Text(stringResource(Res.string.travel_map_fit)) }
            }
        }
        if (showMap) {
            if (mapUnavailable) {
                Text(stringResource(Res.string.travel_map_unavailable), Modifier.padding(horizontal = 16.dp))
                TextButton(onClick = {
                    mapAttempt++
                    mapUnavailable = false
                }) {
                    Text(stringResource(Res.string.common_retry_button))
                }
            } else {
                key(mapAttempt) {
                    PlatformTravelMap(
                        scene = scene,
                        viewportRequest = state.viewportRequest,
                        focusedVisitId = mapFocus,
                        onMarkerClick = { visitId ->
                            val coordinates = scene.markers.firstOrNull { it.id == visitId }?.coordinates
                            val atPoint = scene.markers.filter { it.coordinates == coordinates }.map { it.id }
                            if (atPoint.size > 1) overlappingVisits = atPoint else onMarkerClick(visitId)
                        },
                        onUnavailable = { mapUnavailable = true },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                    )
                }
                if (scene.lines.isNotEmpty()) {
                    Text(
                        stringResource(Res.string.travel_map_sequence),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        if (overlappingVisits.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { overlappingVisits = emptyList() },
                title = { Text(stringResource(Res.string.travel_overlapping_visits)) },
                text = {
                    LazyColumn(Modifier.heightIn(max = 240.dp)) {
                        itemsIndexed(visits) { index, visit ->
                            if (visit.id in overlappingVisits) {
                                TextButton(onClick = {
                                    overlappingVisits = emptyList()
                                    onMarkerClick(visit.id)
                                }) {
                                    val title = state.dayPlaces[visit.details.googlePlaceId]?.name ?: visit.details.name
                                    Text(
                                        if (title == null) {
                                            stringResource(Res.string.travel_visit_fallback, index + 1)
                                        } else {
                                            stringResource(Res.string.travel_visit_number, index + 1, title)
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { overlappingVisits = emptyList() }) {
                        Text(stringResource(Res.string.travel_cancel))
                    }
                },
            )
        }
        if (state.isLoadingDayPlaces) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.dayPlacesIncomplete) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(stringResource(Res.string.travel_place_details_unavailable), Modifier.weight(1f))
                TextButton(onClick = onRetryPlaces) { Text(stringResource(Res.string.common_retry_button)) }
            }
        }

        if (failure != null && state.visitDraft == null && state.placeSearch == null) {
            TravelInlineFailure(
                failure = failure,
                onRetry =
                    when {
                        state.canRetrySave -> onRetrySave
                        failure == TravelPlanFailure.CONFLICT || failure == TravelPlanFailure.CONNECTION -> onReload
                        else -> null
                    },
                retryLabel =
                    if (state.canRetrySave) {
                        stringResource(Res.string.common_retry_button)
                    } else {
                        stringResource(Res.string.travel_reload)
                    },
            )
        }

        if (state.visitUndo != null && state.visitDraft == null && state.placeSearch == null) {
            TextButton(onClick = onUndoVisit, enabled = canEdit, modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(stringResource(Res.string.travel_undo_visit))
            }
        }

        val visitListState = rememberLazyListState()
        var draggedId by remember(state.selectedDay, plan.revision) { mutableStateOf<String?>(null) }
        var dragPosition by remember(state.selectedDay, plan.revision) { mutableStateOf(0f) }
        LaunchedEffect(state.listFocusRequest) {
            val index = visits.indexOfFirst { it.id == state.selectedVisitId }
            if (state.listFocusRequest > 0 && index >= 0) visitListState.scrollToItem(index)
        }
        LazyColumn(
            state = visitListState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (visits.isEmpty()) {
                item { Text(stringResource(Res.string.travel_empty_day)) }
            }
            itemsIndexed(visits, key = { _, visit -> visit.id }) { index, visit ->
                TravelVisitRow(
                    visit = visit,
                    title =
                        state.dayPlaces[visit.details.googlePlaceId]?.name ?: visit.details.name
                            ?: stringResource(Res.string.travel_visit_fallback, index + 1),
                    number = (index + 1).takeIf { state.selectedDay != TravelPlan.UNASSIGNED_DAY },
                    selected = state.selectedVisitId == visit.id,
                    canEdit = canEdit,
                    canMoveUp = index > 0,
                    canMoveDown = index < visits.lastIndex,
                    onOpen = { onOpenVisit(visit) },
                    onMove = { offset -> onMoveVisit(visit, offset) },
                    onDragStart = {
                        val row = visitListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == visit.id }
                        if (row != null) {
                            draggedId = visit.id
                            dragPosition = row.offset + row.size / 2f
                            dragOrder = savedVisits.map(TravelVisit::id)
                        }
                    },
                    onDrag = { delta ->
                        val order = dragOrder
                        if (order != null && draggedId == visit.id) {
                            dragPosition += delta
                            val target =
                                visitListState.layoutInfo.visibleItemsInfo.firstOrNull { row ->
                                    dragPosition >= row.offset && dragPosition < row.offset + row.size
                                }
                            val from = order.indexOf(visit.id)
                            val to = target?.index ?: from
                            if (from >= 0 && to in order.indices && from != to) {
                                dragOrder = order.toMutableList().apply { add(to, removeAt(from)) }
                            }
                        }
                    },
                    onDragFinished = { apply ->
                        val order = dragOrder
                        if (apply && order != null && order != savedVisits.map(TravelVisit::id)) {
                            onReorderVisits(order)
                        } else {
                            dragOrder = null
                        }
                        draggedId = null
                    },
                )
            }
            if (state.dayPlaces.isNotEmpty()) {
                item { GooglePlacesAttribution(state.dayPlaces.values.flatMap { it.attributions }) }
            }
        }
        if (plan.summary.hasEnded {
                Clock.System
                    .now()
                    .toLocalDateTime(it)
                    .date
            }
        ) {
            TextButton(onClick = onFindNextHoliday, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(stringResource(Res.string.travel_find_next_holiday))
            }
        }
        Button(
            enabled = canEdit,
            onClick = onSearchPlaces,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Text(stringResource(Res.string.travel_add_visit))
        }

        state.visitDraft?.let { draft ->
            TravelVisitEditor(
                draft = draft,
                dates = dates,
                canEdit = canEdit,
                isSaving = state.isSaving,
                failure = failure,
                attributions = state.dayPlaces[draft.visit?.details?.googlePlaceId]?.attributions.orEmpty(),
                canRetrySave = state.canRetrySave,
                onDraftChange = onDraftChange,
                onApply = onApplyVisit,
                onRemove = onRemoveVisit,
                onReload = onReload,
                onRetrySave = onRetrySave,
            )
        }
    }
}

@Composable
private fun TravelVisitRow(
    visit: TravelVisit,
    title: String,
    number: Int?,
    selected: Boolean,
    canEdit: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onOpen: () -> Unit,
    onMove: (Int) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragFinished: (Boolean) -> Unit,
) {
    val dragStart by rememberUpdatedState(onDragStart)
    val drag by rememberUpdatedState(onDrag)
    val dragFinished by rememberUpdatedState(onDragFinished)
    var showMenu by remember { mutableStateOf(false) }
    Row {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .noRippleClickable(
                        onClick = onOpen,
                        interactionSource = remember { MutableInteractionSource() },
                        enabled = canEdit,
                    ).padding(vertical = 12.dp, horizontal = 4.dp),
        ) {
            val label = if (number == null) title else stringResource(Res.string.travel_visit_number, number, title)
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            if (selected) Text(stringResource(Res.string.travel_visit_selected), style = MaterialTheme.typography.labelSmall)
            visit.details.localTime?.let { Text(it.toString()) }
            if (visit.details.note != null) {
                Text(stringResource(Res.string.travel_has_note), style = MaterialTheme.typography.labelMedium)
            }
        }
        if (canMoveUp || canMoveDown) {
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    enabled = canEdit,
                    modifier =
                        Modifier.pointerInput(visit.id, canEdit) {
                            if (canEdit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { dragStart() },
                                    onDragEnd = { dragFinished(true) },
                                    onDragCancel = { dragFinished(false) },
                                ) { change, delta ->
                                    change.consume()
                                    drag(delta.y)
                                }
                            }
                        },
                ) {
                    Icon(
                        painterResource(Res.drawable.btn_menu),
                        stringResource(Res.string.travel_move_visit, title),
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.travel_move_up)) },
                        enabled = canMoveUp,
                        onClick = {
                            showMenu = false
                            onMove(-1)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.travel_move_down)) },
                        enabled = canMoveDown,
                        onClick = {
                            showMenu = false
                            onMove(1)
                        },
                    )
                }
            }
        }
    }
    HorizontalDivider()
}

@Composable
internal fun travelDayLabel(
    dates: TravelDates,
    dayNumber: Int,
): String =
    if (dayNumber == TravelPlan.UNASSIGNED_DAY) {
        stringResource(Res.string.travel_unassigned)
    } else if (dayNumber in 1..dates.dayCount) {
        stringResource(Res.string.travel_day, dayNumber, dates.dateOn(dayNumber).toString())
    } else {
        stringResource(Res.string.travel_day_outside_trip, dayNumber)
    }
