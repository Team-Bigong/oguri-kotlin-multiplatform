package com.bigong.oguri.feature.travelplanner.ui.component

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
import com.bigong.oguri.core.designsystem.Neutral0
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
    ModalBottomSheet(
        containerColor = com.bigong.oguri.core.designsystem.Neutral5,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        onDismissRequest = { if (canDismiss) dismissDraft() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.88f).imePadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.travel_edit_visit), Modifier.weight(1f), style = OguriTheme.typography.cardTitle)
                TravelIconButton(onClick = dismissDraft, enabled = canDismiss) { Icon(painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close), Modifier.size(22.dp)) }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TravelConflictComparison(draft, dates)
            if (isSaving) TravelLoading(modifier = Modifier.fillMaxWidth())
            if (placeId == null || original.details.name != null) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { if (it.length <= TravelVisitDetails.MAX_NAME_LENGTH) onDraftChange(draft.copy(name = it)) },
                    label = { Text(stringResource(Res.string.travel_visit_name)) },
                    singleLine = true,
                    enabled = canEdit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = OguriTheme.typography.bodyLarge,
                    colors = TravelFieldColors(),
                )
            } else if (draft.placeTitle != null) {
                Text(draft.placeTitle, style = OguriTheme.typography.cardTitle)
                GooglePlacesAttribution(attributions)
            }
            TravelOutlineButton(onClick = { showDayMenu = true }, enabled = canEdit, modifier = Modifier.fillMaxWidth()) {
                Text(travelDayLabel(dates, draft.dayNumber))
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
            if (placeId == null || original.details.location != null) {
                OutlinedTextField(
                    value = draft.location,
                    onValueChange = { if (it.length <= TravelVisitDetails.MAX_LOCATION_LENGTH) onDraftChange(draft.copy(location = it)) },
                    label = { Text(stringResource(Res.string.travel_visit_location)) },
                    singleLine = true,
                    enabled = canEdit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = OguriTheme.typography.bodyLarge,
                    colors = TravelFieldColors(),
                )
            }
            OutlinedTextField(
                value = draft.note,
                onValueChange = { if (it.length <= TravelVisitDetails.MAX_NOTE_LENGTH) onDraftChange(draft.copy(note = it)) },
                label = { Text(stringResource(Res.string.travel_visit_note)) },
                supportingText = { Text(stringResource(Res.string.travel_visit_note_shared)) },
                maxLines = 4,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                textStyle = OguriTheme.typography.bodyLarge,
                colors = TravelFieldColors(),
            )
            TravelCategoryPicker(draft.category, canEdit) { onDraftChange(draft.copy(category = it)) }
            failure?.let {
                if (it == TravelPlanFailure.CONFLICT || (it == TravelPlanFailure.CONNECTION && !canRetrySave)) {
                    TravelInlineFailure(it, onReload, stringResource(Res.string.travel_reload))
                } else {
                    TravelInlineFailure(it, onRetrySave.takeIf { canRetrySave })
                }
            }
            if (canRetrySave && !isSaving && onLeaveWithPendingSave != null) {
                TravelLeavePendingSave(onLeaveWithPendingSave)
            }
            TravelDirectionsButton(placeId, draft.placeTitle ?: draft.location, enabled = !isSaving && !canRetrySave, onOpened = onDirectionsOpened)
            }
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (draft.visit != null) TravelTextButton(enabled = canEdit, onClick = { confirmRemoval = true }) {
                    Text(stringResource(Res.string.travel_delete), color = com.bigong.oguri.core.designsystem.Orange50)
                }
                TravelPrimaryButton(enabled = canEdit && (placeId != null || draft.name.isNotBlank()), onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.travel_apply))
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
    Text(stringResource(Res.string.travel_pending_save_preserved), style = MaterialTheme.typography.bodySmall)
    TravelTextButton(onClick = onLeave) { Text(stringResource(Res.string.travel_leave_pending_save)) }
}
