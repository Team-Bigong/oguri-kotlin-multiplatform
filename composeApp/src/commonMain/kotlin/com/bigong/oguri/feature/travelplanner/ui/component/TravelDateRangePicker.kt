package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.domain.model.TravelDates
import kotlinx.datetime.*
import kotlin.time.Instant
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelDateRangePicker(initial: TravelDates?, onDismiss: () -> Unit, onSelected: (TravelDates) -> Unit) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initial?.start?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
        initialSelectedEndDateMillis = initial?.endInclusive?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    )
    val selected = state.selectedStartDateMillis?.let { start -> state.selectedEndDateMillis?.let { end ->
        runCatching { TravelDates(Instant.fromEpochMilliseconds(start).toLocalDateTime(TimeZone.UTC).date, Instant.fromEpochMilliseconds(end).toLocalDateTime(TimeZone.UTC).date) }.getOrNull()
    } }
    TravelFullScreenForm(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.travel_choose_range)) },
        text = { DateRangePicker(state, showModeToggle = false, title = null, headline = { selected?.let { Text(com.bigong.oguri.feature.travelplanner.ui.travelDateRange(it), Modifier.padding(16.dp)) } }) },
        confirmButton = { TravelPrimaryButton(enabled = selected != null, onClick = { selected?.let(onSelected) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.travel_apply)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelTimePicker(onDismiss: () -> Unit, onSelected: (String) -> Unit) {
    val state = rememberTimePickerState(initialHour = 20, is24Hour = false)
    TravelFormSheet(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.travel_reminder_time)) },
        text = { TimePicker(state = state) },
        confirmButton = { TravelPrimaryButton(onClick = { onSelected(state.hour.toString().padStart(2, '0') + ":" + state.minute.toString().padStart(2, '0')) }) { Text(stringResource(Res.string.travel_apply)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelSingleDatePicker(onDismiss: () -> Unit, onSelected: (LocalDate) -> Unit) {
    val state = rememberDatePickerState()
    TravelFullScreenForm(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.travel_reminder_date)) },
        text = { DatePicker(state, title = null, headline = null, showModeToggle = false) },
        confirmButton = {
            TravelPrimaryButton(enabled = state.selectedDateMillis != null, onClick = { state.selectedDateMillis?.let { onSelected(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date) } }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.travel_apply))
            }
        },
    )
}
