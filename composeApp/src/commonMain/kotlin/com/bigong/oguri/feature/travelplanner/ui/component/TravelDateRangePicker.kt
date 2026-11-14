package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint20
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.travelplanner.ui.model.TravelDateSelection
import com.bigong.oguri.feature.travelplanner.ui.travelWeekDate
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
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
internal fun TravelDateRangePicker(
    initial: TravelDates?, onDismiss: () -> Unit, onSelected: (TravelDates) -> Unit,
    recommendedStart: LocalDate? = null, recommendedEnd: LocalDate? = null,
) {
    var startText by rememberSaveable { mutableStateOf(initial?.start?.toString()) }
    var endText by rememberSaveable { mutableStateOf(initial?.endInclusive?.toString()) }
    val selection = TravelDateSelection(startText?.let(LocalDate::parse), endText?.let(LocalDate::parse))
    val selected = selection.dates
    val first = initial?.start ?: recommendedStart ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var monthText by rememberSaveable { mutableStateOf(LocalDate(first.year, first.month, 1).toString()) }
    val colors = OguriTheme.colors
    ModalBottomSheet(scrimColor = com.bigong.oguri.core.designsystem.Neutral90.copy(alpha = .45f), dragHandle = { TravelSheetHandle() }, onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp), containerColor = colors.backgroundSecondary) {
        Column(Modifier.fillMaxWidth().height(584.dp)) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.travel_choose_range), Modifier.weight(1f), style = OguriTheme.typography.cardTitle, color = colors.textPrimary)
                TravelIconButton(onClick = onDismiss) { Icon(painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close), Modifier.size(24.dp)) }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().heightIn(min = 44.dp).background(Mint5, RoundedCornerShape(8.dp)).border(1.dp, Mint20, RoundedCornerShape(8.dp)).padding(12.dp), contentAlignment = Alignment.CenterStart) {
                    Text(if (selected == null) selection.start?.let { travelWeekDate(it) } ?: stringResource(Res.string.travel_select_dates)
                        else stringResource(Res.string.travel_selected_range, travelWeekDate(selected.start), travelWeekDate(selected.endInclusive), selected.dayCount), style = OguriTheme.typography.cardSubtitle, color = colors.textPrimary)
                }
                Spacer(Modifier.height(12.dp))
                TravelMonthCalendar(LocalDate.parse(monthText), selection.start, selection.end, recommendedStart, recommendedEnd, { monthText = it.toString() }, { day ->
                    val next = selection.select(day)
                    startText = next.start?.toString(); endText = next.end?.toString()
                })
                if (selection.end != null && selected == null) Text(stringResource(Res.string.travel_invalid_dates, TravelDates.MAX_DAYS), color = Orange50, style = OguriTheme.typography.caption)
                if (recommendedStart != null && recommendedEnd != null) Row(Modifier.padding(top = 12.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(4.dp).background(Mint70, CircleShape))
                    Text(stringResource(Res.string.travel_calendar_recommendation, recommendedEnd.day), style = OguriTheme.typography.caption, color = colors.textTertiary)
                }
            }
            TravelBottomAction(enabled = selected != null, onClick = { selected?.let(onSelected) }) { Text(stringResource(Res.string.travel_confirm_dates)) }
        }
    }
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
