package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigong.oguri.core.designsystem.Mint10
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral30
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlaceAttribution
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitCategory
import com.bigong.oguri.domain.model.TravelVisitDetails
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelPlaceSearchSheet(
    state: TravelPlannerUiState,
    plan: TravelPlan,
    onQuery: (String) -> Unit,
    onSelect: (TravelPlace?) -> Unit,
    onAdd: (TravelPlace, Int, TravelVisitCategory?, String?, String?) -> Unit,
    onShowVisit: (TravelVisit) -> Unit,
    onDismiss: () -> Unit,
    onRetrySave: () -> Unit,
    onLeaveWithPendingSave: () -> Unit,
    onReload: () -> Unit,
    onManualAdd: () -> Unit,
    onDraftChange: (TravelVisitDraft?) -> Unit,
    onApplyManual: () -> Unit,
) {
    val search = state.placeSearch ?: return
    val colors = OguriTheme.colors
    val focus = LocalFocusManager.current
    val day = state.selectedDay
    val draft = state.visitDraft
    var manual by rememberSaveable { mutableStateOf(draft != null) }
    var searchCategory by rememberSaveable { mutableStateOf<TravelVisitCategory?>(null) }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    var openAfterDiscard by remember { mutableStateOf<TravelVisit?>(null) }
    val selected = search.selected
    val canDismiss by rememberUpdatedState(!state.isSaving && !state.canRetrySave)
    val canEdit = canDismiss && !state.isLoading && !state.needsLatestPlan && !state.isOffline
    val hasDraft by rememberUpdatedState(draft != null && draft.hasPlaceInput())
    val close = {
        if (canDismiss) {
            openAfterDiscard = null
            if (hasDraft) confirmDiscard = true
            else {
                if (draft != null) onDraftChange(null)
                onDismiss()
            }
        }
    }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = {
        if (it != SheetValue.Hidden) true
        else if (!canDismiss) false
        else if (hasDraft) {
            confirmDiscard = true
            false
        } else true
    })
    val targetDay = if (manual) draft?.dayNumber ?: day else day
    val existing = selected?.let { place ->
        plan.visits.firstOrNull { it.dayNumber == targetDay && it.details.googlePlaceId == place.id }
    }

    ModalBottomSheet(
        containerColor = colors.backgroundSecondary,
        onDismissRequest = close,
        sheetState = sheet,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        dragHandle = {
            Box(Modifier.padding(top = 8.dp, bottom = 8.dp).size(width = 36.dp, height = 4.dp)
                .background(colors.borderStrong, CircleShape))
        },
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.88f).imePadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp, top = 0.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(Res.string.travel_add_visit), style = travelTextStyle(18, 21, FontWeight.SemiBold), color = Neutral100)
                Text(
                    travelDayLabel(plan.summary.details.dates, targetDay),
                    Modifier.weight(1f),
                    style = travelTextStyle(12, 14, FontWeight.Medium),
                    color = colors.textTertiary,
                )
                TravelIconButton(onClick = close, enabled = canDismiss) {
                    Icon(
                        painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close),
                        modifier = Modifier.size(22.dp), tint = colors.textPrimary,
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 16.dp)
                    .background(colors.surfaceCard, CircleShape).border(1.dp, colors.borderSubtle, CircleShape).padding(2.dp),
            ) {
                TravelChoiceChip(stringResource(Res.string.travel_google_search_tab), !manual, canEdit, {
                    focus.clearFocus()
                    manual = false
                }, Modifier.weight(1f), segmented = true)
                TravelChoiceChip(stringResource(Res.string.travel_manual_add), manual, canEdit, {
                    focus.clearFocus()
                    onManualAdd()
                    manual = true
                }, Modifier.weight(1f), segmented = true)
            }
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (state.isSaving || (!manual && search.isLoading)) {
                    item { TravelLoading(Modifier.fillMaxWidth()) }
                }
                state.failure?.let { failure ->
                    item {
                        if (state.needsLatestPlan) {
                            TravelInlineFailure(failure, onReload, stringResource(Res.string.travel_reload))
                        } else {
                            TravelInlineFailure(failure, onRetry = onRetrySave.takeIf { state.canRetrySave })
                        }
                    }
                }
                if (manual) {
                    if (draft != null) item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            TravelPlaceField(
                                draft.name, { onDraftChange(draft.copy(name = it)) }, Res.string.travel_visit_name,
                                Res.string.travel_manual_name_placeholder, TravelVisitDetails.MAX_NAME_LENGTH, canEdit,
                            )
                            TravelPlaceField(
                                draft.location, { onDraftChange(draft.copy(location = it)) }, Res.string.travel_visit_location,
                                Res.string.travel_manual_location_placeholder, TravelVisitDetails.MAX_LOCATION_LENGTH, canEdit,
                                supportingText = Res.string.travel_manual_location_hint,
                            )
                            TravelPlaceField(
                                draft.note, { onDraftChange(draft.copy(note = it)) }, Res.string.travel_visit_note,
                                Res.string.travel_manual_note_placeholder, TravelVisitDetails.MAX_NOTE_LENGTH, canEdit, multiline = true,
                            )
                            TravelCategoryPicker(draft.category, canEdit) { onDraftChange(draft.copy(category = it)) }
                        }
                    }
                } else {
                    item {
                        TravelPlaceInput(
                            value = search.query,
                            onChange = { if (it.length <= 120) onQuery(it) },
                            label = stringResource(Res.string.travel_search_places),
                            placeholder = stringResource(Res.string.travel_search_places),
                            enabled = canEdit,
                            leadingIcon = {
                                Icon(painterResource(Res.drawable.ic_travel_search), null, Modifier.size(20.dp), tint = colors.textTertiary)
                            },
                        )
                    }
                    when {
                        search.failure != null -> item {
                            TravelInlineFailure(
                                search.failure, onRetry = { onQuery(search.query) },
                                message = if (search.failure == TravelPlanFailure.CONNECTION) {
                                    stringResource(Res.string.travel_place_search_unavailable)
                                } else null,
                            )
                        }
                        search.results.isNotEmpty() -> {
                            item {
                                Text(
                                    stringResource(Res.string.travel_search_city, plan.summary.details.city),
                                    style = travelTextStyle(11, 13), color = colors.textTertiary,
                                )
                            }
                            items(search.results, key = TravelPlace::id) { place ->
                                val checked = selected?.id == place.id
                                Row(
                                    Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(8.dp))
                                        .background(if (checked) Mint10 else Color.Transparent)
                                        .selectable(checked, interactionSource = null, indication = null, enabled = canEdit, role = Role.RadioButton) {
                                            focus.clearFocus()
                                            onSelect(place)
                                        }.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(place.name, style = travelTextStyle(15, 18, FontWeight.Medium), color = Neutral100)
                                        place.address?.let { Text(it, style = travelTextStyle(12, 14), color = colors.textTertiary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }
                                    }
                                    TravelSearchSelectionMark(checked)
                                }
                                if (!checked) HorizontalDivider(color = colors.borderSubtle)
                            }
                            item { GooglePlacesAttribution(search.results.flatMap(TravelPlace::attributions)) }
                        }
                        !search.isLoading && search.query.trim().length >= 2 -> item {
                            Text(stringResource(Res.string.travel_search_empty), color = colors.textTertiary)
                        }
                    }
                    if (selected != null) item {
                        existing?.let { visit ->
                            TravelTextButton(
                                onClick = {
                                    if (hasDraft) {
                                        openAfterDiscard = visit
                                        confirmDiscard = true
                                    } else {
                                        onShowVisit(visit)
                                    }
                                },
                                enabled = canEdit,
                            ) {
                                Text(stringResource(Res.string.travel_show_existing_visit))
                            }
                        }
                    }
                    item {
                        Box(Modifier.padding(top = if (selected == null) 12.dp else 16.dp)) {
                            TravelCategoryPicker(searchCategory, canEdit) { searchCategory = it }
                        }
                    }
                }
                if (state.canRetrySave && state.pendingSaveStored && !state.isSaving) {
                    item { TravelLeavePendingSave(onLeaveWithPendingSave) }
                }
            }
            TravelBottomAction(
                enabled = canEdit && if (manual) draft?.name?.isNotBlank() == true
                    else selected != null,
                onClick = {
                    focus.clearFocus()
                    if (manual) onApplyManual()
                    else if (selected != null) {
                        onAdd(
                            selected,
                            targetDay,
                            searchCategory,
                            null,
                            null,
                        )
                    }
                },
            ) {
                Text(
                    when {
                        existing != null && !manual -> stringResource(Res.string.travel_add_again)
                        else -> stringResource(Res.string.travel_add_to_day, targetDay)
                    },
                )
            }
        }
    }
    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(Res.string.travel_discard_title)) },
            confirmButton = {
                TravelTextButton(onClick = {
                    confirmDiscard = false
                    onDraftChange(null)
                    val visit = openAfterDiscard
                    openAfterDiscard = null
                    if (visit == null) onDismiss() else onShowVisit(visit)
                }) { Text(stringResource(Res.string.travel_discard_draft)) }
            },
            dismissButton = {
                TravelTextButton(onClick = { confirmDiscard = false }) { Text(stringResource(Res.string.travel_cancel)) }
            },
        )
    }
}

