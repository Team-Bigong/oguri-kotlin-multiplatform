package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.domain.model.TravelRecentChanges
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_change_day_reordered
import oguri.composeapp.generated.resources.travel_change_moved_now
import oguri.composeapp.generated.resources.travel_change_open_day
import oguri.composeapp.generated.resources.travel_change_owner
import oguri.composeapp.generated.resources.travel_change_removed_now
import oguri.composeapp.generated.resources.travel_change_trip
import oguri.composeapp.generated.resources.travel_change_visit_added
import oguri.composeapp.generated.resources.travel_change_visit_edited
import oguri.composeapp.generated.resources.travel_change_visit_removed
import oguri.composeapp.generated.resources.travel_change_visit_undone
import oguri.composeapp.generated.resources.travel_changed_place
import oguri.composeapp.generated.resources.travel_changes_trip
import oguri.composeapp.generated.resources.travel_choose_time_zone
import oguri.composeapp.generated.resources.travel_city
import oguri.composeapp.generated.resources.travel_dates_value
import oguri.composeapp.generated.resources.travel_details_keep_local_times
import oguri.composeapp.generated.resources.travel_details_move_unassigned
import oguri.composeapp.generated.resources.travel_edit_trip
import oguri.composeapp.generated.resources.travel_end_date
import oguri.composeapp.generated.resources.travel_no_recent_changes
import oguri.composeapp.generated.resources.travel_open_source
import oguri.composeapp.generated.resources.travel_recent_changes
import oguri.composeapp.generated.resources.travel_recommended_dates
import oguri.composeapp.generated.resources.travel_search_close
import oguri.composeapp.generated.resources.travel_source
import oguri.composeapp.generated.resources.travel_source_explanation
import oguri.composeapp.generated.resources.travel_start_date
import oguri.composeapp.generated.resources.travel_time_zone
import oguri.composeapp.generated.resources.travel_title
import oguri.composeapp.generated.resources.travel_trip_info
import org.jetbrains.compose.resources.stringResource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TravelDetailsDialog(
    plan: TravelPlan,
    busy: Boolean,
    failure: TravelPlanFailure?,
    onDismiss: () -> Unit,
    onReload: () -> Unit,
    onApply: (TravelPlanDetails, Int) -> Unit,
    canEdit: Boolean,
    startEditing: Boolean,
    onOpenSource: (TravelPlanSource) -> Unit,
) {
    val current = plan.summary.details
    var editing by remember(plan.summary.id) { mutableStateOf(startEditing) }
    val showEditor = canEdit && editing
    var title by remember(plan.revision) { mutableStateOf(current.title) }
    var city by remember(plan.revision) { mutableStateOf(current.city) }
    var zone by remember(plan.revision) { mutableStateOf(current.timeZone.id) }
    var start by remember(plan.revision) { mutableStateOf(current.dates.start.toString()) }
    var end by remember(plan.revision) { mutableStateOf(current.dates.endInclusive.toString()) }
    val dates = runCatching { TravelDates(LocalDate.parse(start), LocalDate.parse(end)) }.getOrNull()
    val timeZone = zone.trim().takeIf { it in TimeZone.availableZoneIds }?.let(TimeZone::of)
    val affected = if (dates == null) 0 else plan.visits.count { it.dayNumber > dates.dayCount }
    val valid =
        title.isNotBlank() &&
            city.isNotBlank() &&
            title.length <= TravelPlanDetails.MAX_TEXT_LENGTH &&
            city.length <= TravelPlanDetails.MAX_TEXT_LENGTH &&
            dates != null &&
            timeZone != null
    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        containerColor = Neutral0,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (showEditor) Res.string.travel_edit_trip else Res.string.travel_trip_info)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (showEditor) {
                    OutlinedTextField(
                        title,
                        { title = it },
                        label = { Text(stringResource(Res.string.travel_title)) },
                        enabled = !busy,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = OguriTheme.typography.bodyLarge,
                        colors = TravelFieldColors(),
                    )
                    OutlinedTextField(
                        city,
                        { city = it },
                        label = { Text(stringResource(Res.string.travel_city)) },
                        enabled = !busy,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = OguriTheme.typography.bodyLarge,
                        colors = TravelFieldColors(),
                    )
                    OutlinedTextField(
                        zone,
                        { zone = it },
                        label = { Text(stringResource(Res.string.travel_choose_time_zone)) },
                        enabled = !busy,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = OguriTheme.typography.bodyLarge,
                        colors = TravelFieldColors(),
                    )
                    OutlinedTextField(
                        start,
                        { start = it },
                        label = { Text(stringResource(Res.string.travel_start_date)) },
                        enabled = !busy,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = OguriTheme.typography.bodyLarge,
                        colors = TravelFieldColors(),
                    )
                    OutlinedTextField(
                        end,
                        { end = it },
                        label = { Text(stringResource(Res.string.travel_end_date)) },
                        enabled = !busy,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = OguriTheme.typography.bodyLarge,
                        colors = TravelFieldColors(),
                    )
                    if (affected > 0) Text(stringResource(Res.string.travel_details_move_unassigned, affected))
                    if (zone.trim() != current.timeZone.id) Text(stringResource(Res.string.travel_details_keep_local_times))
                } else {
                    Text(current.title, style = OguriTheme.typography.cardTitle)
                    Text(current.city)
                    Text(
                        stringResource(
                            Res.string.travel_dates_value,
                            current.dates.start.toString(),
                            current.dates.endInclusive.toString(),
                        ),
                    )
                    Text(stringResource(Res.string.travel_time_zone, current.timeZone.id))
                    current.source?.let { source ->
                        Text(stringResource(Res.string.travel_source), style = OguriTheme.typography.cardSubtitle)
                        if (source.startDate != null && source.endDate != null) {
                            Text(
                                stringResource(Res.string.travel_recommended_dates, source.startDate.toString(), source.endDate.toString()),
                            )
                        }
                        Text(stringResource(Res.string.travel_source_explanation))
                        TravelTextButton(enabled = !busy, onClick = { onOpenSource(source) }) {
                            Text(stringResource(Res.string.travel_open_source))
                        }
                    }
                }
                if (failure != null) TravelInlineFailure(failure, onRetry = onReload.takeIf { failure == TravelPlanFailure.CONFLICT })
            }
        },
        confirmButton = {
            if (showEditor) {
                TravelTextButton(enabled = !busy && valid && failure != TravelPlanFailure.CONFLICT, onClick = {
                    if (dates != null && timeZone != null) {
                        onApply(TravelPlanDetails(city.trim(), timeZone, title.trim(), dates, current.source), affected)
                    }
                }) { Text(stringResource(Res.string.travel_apply)) }
            } else if (canEdit) {
                TravelTextButton(enabled = !busy, onClick = { editing = true }) { Text(stringResource(Res.string.travel_edit_trip)) }
            }
        },
        dismissButton = {
            TravelTextButton(enabled = !busy, onClick = { if (showEditor) editing = false else onDismiss() }) {
                Text(stringResource(if (showEditor) Res.string.travel_cancel else Res.string.travel_search_close))
            }
        },
    )
}

