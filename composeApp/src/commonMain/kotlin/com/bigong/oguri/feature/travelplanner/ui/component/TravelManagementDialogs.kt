package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelRecentChanges
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
import oguri.composeapp.generated.resources.travel_choose_time_zone
import oguri.composeapp.generated.resources.travel_city
import oguri.composeapp.generated.resources.travel_details_keep_local_times
import oguri.composeapp.generated.resources.travel_details_move_unassigned
import oguri.composeapp.generated.resources.travel_edit_trip
import oguri.composeapp.generated.resources.travel_end_date
import oguri.composeapp.generated.resources.travel_no_recent_changes
import oguri.composeapp.generated.resources.travel_recent_changes
import oguri.composeapp.generated.resources.travel_search_close
import oguri.composeapp.generated.resources.travel_start_date
import oguri.composeapp.generated.resources.travel_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelDetailsDialog(
    plan: TravelPlan,
    busy: Boolean,
    failure: TravelPlanFailure?,
    onDismiss: () -> Unit,
    onReload: () -> Unit,
    onApply: (TravelPlanDetails, Int) -> Unit,
) {
    val current = plan.summary.details
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
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.travel_edit_trip)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(Res.string.travel_title)) }, singleLine = true)
                OutlinedTextField(city, { city = it }, label = { Text(stringResource(Res.string.travel_city)) }, singleLine = true)
                OutlinedTextField(
                    zone,
                    { zone = it },
                    label = { Text(stringResource(Res.string.travel_choose_time_zone)) },
                    singleLine = true,
                )
                OutlinedTextField(start, { start = it }, label = { Text(stringResource(Res.string.travel_start_date)) }, singleLine = true)
                OutlinedTextField(end, { end = it }, label = { Text(stringResource(Res.string.travel_end_date)) }, singleLine = true)
                if (affected > 0) Text(stringResource(Res.string.travel_details_move_unassigned, affected))
                if (zone.trim() != current.timeZone.id) Text(stringResource(Res.string.travel_details_keep_local_times))
                if (failure != null) TravelInlineFailure(failure, onRetry = onReload.takeIf { failure == TravelPlanFailure.CONFLICT })
            }
        },
        confirmButton = {
            TextButton(enabled = !busy && valid && failure != TravelPlanFailure.CONFLICT, onClick = {
                if (dates != null && timeZone != null) {
                    onApply(
                        TravelPlanDetails(city.trim(), timeZone, title.trim(), dates, current.source),
                        affected,
                    )
                }
            }) { Text(stringResource(Res.string.travel_apply)) }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text(stringResource(Res.string.travel_cancel)) } },
    )
}

@Composable
internal fun TravelRecentChangesDialog(
    changes: TravelRecentChanges,
    dates: TravelDates,
    onDismiss: () -> Unit,
    onSelectDay: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.travel_recent_changes)) },
        text = {
            if (changes.changes.isEmpty()) {
                Text(stringResource(Res.string.travel_no_recent_changes))
            } else {
                val currentDates = changes.dates ?: dates
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(changes.changes, key = { it.revision }) { entry ->
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
                            Text(stringResource(label), style = MaterialTheme.typography.titleSmall)
                            entry.occurredAt?.let { instant ->
                                Text(changes.timeZone?.let { instant.toLocalDateTime(it).toString() + " · " + it.id } ?: instant.toString())
                            }
                            if (entry.visitId != null) Text(entry.visitName ?: stringResource(Res.string.travel_changed_place))
                            entry.dayNumber?.let { day -> Text(travelDayLabel(currentDates, day)) }
                            if (entry.isVisitRemoved) {
                                Text(stringResource(Res.string.travel_change_removed_now))
                            } else if (destination != null) {
                                if (entry.visitId != null && entry.dayNumber != destination) {
                                    Text(stringResource(Res.string.travel_change_moved_now, travelDayLabel(currentDates, destination)))
                                }
                                TextButton(onClick = { onSelectDay(destination) }) {
                                    Text(stringResource(Res.string.travel_change_open_day, travelDayLabel(currentDates, destination)))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.travel_search_close)) } },
    )
}