private fun TravelVisitDraft.hasPlaceInput(): Boolean =
    name.isNotBlank() || location.isNotBlank() || note.isNotBlank() || category != null

@Composable
private fun TravelPlaceField(
    value: String,
    onChange: (String) -> Unit,
    label: org.jetbrains.compose.resources.StringResource,
    placeholder: org.jetbrains.compose.resources.StringResource,
    limit: Int,
    enabled: Boolean,
    multiline: Boolean = false,
    supportingText: org.jetbrains.compose.resources.StringResource? = null,
) {
    val fieldLabel = stringResource(label)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(fieldLabel, style = travelTextStyle(12, 14, FontWeight.Medium), color = OguriTheme.colors.textTertiary)
        TravelPlaceInput(
            value = value,
            onChange = { if (it.length <= limit) onChange(it) },
            label = fieldLabel,
            placeholder = stringResource(placeholder),
            enabled = enabled,
            multiline = multiline,
        )
        if (supportingText != null) {
            Text(
                stringResource(supportingText),
                Modifier.padding(horizontal = 4.dp),
                style = travelTextStyle(11, 13),
                color = OguriTheme.colors.textTertiary,
            )
        }
    }
}

@Composable
private fun TravelPlaceInput(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    placeholder: String,
    enabled: Boolean,
    multiline: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    val style = travelTextStyle(15, if (multiline) 21 else 18)
    val focusSource = remember { MutableInteractionSource() }
    val focused by focusSource.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
        enabled = enabled,
        singleLine = !multiline,
        maxLines = if (multiline) 5 else 1,
        textStyle = style.copy(color = Neutral100),
        cursorBrush = SolidColor(Neutral100),
        interactionSource = focusSource,
        decorationBox = { innerField ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = if (multiline) 89.dp else 48.dp)
                    .background(Neutral0, shape).border(1.dp, if (focused) Neutral50 else Neutral30, shape)
                    .padding(horizontal = 13.dp, vertical = if (multiline) 13.dp else 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically,
            ) {
                leadingIcon?.invoke()
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, style = style, color = com.bigong.oguri.core.designsystem.Neutral40)
                    innerField()
                }
            }
        },
    )
}

@Composable
private fun TravelSearchSelectionMark(selected: Boolean) {
    Icon(
        painterResource(if (selected) Res.drawable.ic_travel_selected else Res.drawable.ic_travel_unselected),
        null, Modifier.size(22.dp), tint = Color.Unspecified,
    )
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
                TravelTextButton(onClick = { runCatching { uriHandler.openUri(uri) } }) { Text(attribution.provider) }
            }
        }
    }
}
