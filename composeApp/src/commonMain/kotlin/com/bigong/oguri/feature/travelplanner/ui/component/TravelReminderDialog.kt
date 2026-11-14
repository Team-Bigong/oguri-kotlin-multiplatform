package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelReminderStatus
import com.bigong.oguri.domain.model.TravelReminderTime
import com.bigong.oguri.feature.travelplanner.ui.model.TravelReminderUiState
import com.bigong.oguri.feature.travelplanner.ui.model.parseTravelReminderTime
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TravelReminderDialog(
    state: TravelReminderUiState,
    onSchedule: (TravelReminderTime) -> Unit,
    onCancel: () -> Unit,
    onReload: () -> Unit,
    onDismiss: () -> Unit,
) {
    val settings = state.settings
    var date by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var zone by rememberSaveable { mutableStateOf("") }
    val effectiveZone = zone.ifBlank { settings?.tripTimeZone?.id.orEmpty() }
    val selection = settings?.let { parseTravelReminderTime(date, time, effectiveZone, it, Clock.System.now()) }
    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        containerColor = Neutral0,
        onDismissRequest = { if (!state.busy) onDismiss() },
        title = { Text(stringResource(Res.string.travel_my_reminder)) },
        text = {
            Column(
                Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.busy) TravelLoading(Modifier.fillMaxWidth())
                if (settings != null) {
                    val reminder = settings.reminder
                    Text(stringResource(Res.string.travel_reminder_explanation))
                    if (reminder == null) {
                        Text(stringResource(Res.string.travel_reminder_off))
                    } else {
                        Text(
                            stringResource(
                                Res.string.travel_reminder_scheduled,
                                reminder.scheduledAt.toLocalDateTime(reminder.timeZone).toString(),
                                reminder.timeZone.id,
                            ),
                        )
                        Text(
                            stringResource(
                                when (reminder.status) {
                                    TravelReminderStatus.PENDING -> Res.string.travel_reminder_pending
                                    TravelReminderStatus.HANDOFF_STARTED -> Res.string.travel_reminder_sending
                                    TravelReminderStatus.SENT -> Res.string.travel_reminder_sent
                                    TravelReminderStatus.FAILED, TravelReminderStatus.EXPIRED -> Res.string.travel_reminder_failed
                                },
                            ),
                        )
                        TravelTextButton(enabled = !state.busy, onClick = onCancel) { Text(stringResource(Res.string.travel_reminder_turn_off)) }
                    }
                    if (settings.canSchedule) {
                        OutlinedTextField(date, {
                            date = it
                        }, enabled = !state.busy, label = { Text(stringResource(Res.string.travel_reminder_date)) }, singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            textStyle = OguriTheme.typography.bodyLarge,
                            colors = TravelFieldColors(),
                        )
                        OutlinedTextField(time, {
                            time = it
                        }, enabled = !state.busy, label = { Text(stringResource(Res.string.travel_reminder_time)) }, singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            textStyle = OguriTheme.typography.bodyLarge,
                            colors = TravelFieldColors(),
                        )
                        Text(
                            stringResource(
                                Res.string.travel_reminder_before_departure,
                                settings.dates.start.toString(),
                                "",
                            ),
                        )
                        if (date.isNotBlank() &&
                            time.isNotBlank() &&
                            selection == null
                        ) {
                            Text(stringResource(Res.string.travel_reminder_invalid_time))
                        }
                    } else {
                        Text(stringResource(Res.string.travel_reminder_not_available))
                    }
                }
                if (state.permissionDenied) Text(stringResource(Res.string.travel_reminder_permission_denied))
                state.failure?.let { TravelInlineFailure(it, onRetry = onReload) }
            }
        },
        confirmButton = {
            if (settings?.canSchedule == true) {
                TravelTextButton(enabled = !state.busy && selection != null, onClick = {
                    selection?.let(onSchedule)
                }) { Text(stringResource(Res.string.travel_reminder_save)) }
            }
        },
        dismissButton = { TravelTextButton(enabled = !state.busy, onClick = onDismiss) { Text(stringResource(Res.string.travel_search_close)) } },
    )
}
