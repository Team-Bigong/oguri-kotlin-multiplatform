package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitField
import oguri.composeapp.generated.resources.*
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import org.jetbrains.compose.resources.painterResource
import oguri.composeapp.generated.resources.ic_travel_close
import oguri.composeapp.generated.resources.travel_visit_day
import oguri.composeapp.generated.resources.travel_search_close
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.ConfirmAlertDialog
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlaceAttribution
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelVisitDetails
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_alert
import oguri.composeapp.generated.resources.travel_add_visit
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_delete
import oguri.composeapp.generated.resources.travel_delete_visit_title
import oguri.composeapp.generated.resources.travel_discard_draft
import oguri.composeapp.generated.resources.travel_discard_title
import oguri.composeapp.generated.resources.travel_edit_visit
import oguri.composeapp.generated.resources.travel_invalid_time
import oguri.composeapp.generated.resources.travel_leave_pending_save
import oguri.composeapp.generated.resources.travel_pending_save_preserved
import oguri.composeapp.generated.resources.travel_reload
import oguri.composeapp.generated.resources.travel_save_failed_label
import oguri.composeapp.generated.resources.travel_pending_save_preserved
import oguri.composeapp.generated.resources.travel_visit_location
import oguri.composeapp.generated.resources.travel_visit_name
import oguri.composeapp.generated.resources.travel_visit_note
import oguri.composeapp.generated.resources.travel_visit_note_shared
import oguri.composeapp.generated.resources.travel_visit_time
import oguri.composeapp.generated.resources.travel_visit_time_order
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelVisitEditor(
    draft: TravelVisitDraft,
    dates: TravelDates,
    canEdit: Boolean,
    isSaving: Boolean,
    failure: TravelPlanFailure?,
    attributions: List<TravelPlaceAttribution>,
    placeAddress: String?,
    canRetrySave: Boolean,
    onDraftChange: (TravelVisitDraft?) -> Unit,
    onApply: () -> Unit,
    onRemove: () -> Unit,
    onReload: () -> Unit,
    onRetrySave: () -> Unit,
    onLeaveWithPendingSave: (() -> Unit)?,
    onDirectionsOpened: () -> Unit,
) {
    var confirmDiscard by remember { mutableStateOf(false) }
    val original = draft.visit
    val changed = original?.details != draft.toDetails() || original.dayNumber != draft.dayNumber
    val hasInput = draft.name.isNotBlank() || draft.location.isNotBlank() || draft.localTime.isNotBlank() || draft.note.isNotBlank() || draft.category != null
    val needsDiscardConfirmation by rememberUpdatedState(changed && (original != null || hasInput))
    val dismissDraft = {
        if (needsDiscardConfirmation) confirmDiscard = true else onDraftChange(null)
    }
    val comparisonFields = draft.latestDifferences()
    val comparing = draft.showLatestComparison && comparisonFields.isNotEmpty()
    val placeId = draft.visit?.details?.googlePlaceId
    var showDayMenu by remember { mutableStateOf(false) }
    var confirmRemoval by remember { mutableStateOf(false) }
    val canDismiss by rememberUpdatedState(!isSaving && !canRetrySave)
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { target ->
                when {
                    target != SheetValue.Hidden -> true
                    !canDismiss -> false
                    needsDiscardConfirmation -> {
                        confirmDiscard = true
                        false
                    }
                    else -> true
                }
            },
        )
    val dismissSheet = rememberTravelSheetDismiss(sheetState, dismissDraft)
    ModalBottomSheet(scrimColor = TravelSheetScrim,
        dragHandle = { TravelSheetHandle() },
        containerColor = OguriTheme.colors.backgroundSecondary,
        contentColor = OguriTheme.colors.textPrimary,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        onDismissRequest = { if (canDismiss) dismissDraft() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.92f).imePadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.travel_edit_visit), Modifier.weight(1f), style = OguriTheme.typography.cardTitle)
                TravelIconButton(onClick = dismissSheet, enabled = canDismiss) { Icon(painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close), Modifier.size(22.dp)) }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TravelConflictComparison(draft, dates)
            if (isSaving) TravelLoading(modifier = Modifier.fillMaxWidth())
            if (canRetrySave && !isSaving) {
                val canLeave = onLeaveWithPendingSave != null
                TravelIssueCard(
                    title = stringResource(Res.string.travel_save_failed_label),
                    description = if (canLeave) stringResource(Res.string.travel_pending_save_preserved) else null,
                    onRetry = onRetrySave,
                    secondaryAction = onLeaveWithPendingSave?.let { onLeave ->
                        { TravelTextButton(onClick = onLeave) { Text(stringResource(Res.string.travel_leave_pending_save)) } }
                    },
                )
            }
            if (!comparing && placeId != null) {
                val shape = RoundedCornerShape(8.dp)
                TravelDirectionsButton(
                    placeId, draft.placeTitle,
                    compact = true,
                    enabled = !isSaving && !canRetrySave,
                    onOpened = onDirectionsOpened,
                ) { directions ->
                    Row(
                        Modifier.fillMaxWidth().background(OguriTheme.colors.mintContainer, shape)
                            .border(1.dp, OguriTheme.colors.borderSubtle, shape).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(draft.placeTitle ?: draft.name, style = OguriTheme.typography.cardSubtitle, color = OguriTheme.colors.textPrimary)
                            Text(listOfNotNull(placeAddress, stringResource(Res.string.travel_google_maps_attribution)).joinToString(" · "), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                            if (attributions.isNotEmpty()) GooglePlacesAttribution(attributions, includeGoogleBranding = false)
                        }
                        directions()
                    }
                }
            }
            if ((placeId == null || original.details.name != null) && (!comparing || TravelVisitField.NAME in comparisonFields)) {
                TravelPlaceField(draft.name, { onDraftChange(draft.copy(name = it)) }, Res.string.travel_visit_name,
                    Res.string.travel_manual_name_placeholder, TravelVisitDetails.MAX_NAME_LENGTH, canEdit)
            }
            if (!comparing || TravelVisitField.DAY in comparisonFields) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.travel_visit_day), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .background(OguriTheme.colors.surfaceCard, RoundedCornerShape(8.dp))
                        .border(1.dp, OguriTheme.colors.borderStrong, RoundedCornerShape(8.dp))
                        .noRippleClickable(enabled = canEdit, onClick = { showDayMenu = true }).padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(travelDayLabel(dates, draft.dayNumber), Modifier.weight(1f), style = travelTextStyle(15, 18))
                    Icon(painterResource(Res.drawable.ic_travel_chevron_down), null, Modifier.size(20.dp), tint = OguriTheme.colors.textTertiary)
                }
            }
            if (showDayMenu) TravelFormSheet(
                onDismissRequest = { showDayMenu = false },
                title = { Text(stringResource(Res.string.travel_visit_day)) },
                text = {
                    androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max = 320.dp)) {
                        items(dates.dayCount) { index ->
                            TravelTextButton(onClick = { showDayMenu = false; onDraftChange(draft.copy(dayNumber = index + 1)) }) {
                                Text(travelDayLabel(dates, index + 1))
                            }
                        }
                    }
                },
            )
            if ((placeId == null || original.details.location != null) && (!comparing || TravelVisitField.LOCATION in comparisonFields)) {
                TravelPlaceField(draft.location, { onDraftChange(draft.copy(location = it)) }, Res.string.travel_visit_location,
                    Res.string.travel_manual_location_placeholder, TravelVisitDetails.MAX_LOCATION_LENGTH, canEdit)
            }
            if (!comparing || TravelVisitField.NOTE in comparisonFields) {
                TravelNoteField(
                    draft.note, { onDraftChange(draft.copy(note = it)) },
                    if (comparing) Res.string.travel_note_mine else Res.string.travel_visit_note,
                    canEdit, supportingText = Res.string.travel_visit_note_shared.takeUnless { comparing },
                )
            }
            if (!comparing || TravelVisitField.CATEGORY in comparisonFields) {
                TravelCategoryPicker(draft.category, canEdit) { onDraftChange(draft.copy(category = it)) }
            }
            failure?.takeUnless { canRetrySave || (comparing && it == TravelPlanFailure.CONFLICT) }?.let {
                if (it == TravelPlanFailure.CONFLICT || it == TravelPlanFailure.CONNECTION) {
                    TravelInlineFailure(it, onReload, stringResource(Res.string.travel_reload))
                } else {
                    TravelInlineFailure(it)
                }
            }
            if (placeId == null && !comparing) TravelDirectionsButton(null, draft.location, enabled = !isSaving && !canRetrySave, onOpened = onDirectionsOpened)
            }
            Column(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.HorizontalDivider(color = OguriTheme.colors.borderSubtle)
                if (comparing && original != null) TravelTextButton(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), enabled = canEdit, onClick = {
                    onDraftChange(draft.useSavedValues())
                }) { Text(stringResource(Res.string.travel_use_saved), style = OguriTheme.typography.labelMedium, color = OguriTheme.colors.textTertiary) }
                else if (draft.visit != null) TravelTextButton(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), enabled = canEdit, onClick = { confirmRemoval = true }) {
                    Text(stringResource(Res.string.travel_delete_visit_action), style = OguriTheme.typography.labelMedium, color = OguriTheme.colors.error)
                }
                TravelPrimaryButton(enabled = canEdit && (placeId != null || draft.name.isNotBlank()), onClick = onApply, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), textStyle = OguriTheme.typography.cardTitle) {
                    Text(stringResource(if (comparing) Res.string.travel_apply_mine else Res.string.travel_apply))
                }
            }
        }
    }

    if (confirmDiscard) {
        TravelDiscardConfirmation(
            onKeep = { confirmDiscard = false },
            onDiscard = { confirmDiscard = false; onDraftChange(null) },
        )
    }
    if (confirmRemoval) {
        ConfirmAlertDialog(
            iconResource = Res.drawable.ic_alert,
            titleText = stringResource(Res.string.travel_delete_visit_title),
            messageText = draft.placeTitle ?: draft.name,
            confirmButtonText = stringResource(Res.string.travel_delete),
            cancelButtonText = stringResource(Res.string.travel_cancel),
            onDismissRequest = { confirmRemoval = false },
            onConfirmClick = {
                confirmRemoval = false
                onRemove()
            },
            onCancelClick = { confirmRemoval = false },
        )
    }
}

@Composable
internal fun TravelLeavePendingSave(onLeave: () -> Unit) {
    Text(stringResource(Res.string.travel_pending_save_preserved), style = OguriTheme.typography.bodySmall)
    TravelTextButton(onClick = onLeave) { Text(stringResource(Res.string.travel_leave_pending_save)) }
}
