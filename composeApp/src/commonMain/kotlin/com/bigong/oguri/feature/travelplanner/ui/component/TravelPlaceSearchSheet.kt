package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.consumeVerticalDragForBottomSheetContent
import com.bigong.oguri.core.util.extension.dismissKeyboardOnOutsideTouch
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlaceAttribution
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelVisitCategory
import com.bigong.oguri.domain.model.TravelVisitDetails
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val VISIBLE_PLACE_RESULTS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelPlaceSearchSheet(
    state: TravelPlannerUiState,
    plan: TravelPlan,
    onQuery: (String) -> Unit,
    onSelectPlace: (String) -> Unit,
    onSelectCategory: (TravelVisitCategory?) -> Unit,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit,
    onRetrySave: () -> Unit,
    onLeaveWithPendingSave: () -> Unit,
    onReload: () -> Unit,
    onManualAdd: () -> Unit,
    onDraftChange: (TravelVisitDraft?) -> Unit,
    onApplyManual: () -> Unit,
    onSearchCity: (String) -> Unit = {},
) {
    val search = state.placeSearch ?: return
    val colors = OguriTheme.colors
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val keyboardVisible by rememberUpdatedState(WindowInsets.ime.getBottom(LocalDensity.current) > 0)
    val day = state.selectedDay
    val draft = state.visitDraft
    var manual by rememberSaveable { mutableStateOf(draft != null) }
    var searchNote by rememberSaveable { mutableStateOf("") }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    val selected = search.selection?.place
    val canDismiss by rememberUpdatedState(!state.isSaving && !state.canRetrySave)
    val canEdit = canDismiss && !state.isLoading && !state.needsLatestPlan && !state.isOffline
    val hasDraft by rememberUpdatedState(hasUnsavedPlaceInput(draft, selected, searchNote))
    val close = {
        if (canDismiss) {
            if (hasDraft) confirmDiscard = true
            else {
                if (draft != null) onDraftChange(null)
                onDismiss()
            }
        }
    }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = {
        if (it != SheetValue.Hidden) true
        else if (keyboardVisible) {
            focus.clearFocus(force = true)
            keyboard?.hide()
            false
        } else if (!canDismiss) false
        else if (hasDraft) {
            confirmDiscard = true
            false
        } else true
    })
    val dismissSheet = rememberTravelSheetDismiss(sheet, close)
    val targetDay = if (manual) draft?.dayNumber ?: day else day

    ModalBottomSheet(scrimColor = TravelSheetScrim,
        containerColor = colors.backgroundSecondary,
        contentColor = Neutral100,
        onDismissRequest = { if (!sheet.isVisible) close() },
        sheetState = sheet,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        dragHandle = {
            Box(Modifier.padding(top = 8.dp, bottom = 8.dp).size(width = 36.dp, height = 4.dp)
                .background(colors.borderStrong, CircleShape))
        },
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.92f).dismissKeyboardOnOutsideTouch()) {
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
                TravelIconButton(onClick = dismissSheet, enabled = canDismiss) {
                    Icon(
                        painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close),
                        modifier = Modifier.size(22.dp), tint = Neutral100,
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
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)
                    .consumeVerticalDragForBottomSheetContent(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (state.isSaving) {
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
                    if (draft != null) item(key = "manual-place-form") {
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
                            TravelNoteField(
                                draft.note, { onDraftChange(draft.copy(note = it)) }, Res.string.travel_visit_note, canEdit,
                            )
                            TravelCategoryPicker(draft.category, canEdit) { onDraftChange(draft.copy(category = it)) }
                        }
                    }
                } else {
                    if (plan.summary.details.cities.size > 1) item {
                        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            plan.summary.details.cities.forEach { city ->
                                TravelChoiceChip(
                                    plan.summary.details.catalogCities[city]?.name ?: city,
                                    (state.searchCity ?: plan.summary.details.city) == city,
                                    canEdit,
                                    { onSearchCity(city) },
                                )
                            }
                        }
                    }
                    item(key = "place-search-input") {
                        TravelPlaceInput(
                            value = search.query,
                            onChange = onQuery,
                            limit = TRAVEL_SEARCH_QUERY_LIMIT,
                            label = stringResource(Res.string.travel_search_places),
                            placeholder = stringResource(Res.string.travel_search_places),
                            enabled = canEdit,
                            leadingIcon = {
                                Icon(painterResource(Res.drawable.ic_travel_search), null, Modifier.size(20.dp), tint = colors.textTertiary)
                            },
                        )
                    }
                    when {
                        search.isLoading -> item { TravelSearchResultsSkeleton() }
                        search.failure != null -> item {
                            if (search.failure == TravelPlanFailure.CONNECTION) {
                                Column(Modifier.fillMaxWidth().padding(vertical = 44.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        stringResource(Res.string.travel_place_search_unavailable),
                                        style = OguriTheme.typography.cardSubtitle,
                                        modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite },
                                    )
                                    Text(stringResource(Res.string.travel_search_preserved_hint), style = OguriTheme.typography.bodySmall, color = colors.textTertiary)
                                    Row {
                                        TravelTextButton(onClick = { onQuery(search.query) }, enabled = canEdit) { Text(stringResource(Res.string.travel_retry)) }
                                        TravelTextButton(onClick = { onManualAdd(); manual = true }, enabled = canEdit) { Text(stringResource(Res.string.travel_manual_add), color = colors.textTertiary) }
                                    }
                                }
                            } else TravelInlineFailure(search.failure, onRetry = { onQuery(search.query) })
                        }
                        search.results.isNotEmpty() -> {
                            item {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        (state.searchCity ?: plan.summary.details.city).let { city ->
                                            stringResource(Res.string.travel_search_city, plan.summary.details.catalogCities[city]?.name ?: city)
                                        },
                                        modifier = Modifier.weight(1f),
                                        style = travelTextStyle(11, 13),
                                        color = colors.textTertiary,
                                    )
                                    Text(
                                        stringResource(Res.string.travel_google_maps_attribution),
                                        style = OguriTheme.typography.caption,
                                        color = colors.textTertiary,
                                        softWrap = false,
                                    )
                                }
                            }
                            item(key = "place-search-results") {
                                key(search.query, state.searchCity) {
                                    TravelPlaceSearchResults(search.results, selected, canEdit) { place ->
                                        focus.clearFocus()
                                        onSelectPlace(place.id)
                                    }
                                }
                            }
                            val attributions = search.results.flatMap(TravelPlace::attributions)
                            if (attributions.isNotEmpty()) {
                                item { GooglePlacesAttribution(attributions, includeGoogleBranding = false) }
                            }
                        }
                        !search.isLoading && search.query.trim().length >= 2 -> item {
                            Column(Modifier.fillMaxWidth().padding(vertical = 44.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(Res.string.travel_search_empty), style = OguriTheme.typography.cardSubtitle)
                                Text(stringResource(Res.string.travel_search_empty_hint), style = OguriTheme.typography.bodySmall, color = colors.textTertiary)
                                TravelTextButton(onClick = { onManualAdd(); manual = true }, enabled = canEdit) { Text(stringResource(Res.string.travel_manual_action)) }
                            }
                        }
                    }
                    if (search.results.isNotEmpty()) item {
                        Column(
                            Modifier.padding(top = if (selected == null) 12.dp else 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            TravelNoteField(
                                note = searchNote,
                                onChange = { searchNote = it },
                                label = Res.string.travel_visit_note,
                                enabled = canEdit,
                            )
                            TravelCategoryPicker(search.selection?.category, canEdit && selected != null, onSelectCategory)
                        }
                    }
                }
                if (state.canRetrySave && state.pendingSaveStored && !state.isSaving) {
                    item { TravelLeavePendingSave(onLeaveWithPendingSave) }
                }
            }
            if (manual || search.results.isNotEmpty()) TravelBottomAction(
                bottomPadding = if (keyboardVisible) 0.dp else 20.dp,
                enabled = canEdit && if (manual) draft?.name?.isNotBlank() == true
                    else selected != null,
                onClick = {
                    focus.clearFocus()
                    if (manual) onApplyManual()
                    else onAdd(searchNote)
                },
            ) {
                Text(
                    when {
                        else -> stringResource(Res.string.travel_add_to_day, targetDay)
                    },
                )
            }
        }
    }
    if (confirmDiscard) {
        TravelDiscardConfirmation(
            onKeep = { confirmDiscard = false },
            onDiscard = {
                confirmDiscard = false
                onDraftChange(null)
                onDismiss()
            },
        )
    }
}

