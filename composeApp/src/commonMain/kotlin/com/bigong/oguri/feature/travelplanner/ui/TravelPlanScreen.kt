package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.feature.travelplanner.ui.component.GooglePlacesAttribution
import com.bigong.oguri.feature.travelplanner.ui.component.TravelDaySelector
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoadFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelModifiedButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlanPane
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelVisitDetailsSheet
import com.bigong.oguri.feature.travelplanner.ui.component.TravelVisitEditor
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.model.canApplyTo
import com.bigong.oguri.feature.travelplanner.ui.model.isOutsideTrip
import com.bigong.oguri.feature.travelplanner.ui.model.hasEnded
import com.bigong.oguri.feature.travelplanner.ui.model.hasTimeOrderMismatch
import com.bigong.oguri.feature.travelplanner.ui.model.travelMapScene
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_menu
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.travel_add_visit
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_discard_draft
import oguri.composeapp.generated.resources.travel_order_mine
import oguri.composeapp.generated.resources.travel_order_cannot_reapply
import oguri.composeapp.generated.resources.travel_order_matches
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_day
import oguri.composeapp.generated.resources.travel_day_outside_trip
import oguri.composeapp.generated.resources.travel_empty_day
import oguri.composeapp.generated.resources.travel_find_next_holiday
import oguri.composeapp.generated.resources.travel_has_note
import oguri.composeapp.generated.resources.travel_list
import oguri.composeapp.generated.resources.travel_manage
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
    onOverview: () -> Unit,
    onOpenVisit: (TravelVisit?) -> Unit,
    onInspectVisit: (String) -> Unit,
    onCloseInspection: () -> Unit,
    onMoveVisit: (TravelVisit, Int) -> Unit,
    onReorderVisits: (Int, Long, Long, List<String>) -> Boolean,
    onApplyUnappliedOrder: () -> Unit,
    onDiscardUnappliedOrder: () -> Unit,
    onDraftChange: (TravelVisitDraft?) -> Unit,
    onApplyVisit: () -> Unit,
    onRemoveVisit: () -> Unit,
    onSearchPlaces: () -> Unit,
    onMarkerClick: (String) -> Unit,
    onDirectionsOpened: () -> Unit,
    onFitMap: () -> Unit,
    onFindNextHoliday: () -> Unit,
    onMembers: () -> Unit,
    onRecentChanges: () -> Unit,
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

        TravelModifiedButton(plan, enabled = !state.isSaving && !state.canRetrySave, onClick = onRecentChanges)
        val dates = plan.summary.details.dates
        val savedVisits = plan.visits.filter { it.dayNumber == state.selectedDay }
        val orderRevision = plan.orderRevision(state.selectedDay)
        var dragOrder by remember(state.selectedDay, plan.structureRevision, orderRevision) { mutableStateOf<List<String>?>(null) }
        LaunchedEffect(state.failure, state.isOffline) {
            if (state.failure != null || state.isOffline) dragOrder = null
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
        TravelDaySelector(
            dates = dates,
            selectedDay = state.selectedDay,
            enabled = !state.isSaving && !state.canRetrySave,
            onSelectDay = onSelectDay,
            onOverview = onOverview,
        )

        val cityCenter = TravelCity.centerOf(plan.summary.details.city)
        val scene =
            remember(state.selectedDay, visits, state.dayPlaces, state.selectedVisitId, cityCenter) {
                travelMapScene(state.selectedDay, visits, state.dayPlaces, state.selectedVisitId, cityCenter)
            }
        var overlappingVisits by remember(state.selectedDay) { mutableStateOf<List<String>>(emptyList()) }
        val availableOverlaps = scene.markers.filter { it.id in overlappingVisits }.map { it.id }
        if (availableOverlaps.size > 1) {
            AlertDialog(
                onDismissRequest = { overlappingVisits = emptyList() },
                title = { Text(stringResource(Res.string.travel_overlapping_visits)) },
                text = {
                    LazyColumn(Modifier.heightIn(max = 240.dp)) {
                        itemsIndexed(visits) { index, visit ->
                            if (visit.id in availableOverlaps) {
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
        TravelPlanPane(
            scene = scene,
            viewportRequest = state.viewportRequest,
            focusedVisitId = state.mapFocusedVisitId,
            onFitMap = onFitMap,
            onMarkerClick = { visitId ->
                val coordinates = scene.markers.firstOrNull { it.id == visitId }?.coordinates
                val atPoint = scene.markers.filter { it.coordinates == coordinates }.map { it.id }
                if (atPoint.size > 1) overlappingVisits = atPoint else onMarkerClick(visitId)
            },
            modifier = Modifier.weight(1f),
            status = {
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
            },
        ) {
            val visitListState = rememberLazyListState()
            var draggedId by remember(state.selectedDay, plan.structureRevision, orderRevision) { mutableStateOf<String?>(null) }
            var dragPosition by remember(state.selectedDay, plan.structureRevision, orderRevision) { mutableStateOf(0f) }
            val unappliedOrder =
                state.unappliedOrder?.takeIf { it.dayNumber == state.selectedDay || it.isOutsideTrip(plan) }
            LaunchedEffect(state.listFocusRequest) {
                val index = visits.indexOfFirst { it.id == state.selectedVisitId }
                if (state.listFocusRequest > 0 && index >= 0) {
                    visitListState.scrollToItem(index + if (unappliedOrder == null) 0 else 1)
                }
            }
            LazyColumn(
                state = visitListState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (unappliedOrder != null) {
                    item(key = "unapplied-visit-order") {
                        val savedIds = savedVisits.map(TravelVisit::id)
                        val latestById = plan.visits.associateBy(TravelVisit::id)
                        val sameOrder = !unappliedOrder.isOutsideTrip(plan) && savedIds == unappliedOrder.visitIds
                        val canReapply = unappliedOrder.canApplyTo(plan)
                        Text(stringResource(Res.string.travel_order_mine), style = MaterialTheme.typography.titleSmall)
                        Text(travelDayLabel(dates, unappliedOrder.dayNumber), style = MaterialTheme.typography.bodySmall)
                        unappliedOrder.visitIds.forEachIndexed { index, id ->
                            val details = latestById[id]?.details
                            val name = state.dayPlaces[details?.googlePlaceId]?.name ?: details?.name
                            Text(
                                if (name == null) stringResource(Res.string.travel_visit_fallback, index + 1)
                                else stringResource(Res.string.travel_visit_number, index + 1, name),
                            )
                        }
                        if (!state.needsLatestPlan) {
                            when {
                                sameOrder -> Text(stringResource(Res.string.travel_order_matches))
                                !canReapply -> Text(stringResource(Res.string.travel_order_cannot_reapply))
                                else -> TextButton(onClick = onApplyUnappliedOrder, enabled = canEdit) {
                                    Text(stringResource(Res.string.travel_apply))
                                }
                            }
                        }
                        TextButton(onClick = onDiscardUnappliedOrder, enabled = !state.isSaving && !state.canRetrySave) {
                            Text(stringResource(Res.string.travel_discard_draft))
                        }
                        HorizontalDivider()
                    }
                }
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
                        canOpen = !state.isSaving && !state.isLoading && !state.canRetrySave && state.visitDraft == null,
                        canMoveUp = index > 0,
                        canMoveDown = index < visits.lastIndex,
                        onOpen = { if (canEdit) onOpenVisit(visit) else onInspectVisit(visit.id) },
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
                                val to = order.indexOf(target?.key).takeIf { it >= 0 } ?: from
                                if (from >= 0 && to in order.indices && from != to) {
                                    dragOrder = order.toMutableList().apply { add(to, removeAt(from)) }
                                }
                            }
                        },
                        onDragFinished = { apply ->
                            if (draggedId == visit.id) {
                                val order = dragOrder
                                val accepted =
                                    apply && order != null &&
                                        onReorderVisits(state.selectedDay, orderRevision, plan.structureRevision, order)
                                if (!accepted) dragOrder = null
                                draggedId = null
                            }
                        },
                    )
                }
                if (state.dayPlaces.isNotEmpty()) {
                    item { GooglePlacesAttribution(state.dayPlaces.values.flatMap { it.attributions }) }
                }
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

        state.inspectedVisitId?.let { id ->
            plan.visits.firstOrNull { it.id == id }?.let { visit ->
                TravelVisitDetailsSheet(
                    visit = visit,
                    title =
                        state.dayPlaces[visit.details.googlePlaceId]?.name ?: visit.details.name
                            ?: stringResource(
                                Res.string.travel_visit_fallback,
                                plan.visits.filter { it.dayNumber == visit.dayNumber }.indexOf(visit) + 1,
                            ),
                    dates = dates,
                    place = state.dayPlaces[visit.details.googlePlaceId],
                    offline = state.isOffline,
                    onDismiss = onCloseInspection,
                    onDirectionsOpened = onDirectionsOpened,
                )
            }
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
                timeOrderMismatch = draft.hasTimeOrderMismatch(plan.visits),
                onDraftChange = onDraftChange,
                onApply = onApplyVisit,
                onRemove = onRemoveVisit,
                onReload = onReload,
                onRetrySave = onRetrySave,
                onLeaveWithPendingSave = onBack.takeIf { state.pendingSaveStored },
                onDirectionsOpened = onDirectionsOpened,
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
    canOpen: Boolean,
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
                        enabled = canOpen,
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
