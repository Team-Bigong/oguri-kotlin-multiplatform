package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import oguri.composeapp.generated.resources.ic_travel_drag
import oguri.composeapp.generated.resources.ic_travel_note
import oguri.composeapp.generated.resources.ic_travel_undo
import oguri.composeapp.generated.resources.travel_removed_visit
import oguri.composeapp.generated.resources.travel_save_failed_label
import oguri.composeapp.generated.resources.travel_delete
import androidx.compose.foundation.border
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint10
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.SkeletonBox
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.feature.travelplanner.ui.component.GooglePlacesAttribution
import com.bigong.oguri.feature.travelplanner.ui.component.TravelBottomAction
import com.bigong.oguri.feature.travelplanner.ui.component.TravelDaySelector
import com.bigong.oguri.feature.travelplanner.ui.component.TravelIconButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoadFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoading
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlanPane
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelVisitDetailsSheet
import com.bigong.oguri.feature.travelplanner.ui.component.TravelVisitEditor
import com.bigong.oguri.feature.travelplanner.ui.component.label
import com.bigong.oguri.feature.travelplanner.ui.component.travelTextStyle
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.model.canApplyTo
import com.bigong.oguri.feature.travelplanner.ui.model.hasEnded
import com.bigong.oguri.feature.travelplanner.ui.model.isOutsideTrip
import com.bigong.oguri.feature.travelplanner.ui.model.travelMapScene
import kotlin.time.Clock
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_menu
import oguri.composeapp.generated.resources.ic_travel_more
import oguri.composeapp.generated.resources.calendar_month_day
import oguri.composeapp.generated.resources.travel_retry
import oguri.composeapp.generated.resources.ic_travel_plus
import oguri.composeapp.generated.resources.travel_add_visit
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_day
import oguri.composeapp.generated.resources.travel_day_outside_trip
import oguri.composeapp.generated.resources.travel_discard_draft
import oguri.composeapp.generated.resources.travel_empty_day
import oguri.composeapp.generated.resources.travel_find_next_holiday
import oguri.composeapp.generated.resources.travel_has_note
import oguri.composeapp.generated.resources.travel_list
import oguri.composeapp.generated.resources.travel_manage
import oguri.composeapp.generated.resources.travel_move_down
import oguri.composeapp.generated.resources.travel_move_up
import oguri.composeapp.generated.resources.travel_move_visit
import oguri.composeapp.generated.resources.ic_travel_offline
import oguri.composeapp.generated.resources.travel_offline_snapshot
import oguri.composeapp.generated.resources.travel_order_cannot_reapply
import oguri.composeapp.generated.resources.travel_order_matches
import oguri.composeapp.generated.resources.travel_order_save_failed
import oguri.composeapp.generated.resources.travel_order_conflict_description
import oguri.composeapp.generated.resources.travel_discard_order
import oguri.composeapp.generated.resources.travel_apply_order
import oguri.composeapp.generated.resources.travel_saved_day_order
import oguri.composeapp.generated.resources.travel_just_added
import oguri.composeapp.generated.resources.travel_overlapping_visits
import oguri.composeapp.generated.resources.travel_place_details_unavailable
import oguri.composeapp.generated.resources.travel_reload
import oguri.composeapp.generated.resources.travel_saved_status
import oguri.composeapp.generated.resources.travel_sign_in
import oguri.composeapp.generated.resources.travel_unassigned
import oguri.composeapp.generated.resources.travel_undo_visit
import oguri.composeapp.generated.resources.travel_visit_fallback
import oguri.composeapp.generated.resources.travel_visit_number
import oguri.composeapp.generated.resources.travel_visit_selected
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    onRemoveFromList: (TravelVisit) -> Unit,
    onSearchPlaces: () -> Unit,
    onMarkerClick: (String) -> Unit,
    onDirectionsOpened: () -> Unit,
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
        showTopDivider = false,
        actions = {
            if (plan != null) {
                TravelIconButton(onClick = onMembers, enabled = !state.isSaving && !state.canRetrySave) {
                    Icon(painterResource(Res.drawable.ic_travel_more), stringResource(Res.string.travel_manage), tint = Neutral100)
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
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TravelLoading(Modifier.fillMaxWidth(0.65f))
                    SkeletonBox(Modifier.fillMaxWidth().height(230.dp))
                    TravelLoading(Modifier.fillMaxWidth(0.45f))
                }
            }
            return@TravelPlannerPage
        }

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
            Row(Modifier.fillMaxWidth().background(com.bigong.oguri.core.designsystem.Neutral10).heightIn(min = 42.dp).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(oguri.composeapp.generated.resources.Res.drawable.ic_travel_offline), null, Modifier.size(18.dp), tint = OguriTheme.colors.textSecondary)
                Text(offlineMessage, style = OguriTheme.typography.bodySmall, color = OguriTheme.colors.textSecondary)
            }
        }
        TravelDaySelector(
            dates = dates,
            selectedDay = state.selectedDay,
            enabled = !state.isSaving && !state.canRetrySave,
            onSelectDay = { day -> if (day == state.selectedDay) onFitMap() else onSelectDay(day) },
            onOverview = onOverview,
        )

        val unappliedOrder = state.unappliedOrder?.takeIf { it.dayNumber == state.selectedDay || it.isOutsideTrip(plan) }
        val cityCenter = TravelCity.centerOf(plan.summary.details.city)
        val scene =
            remember(state.selectedDay, visits, state.dayPlaces, state.selectedVisitId, cityCenter) {
                travelMapScene(state.selectedDay, visits, state.dayPlaces, state.selectedVisitId, cityCenter)
            }
        TravelPlanPane(
            scene = scene,
            hideMap = unappliedOrder != null,
            viewportRequest = state.viewportRequest,
            focusedVisitId = state.mapFocusedVisitId,
            onMarkerClick = onMarkerClick,
            modifier = Modifier.weight(1f),
            status = {
                if (state.isLoadingDayPlaces) TravelLoading(Modifier.fillMaxWidth())
                if (state.dayPlacesIncomplete && !state.isOffline) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Text(stringResource(Res.string.travel_place_details_unavailable), Modifier.weight(1f))
                        TravelTextButton(onClick = onRetryPlaces) { Text(stringResource(Res.string.travel_retry)) }
                    }
                }

                if (failure != null && !(failure == TravelPlanFailure.CONNECTION && (state.isOffline || state.canRetrySave)) && !(failure == TravelPlanFailure.CONFLICT && unappliedOrder != null) && state.visitDraft == null && state.placeSearch == null) {
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
                                stringResource(Res.string.travel_retry)
                            } else {
                                stringResource(Res.string.travel_reload)
                            },
                    )
                }

                if (unappliedOrder != null) {
                    val latestById = plan.visits.associateBy(TravelVisit::id)
                    val sameOrder = !unappliedOrder.isOutsideTrip(plan) && savedVisits.map(TravelVisit::id) == unappliedOrder.visitIds
                    val canReapply = unappliedOrder.canApplyTo(plan)
                    val shape = RoundedCornerShape(8.dp)
                    Column(
                        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                            .background(com.bigong.oguri.core.designsystem.Orange5, shape)
                            .border(1.dp, com.bigong.oguri.core.designsystem.Orange20, shape).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(Res.string.travel_order_save_failed), style = OguriTheme.typography.cardSubtitle, color = Neutral100)
                        Text(stringResource(Res.string.travel_order_conflict_description), style = OguriTheme.typography.caption, color = OguriTheme.colors.textSecondary)
                        Column(Modifier.fillMaxWidth().background(Neutral0, shape).padding(12.dp)) {
                            unappliedOrder.visitIds.forEachIndexed { index, id ->
                                val details = latestById[id]?.details
                                val name = details?.name ?: state.dayPlaces[details?.googlePlaceId]?.name
                                Text(
                                    if (name == null) stringResource(Res.string.travel_visit_fallback, index + 1)
                                    else stringResource(Res.string.travel_visit_number, index + 1, name),
                                    style = OguriTheme.typography.bodyMedium, color = Neutral100,
                                )
                            }
                        }
                        if (!state.needsLatestPlan) {
                            when {
                                sameOrder -> Text(stringResource(Res.string.travel_order_matches), style = OguriTheme.typography.caption)
                                !canReapply -> Text(stringResource(Res.string.travel_order_cannot_reapply), style = OguriTheme.typography.caption)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TravelTextButton(onClick = onDiscardUnappliedOrder, enabled = !state.isSaving && !state.canRetrySave) {
                                Text(stringResource(Res.string.travel_discard_order), style = OguriTheme.typography.bodySmall, color = OguriTheme.colors.textTertiary)
                            }
                            if (state.needsLatestPlan) {
                                TravelTextButton(onClick = onReload, enabled = !state.isLoading) { Text(stringResource(Res.string.travel_reload)) }
                            } else if (!sameOrder && canReapply) {
                                TravelTextButton(onClick = onApplyUnappliedOrder, enabled = canEdit) {
                                    Text(stringResource(Res.string.travel_apply_order), style = OguriTheme.typography.labelMedium, color = Mint70)
                                }
                            }
                        }
                    }
                }
            },
        ) { mapVisible ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (unappliedOrder != null) stringResource(Res.string.travel_saved_day_order, state.selectedDay) else travelDayLabel(dates, state.selectedDay),
                    modifier = Modifier.weight(1f),
                    color = Neutral100,
                    style = travelTextStyle(15, 18, FontWeight.Medium),
                )
                if (state.canRetrySave) {
                    Text(stringResource(Res.string.travel_save_failed_label), color = com.bigong.oguri.core.designsystem.Orange50, style = OguriTheme.typography.bodySmall)
                    TravelTextButton(onClick = onRetrySave, enabled = !state.isSaving) { Text(stringResource(Res.string.travel_retry)) }
                }
                if (!state.isOffline && !state.isSaving && (failure == null || (unappliedOrder != null && !state.needsLatestPlan))) {
                    Text(
                        stringResource(Res.string.travel_saved_status),
                        color = OguriTheme.colors.textTertiary,
                        style = travelTextStyle(13, 16),
                    )
                }
            }
            val visitListState = rememberLazyListState()
            var draggedId by remember(state.selectedDay, plan.structureRevision, orderRevision) { mutableStateOf<String?>(null) }
            var dragPosition by remember(state.selectedDay, plan.structureRevision, orderRevision) { mutableStateOf(0f) }
            LaunchedEffect(state.listFocusRequest) {
                val index = visits.indexOfFirst { it.id == state.selectedVisitId }
                if (state.listFocusRequest > 0 && index >= 0) {
                    visitListState.scrollToItem(index)
                }
            }
            LazyColumn(
                state = visitListState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                val undo = state.visitUndo?.takeIf { it.dayNumber == state.selectedDay }
                if (visits.isEmpty()) {
                    item {
                        Text(
                            stringResource(Res.string.travel_empty_day),
                            color = OguriTheme.colors.textTertiary,
                            style = travelTextStyle(14, 21),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                itemsIndexed(visits, key = { _, visit -> visit.id }) { index, visit ->
                    if (undo?.position == index) TravelRemovedVisitRow(undo.title, canEdit, onUndoVisit)
                    TravelVisitRow(
                        visit = visit,
                        title =
                            visit.details.name ?: state.dayPlaces[visit.details.googlePlaceId]?.name
                                ?: stringResource(Res.string.travel_visit_fallback, index + 1),
                        number = (index + 1).takeIf { state.selectedDay != TravelPlan.UNASSIGNED_DAY },
                        selected = state.selectedVisitId == visit.id,
                        canEdit = canEdit && unappliedOrder == null,
                        addedAfterConflict = unappliedOrder != null && visit.id !in unappliedOrder.visitIds,
                        canOpen = !state.isSaving && !state.isLoading && !state.canRetrySave && state.visitDraft == null,
                        canMoveUp = index > 0,
                        canMoveDown = index < visits.lastIndex,
                        onRemove = { onRemoveFromList(visit) },
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
                if (undo != null && undo.position >= visits.size) item(key = "removed-visit") { TravelRemovedVisitRow(undo.title, canEdit, onUndoVisit) }
                val attributions = state.dayPlaces.values.flatMap { it.attributions }
                if (attributions.isNotEmpty() || (!mapVisible && state.dayPlaces.isNotEmpty())) {
                    item { GooglePlacesAttribution(attributions, includeGoogleBranding = !mapVisible) }
                }
            }
        }
        TravelBottomAction(enabled = canEdit, onClick = onSearchPlaces) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(Res.drawable.ic_travel_plus), contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.travel_add_visit))
            }
        }
        if (plan.summary.hasEnded {
                Clock.System
                    .now()
                    .toLocalDateTime(it)
                    .date
            }
        ) {
            TravelTextButton(onClick = onFindNextHoliday, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(stringResource(Res.string.travel_find_next_holiday))
            }
        }
        state.inspectedVisitId?.let { id ->
            plan.visits.firstOrNull { it.id == id }?.let { visit ->
                TravelVisitDetailsSheet(
                    visit = visit,
                    title =
                        visit.details.name ?: state.dayPlaces[visit.details.googlePlaceId]?.name
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
        state.visitDraft?.takeIf { state.placeSearch == null }?.let { draft ->
            TravelVisitEditor(
                draft = draft,
                dates = dates,
                canEdit = canEdit,
                isSaving = state.isSaving,
                failure = failure,
                attributions = state.dayPlaces[draft.visit?.details?.googlePlaceId]?.attributions.orEmpty(),
                placeAddress = state.dayPlaces[draft.visit?.details?.googlePlaceId]?.address,
                canRetrySave = state.canRetrySave,
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
    addedAfterConflict: Boolean,
    canEdit: Boolean,
    canOpen: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    onMove: (Int) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragFinished: (Boolean) -> Unit,
) {
    val dragStart by rememberUpdatedState(onDragStart)
    val drag by rememberUpdatedState(onDrag)
    val dragFinished by rememberUpdatedState(onDragFinished)
    var showMenu by remember { mutableStateOf(false) }
    var swiped by remember(visit.id) { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)
    Box(Modifier.fillMaxWidth().clip(shape)) {
        if (swiped && canEdit) Box(
            Modifier.align(Alignment.CenterEnd).width(64.dp).heightIn(min = 60.dp)
                .background(com.bigong.oguri.core.designsystem.Orange50)
                .noRippleClickable(onClick = onRemove), contentAlignment = Alignment.Center,
        ) { Text(stringResource(Res.string.travel_delete), color = com.bigong.oguri.core.designsystem.Neutral0, style = OguriTheme.typography.labelLarge) }
        Row(
            Modifier.fillMaxWidth().offset(x = if (swiped && canEdit) (-64).dp else 0.dp)
                .background(if (selected) Mint10 else Neutral5)
                .pointerInput(visit.id, canEdit) {
                    if (canEdit) {
                        var distance = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { distance = 0f },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                distance += amount
                                if (distance < -32.dp.toPx()) swiped = true
                                else if (distance > 32.dp.toPx()) swiped = false
                            },
                        )
                    }
                }.semantics { this.selected = selected },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1f).heightIn(min = 60.dp).noRippleClickable(enabled = canOpen, onClick = onOpen).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(24.dp).background(Mint70, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                    Text(number?.toString().orEmpty(), style = OguriTheme.typography.labelMedium, color = com.bigong.oguri.core.designsystem.Neutral0)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = OguriTheme.typography.cardSubtitle, color = Neutral100)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        visit.details.category?.let { Text(stringResource(it.label()) + if (addedAfterConflict) " · " + stringResource(Res.string.travel_just_added) else "", style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary) }
                        if (!visit.details.note.isNullOrBlank()) Icon(painterResource(Res.drawable.ic_travel_note), stringResource(Res.string.travel_has_note), Modifier.size(14.dp), tint = OguriTheme.colors.textTertiary)
                    }
                }
            }
            if (canEdit && (canMoveUp || canMoveDown)) Box {
                TravelIconButton(onClick = { showMenu = true }, enabled = canEdit,
                    modifier = Modifier.pointerInput(visit.id, canEdit) {
                        detectDragGesturesAfterLongPress(onDragStart = { dragStart() }, onDragEnd = { dragFinished(true) }, onDragCancel = { dragFinished(false) }) { change, delta -> change.consume(); drag(delta.y) }
                    },
                ) { Icon(painterResource(Res.drawable.ic_travel_drag), stringResource(Res.string.travel_move_visit, title), Modifier.size(20.dp), tint = OguriTheme.colors.textTertiary) }
                if (showMenu) androidx.compose.ui.window.Popup(alignment = Alignment.TopEnd, onDismissRequest = { showMenu = false }, properties = androidx.compose.ui.window.PopupProperties(focusable = true)) {
                    androidx.compose.material3.Surface(shape = shape, color = Neutral0, shadowElevation = 4.dp) {
                        Column {
                            TravelTextButton(enabled = canMoveUp, onClick = { showMenu = false; onMove(-1) }) { Text(stringResource(Res.string.travel_move_up)) }
                            TravelTextButton(enabled = canMoveDown, onClick = { showMenu = false; onMove(1) }) { Text(stringResource(Res.string.travel_move_down)) }
                            TravelTextButton(onClick = { showMenu = false; onRemove() }) { Text(stringResource(Res.string.travel_delete), color = com.bigong.oguri.core.designsystem.Orange50) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun travelDayLabel(
    dates: TravelDates,
    dayNumber: Int,
): String =
    if (dayNumber == TravelPlan.UNASSIGNED_DAY) {
        stringResource(Res.string.travel_unassigned)
    } else if (dayNumber in 1..dates.dayCount) {
        val date = dates.dateOn(dayNumber)
        stringResource(Res.string.travel_day, dayNumber, travelWeekDate(date))
    } else {
        stringResource(Res.string.travel_day_outside_trip, dayNumber)
    }

@Composable
private fun TravelRemovedVisitRow(title: String?, enabled: Boolean, onUndo: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(com.bigong.oguri.core.designsystem.Neutral10, RoundedCornerShape(8.dp)).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title?.let { stringResource(Res.string.travel_removed_visit, it) } ?: stringResource(Res.string.travel_undo_visit), Modifier.weight(1f), style = OguriTheme.typography.bodySmall)
        TravelIconButton(onClick = onUndo, enabled = enabled) { Icon(painterResource(Res.drawable.ic_travel_undo), stringResource(Res.string.travel_undo_visit), Modifier.size(22.dp), tint = Neutral100) }
    }
}