internal fun hasUnsavedPlaceInput(
    draft: TravelVisitDraft?,
    selected: TravelPlace?,
    searchNote: String,
): Boolean = draft?.hasPlaceInput() == true || selected != null || searchNote.isNotBlank()

private fun TravelVisitDraft.hasPlaceInput(): Boolean =
    name.isNotBlank() || location.isNotBlank() || note.isNotBlank() || category != null

@Composable
internal fun TravelPlaceField(
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
            onChange = onChange,
            limit = limit,
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
    limit: Int,
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
    TravelTextField(
        value = value,
        onValueChange = onChange,
        limit = limit,
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
                    .background(OguriTheme.colors.surfaceCard, shape).border(1.dp, if (focused) OguriTheme.colors.textSecondary else OguriTheme.colors.borderStrong, shape)
                    .padding(horizontal = 13.dp, vertical = if (multiline) 13.dp else 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically,
            ) {
                leadingIcon?.invoke()
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, style = style, color = Neutral40)
                    innerField()
                }
            }
        },
    )
}

@Composable
private fun TravelPlaceSearchResults(
    places: List<TravelPlace>,
    selected: TravelPlace?,
    enabled: Boolean,
    onSelect: (TravelPlace) -> Unit,
) {
    val colors = OguriTheme.colors
    val nameStyle = travelTextStyle(15, 18, FontWeight.Medium)
    val addressStyle = travelTextStyle(12, 14)
    val rowPadding = 8.dp
    val rowHeight = with(LocalDensity.current) {
        (nameStyle.lineHeight.toDp() + addressStyle.lineHeight.toDp() + rowPadding * 2).coerceAtLeast(56.dp)
    }
    val dividerHeight = DividerDefaults.Thickness
    Box(
        Modifier.fillMaxWidth().heightIn(max = (rowHeight + dividerHeight) * VISIBLE_PLACE_RESULTS)
            .consumeVerticalDragForBottomSheetContent(),
    ) {
        LazyColumn(Modifier.fillMaxWidth()) {
            items(places, key = TravelPlace::id) { place ->
                val checked = selected?.id == place.id
                Row(
                    Modifier.fillMaxWidth().heightIn(min = rowHeight).clip(RoundedCornerShape(8.dp))
                        .background(if (checked) OguriTheme.colors.mintContainer else Color.Transparent)
                        .selectable(checked, interactionSource = null, indication = null, enabled = enabled, role = Role.RadioButton) {
                            onSelect(place)
                        }.padding(horizontal = 12.dp, vertical = rowPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(place.name, style = nameStyle, color = Neutral100)
                        place.address?.let {
                            Text(it, style = addressStyle, color = colors.textTertiary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                    TravelSearchSelectionMark(checked)
                }
                if (checked) Spacer(Modifier.height(dividerHeight))
                else HorizontalDivider(color = colors.borderSubtle)
            }
        }
    }
}

@Composable
private fun TravelSearchSelectionMark(selected: Boolean) {
    Icon(
        painterResource(if (selected) Res.drawable.ic_travel_selected else Res.drawable.ic_travel_unselected),
        null, Modifier.size(22.dp), tint = Color.Unspecified,
    )
}

@Composable
internal fun GooglePlacesAttribution(attributions: List<TravelPlaceAttribution>, includeGoogleBranding: Boolean = true) {
    val uriHandler = LocalUriHandler.current
    val textColor = OguriTheme.colors.textTertiary
    Column(Modifier.fillMaxWidth().heightIn(min = 24.dp).padding(vertical = 4.dp)) {
        if (includeGoogleBranding) Text(
            stringResource(Res.string.travel_google_maps_attribution),
            color = textColor,
            style = OguriTheme.typography.caption,
            softWrap = false,
        )
        attributions.distinct().forEach { attribution ->
            val uri = attribution.providerUri
            if (uri == null) {
                Text(attribution.provider, style = OguriTheme.typography.bodySmall)
            } else {
                TravelTextButton(onClick = { runCatching { uriHandler.openUri(uri) } }) { Text(attribution.provider) }
            }
        }
    }
}
