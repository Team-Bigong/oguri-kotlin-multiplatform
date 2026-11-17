package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import oguri.composeapp.generated.resources.ic_travel_drag
import oguri.composeapp.generated.resources.ic_travel_note
import oguri.composeapp.generated.resources.ic_travel_undo
import oguri.composeapp.generated.resources.travel_removed_visit
import oguri.composeapp.generated.resources.travel_save_failed_label
import oguri.composeapp.generated.resources.travel_delete
import androidx.compose.foundation.border
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.travelplanner.ui.component.TravelIssueCard
import com.bigong.oguri.feature.travelplanner.ui.component.travelAccentForeground
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlanSkeleton
import com.bigong.oguri.core.util.extension.HapticType
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.util.extension.perform
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
import oguri.composeapp.generated.resources.img_oguri_empty
import oguri.composeapp.generated.resources.travel_go_to_plans
import oguri.composeapp.generated.resources.travel_trip_unavailable_message
import oguri.composeapp.generated.resources.travel_unavailable
import oguri.composeapp.generated.resources.travel_trip
import oguri.composeapp.generated.resources.travel_add_visit
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_day
import oguri.composeapp.generated.resources.travel_day_outside_trip
import oguri.composeapp.generated.resources.travel_discard_draft
import oguri.composeapp.generated.resources.travel_empty_day
import oguri.composeapp.generated.resources.travel_completed_trip
import oguri.composeapp.generated.resources.ic_travel_row_arrow
import oguri.composeapp.generated.resources.travel_find_next_holiday
import oguri.composeapp.generated.resources.travel_has_note
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
    onOpenPlans: () -> Unit,
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
    val loadingPlan = plan == null && (failure == null || (state.isLoading && failure == TravelPlanFailure.CONNECTION))
    val offlineMessage = stringResource(Res.string.travel_offline_snapshot)
    TravelPlannerPage(
        title = plan?.summary?.details?.title ?: stringResource(Res.string.travel_trip),
        onBack = onBack,
        isSaving = state.isSaving,
        showTopDivider = false,
        loadingTitle = loadingPlan,
        actions = {
            if (plan != null) {
                TravelIconButton(onClick = onMembers, enabled = !state.isSaving && !state.canRetrySave) {
                    Icon(painterResource(Res.drawable.ic_travel_more), stringResource(Res.string.travel_manage), tint = OguriTheme.colors.textPrimary)
                }
            } else if (loadingPlan) {
                TravelIconButton(onClick = onMembers, enabled = false) {
                    Icon(painterResource(Res.drawable.ic_travel_more), contentDescription = null, tint = OguriTheme.colors.textPrimary)
                }
            }
        },
    ) {
        if (plan == null) {
            when {
                loadingPlan -> {
                    TravelPlanSkeleton(Modifier.weight(1f))
                    TravelBottomAction(enabled = false, onClick = {}) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(Res.drawable.ic_travel_plus), null, Modifier.size(18.dp))
                            Text(stringResource(Res.string.travel_add_visit))
                        }
                    }
                }
                failure == TravelPlanFailure.UNAVAILABLE -> TravelPlansEmpty(
                    illustration = Res.drawable.img_oguri_empty,
                    message = stringResource(Res.string.travel_trip_unavailable_message),
                    actionLabel = stringResource(Res.string.travel_go_to_plans),
                    onAction = onOpenPlans,
                    title = stringResource(Res.string.travel_unavailable),
                    modifier = Modifier.weight(1f),
                    illustrationSize = 180.dp,
                    outlinedAction = true,
                )
                failure == TravelPlanFailure.SIGN_IN_REQUIRED -> TravelInlineFailure(
                    failure, onSignIn, stringResource(Res.string.travel_sign_in),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                )
                failure != null -> TravelLoadFailure(failure, onReload)
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
            Row(Modifier.fillMaxWidth().background(OguriTheme.colors.backgroundTertiary).heightIn(min = 42.dp).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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

        val showPartialFailure = state.dayPlacesIncomplete && !state.isOffline
        val unappliedOrder = state.unappliedOrder?.takeIf { it.dayNumber == state.selectedDay || it.isOutsideTrip(plan) }
        val details = plan.summary.details
        val cityCenter = details.catalogCities[details.city]?.coordinates ?: TravelCity.centerOf(details.city)
        val scene =
            remember(state.selectedDay, visits, state.dayPlaces, state.selectedVisitId, cityCenter) {
                travelMapScene(state.selectedDay, visits, state.dayPlaces, state.selectedVisitId, cityCenter)
            }
        TravelPlanPane(
            scene = scene,
            hideMap = unappliedOrder != null || showPartialFailure || state.needsLatestPlan,
            emptyDay = visits.isEmpty(),
            viewportRequest = state.viewportRequest,
            focusedVisitId = state.mapFocusedVisitId,
            onMarkerClick = onMarkerClick,
            modifier = Modifier.weight(1f),
            status = {
                if (state.isLoadingDayPlaces) TravelLoading(Modifier.fillMaxWidth())
                if (showPartialFailure) {
                    TravelIssueCard(
                        title = stringResource(Res.string.travel_place_details_unavailable),
                        onRetry = onRetryPlaces,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
                    )
                }

                if (failure != null && !(failure == TravelPlanFailure.CONNECTION && (state.isOffline || state.canRetrySave)) && !(failure == TravelPlanFailure.CONFLICT && unappliedOrder != null) && state.visitDraft == null && state.placeSearch == null) {
                    TravelInlineFailure(
                        failure = failure,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
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
                    val rebasedVisitIds = unappliedOrder.rebasedVisitIds(plan)
                    val sameOrder = savedVisits.map(TravelVisit::id) == rebasedVisitIds
                    val shape = RoundedCornerShape(8.dp)
                    Column(
                        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                            .background(OguriTheme.colors.orangeContainer, shape)
                            .border(1.dp, OguriTheme.colors.borderSubtle, shape).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(Res.string.travel_order_save_failed), style = OguriTheme.typography.cardSubtitle, color = OguriTheme.colors.textPrimary)
                        Text(stringResource(Res.string.travel_order_conflict_description), style = OguriTheme.typography.caption, color = OguriTheme.colors.textSecondary)
                        Column(Modifier.fillMaxWidth().background(OguriTheme.colors.surfaceCard, shape).padding(12.dp)) {
                            unappliedOrder.visitIds.forEachIndexed { index, id ->
                                val details = latestById[id]?.details
                                val name = details?.name ?: state.dayPlaces[details?.googlePlaceId]?.name
                                Text(
                                    if (name == null) stringResource(Res.string.travel_visit_fallback, index + 1)
                                    else stringResource(Res.string.travel_visit_number, index + 1, name),
                                    style = OguriTheme.typography.bodyMedium, color = OguriTheme.colors.textPrimary,
                                )
                            }
                        }
                        if (!state.needsLatestPlan) {
                            when {
                                sameOrder -> Text(stringResource(Res.string.travel_order_matches), style = OguriTheme.typography.caption)
                                rebasedVisitIds == null -> Text(stringResource(Res.string.travel_order_cannot_reapply), style = OguriTheme.typography.caption)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TravelTextButton(onClick = onDiscardUnappliedOrder, enabled = !state.isSaving && !state.canRetrySave) {
                                Text(stringResource(Res.string.travel_discard_order), style = OguriTheme.typography.bodySmall, color = OguriTheme.colors.textTertiary)
                            }
                            if (state.needsLatestPlan) {
                                TravelTextButton(onClick = onReload, enabled = !state.isLoading) { Text(stringResource(Res.string.travel_reload)) }
                            } else if (!sameOrder && rebasedVisitIds != null) {
                                TravelTextButton(onClick = onApplyUnappliedOrder, enabled = canEdit) {
                                    Text(stringResource(Res.string.travel_apply_order), style = OguriTheme.typography.labelLarge, color = OguriTheme.colors.textPrimary)
                                }
                            }
                        }
                    }
                }
            },
        ) { mapVisible ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (unappliedOrder != null) stringResource(Res.string.travel_saved_day_order, state.selectedDay) else travelDayLabel(dates, state.selectedDay),
                    modifier = Modifier.weight(1f),
                    color = OguriTheme.colors.textPrimary,
                    style = travelTextStyle(15, 18, FontWeight.Medium),
                )
                if (state.canRetrySave) {
                    Text(stringResource(Res.string.travel_save_failed_label), color = OguriTheme.colors.error, style = OguriTheme.typography.bodySmall)
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
            var releasedDrag by remember(state.selectedDay, plan.structureRevision, orderRevision) {
                mutableStateOf<Pair<String, Float>?>(null)
            }
            LaunchedEffect(state.listFocusRequest) {
                val index = visits.indexOfFirst { it.id == state.selectedVisitId }
                if (state.listFocusRequest > 0 && index >= 0) {
                    visitListState.scrollToItem(index)
                }
            }
            LazyColumn(
                state = visitListState,
                modifier = Modifier.weight(1f).clipToBounds(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp),
            ) {
                val undo = state.visitUndo?.takeIf { it.dayNumber == state.selectedDay }
                if (visits.isEmpty()) {
                    item {
                        Text(
                            stringResource(Res.string.travel_empty_day),
                            color = OguriTheme.colors.textTertiary,
                            style = travelTextStyle(14, 21),
                            modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 24.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                itemsIndexed(visits, key = { _, visit -> visit.id }) { index, visit ->
                    if (undo?.position == index) TravelRemovedVisitRow(undo.title, canEdit, onUndoVisit)
                    val isDragging = draggedId == visit.id
                    val release = releasedDrag?.takeIf { it.first == visit.id }
                    val settleOffset = remember(release) { Animatable(release?.second ?: 0f) }
                    LaunchedEffect(release) {
                        if (release != null) {
                            settleOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                            if (releasedDrag == release) releasedDrag = null
                        }
                    }
                    TravelVisitRow(
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                                placementSpec = if (isDragging || release != null) null
                                else spring(stiffness = Spring.StiffnessMediumLow),
                            )
                            .zIndex(if (isDragging || release != null) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isDragging) {
                                    val row = visitListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == visit.id }
                                    if (row == null) 0f else dragPosition - (row.offset + row.size / 2f)
                                } else {
                                    settleOffset.value
                                }
                            },
                        isDragging = isDragging,
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
                                HapticType.ImpactLight.perform()
                                releasedDrag = null
                                draggedId = visit.id
                                dragPosition = row.offset + row.size / 2f
                                dragOrder = savedVisits.map(TravelVisit::id)
                            }
                        },
                        onDrag = { delta ->
                            val order = dragOrder
                            if (order != null && draggedId == visit.id) {
                                dragPosition += delta
                                val from = order.indexOf(visit.id)
                                val visibleRows = visitListState.layoutInfo.visibleItemsInfo
                                // Wait for the list to lay out the last reorder before crossing another row.
                                if (visibleRows.firstOrNull { it.key == visit.id }?.index != from) return@TravelVisitRow
                                val target = visibleRows.firstOrNull { row ->
                                    dragPosition >= row.offset && dragPosition < row.offset + row.size
                                }
                                val to = order.indexOf(target?.key)
                                val crossedCenter = target != null && when {
                                    to < from -> dragPosition <= target.offset + target.size / 2f
                                    to > from -> dragPosition >= target.offset + target.size / 2f
                                    else -> false
                                }
                                if (to in order.indices && crossedCenter) {
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
                                val row = visitListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == visit.id }
                                if (accepted && row != null && row.index == order.indexOf(visit.id)) {
                                    releasedDrag = visit.id to (dragPosition - (row.offset + row.size / 2f))
                                }
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
        val completed = plan.summary.hasEnded { Clock.System.now().toLocalDateTime(it).date }
        if (completed) {
            HorizontalDivider(color = OguriTheme.colors.borderSubtle)
            val shape = RoundedCornerShape(8.dp)
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp)
                .background(OguriTheme.colors.mintContainer, shape)
                .border(1.dp, OguriTheme.colors.borderSubtle, shape)
                .noRippleClickable(onClick = onFindNextHoliday).heightIn(min = 50.dp).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(Res.string.travel_completed_trip), Modifier.weight(1f), style = OguriTheme.typography.bodySmall, color = OguriTheme.colors.textSecondary)
                Text(stringResource(Res.string.travel_find_next_holiday), style = OguriTheme.typography.cardSubtitle, color = OguriTheme.colors.textPrimary)
                Icon(painterResource(Res.drawable.ic_travel_row_arrow), null, Modifier.size(18.dp), tint = OguriTheme.colors.textTertiary)
            }
        }
        TravelBottomAction(enabled = canEdit, onClick = onSearchPlaces, showDivider = !completed, topPadding = if (completed) 10.dp else 12.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(Res.drawable.ic_travel_plus), contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.travel_add_visit))
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
    modifier: Modifier,
    isDragging: Boolean,
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
    val deleteVisible = swiped && canEdit
    val deleteWidth = 72.dp
    val colors = OguriTheme.colors
    val rowColor by animateColorAsState(
        targetValue = when {
            isDragging || selected -> colors.mintContainer
            else -> colors.backgroundSecondary
        },
        label = "Travel visit background",
    )
    Box(modifier.fillMaxWidth().clip(shape)) {
        Row(
            Modifier.fillMaxWidth().padding(end = if (deleteVisible) deleteWidth else 0.dp)
                .background(rowColor)
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
                Box(Modifier.size(24.dp).background(OguriTheme.colors.mint, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                    Text(number?.toString().orEmpty(), style = OguriTheme.typography.labelMedium, color = travelAccentForeground)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = OguriTheme.typography.cardSubtitle, color = OguriTheme.colors.textPrimary)
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
                    androidx.compose.material3.Surface(shape = shape, color = OguriTheme.colors.surfaceCard, shadowElevation = 4.dp) {
                        Column {
                            TravelTextButton(enabled = canMoveUp, onClick = { showMenu = false; onMove(-1) }) { Text(stringResource(Res.string.travel_move_up)) }
                            TravelTextButton(enabled = canMoveDown, onClick = { showMenu = false; onMove(1) }) { Text(stringResource(Res.string.travel_move_down)) }
                            TravelTextButton(onClick = { showMenu = false; onRemove() }) { Text(stringResource(Res.string.travel_delete), color = OguriTheme.colors.error) }
                        }
                    }
                }
            }
        }
        if (deleteVisible) {
            Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterEnd) {
                Box(
                    modifier = Modifier.width(deleteWidth).fillMaxHeight()
                        .background(OguriTheme.colors.orangeContainer)
                        .noRippleClickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(Res.string.travel_delete), color = OguriTheme.colors.textPrimary, style = OguriTheme.typography.labelLarge)
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
        stringResource(Res.string.travel_day, dayNumber, travelMonthDay(date))
    } else {
        stringResource(Res.string.travel_day_outside_trip, dayNumber)
    }

@Composable
private fun TravelRemovedVisitRow(title: String?, enabled: Boolean, onUndo: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(OguriTheme.colors.backgroundTertiary, RoundedCornerShape(8.dp)).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title?.let { stringResource(Res.string.travel_removed_visit, it) } ?: stringResource(Res.string.travel_undo_visit), Modifier.weight(1f), style = OguriTheme.typography.bodySmall)
        TravelIconButton(onClick = onUndo, enabled = enabled) { Icon(painterResource(Res.drawable.ic_travel_undo), stringResource(Res.string.travel_undo_visit), Modifier.size(22.dp), tint = OguriTheme.colors.textPrimary) }
    }
}