@Composable
internal fun TravelRecentChangesDialog(
    changes: TravelRecentChanges?,
    dates: TravelDates,
    onDismiss: () -> Unit,
    onSelectDay: (Int) -> Unit,
    busy: Boolean = false,
    failure: TravelPlanFailure? = null,
    onRetry: (() -> Unit)? = null,
) {
    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        containerColor = Neutral0,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.travel_recent_changes)) },
        text = {
            if (busy) {
                TravelLoading(Modifier.fillMaxWidth())
            } else if (failure != null) {
                TravelInlineFailure(failure, onRetry = onRetry)
            } else if (changes == null || changes.changes.isEmpty()) {
                Text(stringResource(Res.string.travel_no_recent_changes))
            } else {
                val currentDates = changes.dates ?: dates
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    for ((day, entries) in changes.changes.groupBy { it.dayNumber }) {
                        item(key = "change-day-$day") {
                            Text(
                                if (day == null) stringResource(Res.string.travel_changes_trip) else travelDayLabel(currentDates, day),
                                style = OguriTheme.typography.cardTitle,
                            )
                        }
                        items(entries, key = { it.revision }) { entry ->
                            val destination = entry.destinationDay(currentDates)
                            val label =
                                when (entry.kind) {
                                    "VISIT_ADDED" -> Res.string.travel_change_visit_added
                                    "VISIT_EDITED" -> Res.string.travel_change_visit_edited
                                    "VISIT_UNDONE" -> Res.string.travel_change_visit_undone
                                    "VISIT_REMOVED" -> Res.string.travel_change_visit_removed
                                    "DAY_REORDERED" -> Res.string.travel_change_day_reordered
                                    "OWNER_CHANGED" -> Res.string.travel_change_owner
                                    else -> Res.string.travel_change_trip
                                }
                            Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(label), style = OguriTheme.typography.cardSubtitle)
                                entry.occurredAt?.let { instant ->
                                    Text(
                                        changes.timeZone?.let { instant.toLocalDateTime(it).toString() + " · " + it.id }
                                            ?: instant.toString(),
                                    )
                                }
                                if (entry.visitId != null) Text(entry.visitName ?: stringResource(Res.string.travel_changed_place))
                                if (entry.isVisitRemoved) {
                                    Text(stringResource(Res.string.travel_change_removed_now))
                                } else if (destination != null) {
                                    if (entry.visitId != null && entry.dayNumber != destination) {
                                        Text(stringResource(Res.string.travel_change_moved_now, travelDayLabel(currentDates, destination)))
                                    }
                                    TravelTextButton(onClick = { onSelectDay(destination) }) {
                                        Text(stringResource(Res.string.travel_change_open_day, travelDayLabel(currentDates, destination)))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TravelTextButton(onClick = onDismiss) { Text(stringResource(Res.string.travel_search_close)) } },
    )
}
