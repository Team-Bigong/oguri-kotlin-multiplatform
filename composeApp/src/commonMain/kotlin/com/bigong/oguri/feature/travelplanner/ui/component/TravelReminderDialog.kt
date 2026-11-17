package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.feature.travelplanner.ui.travelWeekDate
import com.bigong.oguri.feature.travelplanner.ui.travelClockTime
import org.jetbrains.compose.resources.painterResource
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.foundation.border
import androidx.compose.runtime.remember
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
    onRefreshPermission: suspend () -> Unit = {},
) {
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    androidx.compose.runtime.LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) { onRefreshPermission() }
    }
    val settings = state.settings
    val reminder = settings?.reminder
    val scheduled = reminder?.scheduledAt?.toLocalDateTime(settings.tripTimeZone)
    var date by rememberSaveable(settings?.dates, reminder?.id) { mutableStateOf(scheduled?.date?.toString() ?: settings?.dates?.start?.minus(DatePeriod(days = 1))?.toString().orEmpty()) }
    var time by rememberSaveable(settings?.dates, reminder?.id) { mutableStateOf(scheduled?.time?.toString() ?: "20:00") }
    var enabled by rememberSaveable(settings?.dates, reminder?.id) { mutableStateOf(reminder != null || settings?.canSchedule == true) }
    var chooseDate by remember { mutableStateOf(false) }
    var chooseTime by remember { mutableStateOf(false) }
    val selection = settings?.let { parseTravelReminderTime(date, time, it.tripTimeZone.id, it, Clock.System.now()) }
    val selectedDate = runCatching { kotlinx.datetime.LocalDate.parse(date) }.getOrNull()
    val selectedTime = runCatching { kotlinx.datetime.LocalTime.parse(time) }.getOrNull()
    val canChoose = enabled && !state.permissionDenied && !state.busy && settings?.canSchedule == true
    TravelFullScreenForm(
        onDismissRequest = { if (!state.busy) onDismiss() },
        contentPadding = PaddingValues(0.dp),
        showBottomActions = !state.permissionDenied,
        title = { Text(stringResource(Res.string.travel_my_reminder)) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.travel_reminder_heading), style = OguriTheme.typography.sectionTitle, color = OguriTheme.colors.textPrimary)
                    Text(stringResource(Res.string.travel_reminder_explanation), style = OguriTheme.typography.cardSubtitle, color = OguriTheme.colors.textTertiary)
                }
                if (state.busy) TravelLoading(Modifier.fillMaxWidth())
                if (settings != null) {
                    HorizontalDivider(color = OguriTheme.colors.borderSubtle)
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(Res.string.travel_reminder_toggle), Modifier.weight(1f), style = OguriTheme.typography.cardSubtitle)
                        val toggleLabel = stringResource(Res.string.travel_reminder_toggle)
                        Box(Modifier.size(48.dp).semantics { contentDescription = toggleLabel }
                            .toggleable(value = enabled && !state.permissionDenied, role = Role.Switch, enabled = !state.busy && !state.permissionDenied && (settings.canSchedule || reminder != null), onValueChange = { enabled = it }), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(40.dp, 24.dp).background(if (enabled && !state.permissionDenied) OguriTheme.colors.mint else OguriTheme.colors.borderStrong, CircleShape)) {
                                Box(Modifier.align(if (enabled && !state.permissionDenied) Alignment.CenterEnd else Alignment.CenterStart).padding(2.dp).size(20.dp).background(Neutral0, CircleShape))
                            }
                        }
                    }
                    if (!state.permissionDenied) {
                        TravelReminderSettingRow(stringResource(Res.string.travel_reminder_date), selectedDate?.let { travelWeekDate(it) }.orEmpty(), canChoose) { chooseDate = true }
                        TravelReminderSettingRow(stringResource(Res.string.travel_reminder_time), selectedTime?.let { travelClockTime(it) }.orEmpty(), canChoose) { chooseTime = true }
                        HorizontalDivider(color = OguriTheme.colors.borderSubtle)
                        Text(stringResource(Res.string.travel_reminder_date_hint, travelWeekDate(settings.dates.start)), Modifier.padding(horizontal = 24.dp, vertical = 16.dp), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                        if (!settings.canSchedule) Text(stringResource(Res.string.travel_reminder_not_available), Modifier.padding(horizontal = 24.dp), style = OguriTheme.typography.bodySmall)
                        when (reminder?.status) {
                            TravelReminderStatus.FAILED, TravelReminderStatus.EXPIRED -> TravelIssueCard(
                                title = stringResource(Res.string.travel_reminder_delivery_failed_title),
                                description = stringResource(Res.string.travel_reminder_failed),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                            TravelReminderStatus.HANDOFF_STARTED, TravelReminderStatus.SENT -> Text(
                                stringResource(
                                    if (reminder.status == TravelReminderStatus.SENT) Res.string.travel_reminder_sent
                                    else Res.string.travel_reminder_sending,
                                ),
                                Modifier.padding(horizontal = 24.dp),
                                style = OguriTheme.typography.bodySmall,
                            )
                            null, TravelReminderStatus.PENDING -> Unit
                        }
                    }
                }
                if (state.permissionDenied) {
                    HorizontalDivider(color = OguriTheme.colors.borderSubtle)
                    val shape = RoundedCornerShape(8.dp)
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth()
                        .background(com.bigong.oguri.core.designsystem.Orange5, shape)
                        .border(1.dp, com.bigong.oguri.core.designsystem.Orange20, shape).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(Res.string.travel_reminder_permission_title), style = OguriTheme.typography.cardSubtitle)
                        Text(stringResource(Res.string.travel_reminder_permission_denied), style = OguriTheme.typography.caption, color = OguriTheme.colors.textSecondary)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TravelTextButton(onClick = { com.bigong.oguri.core.notification.openTravelReminderSettings() }) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(stringResource(Res.string.travel_open_settings))
                                    Icon(painterResource(Res.drawable.ic_travel_row_arrow), null, Modifier.size(20.dp), tint = OguriTheme.colors.textTertiary)
                                }
                            }
                        }
                    }
                }
                state.failure?.let {
                    TravelInlineFailure(it, onRetry = onReload, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
            }
        },
        confirmButton = {
            TravelPrimaryButton(
                enabled = !state.busy && settings != null && if (enabled) settings.canSchedule && selection != null else true,
                modifier = Modifier.fillMaxWidth(),
                onClick = { if (enabled) selection?.let(onSchedule) else if (reminder != null) onCancel() else onDismiss() },
            ) { Text(stringResource(if (enabled) Res.string.travel_reminder_save else Res.string.travel_apply)) }
        },
    )
    if (chooseDate && settings != null) TravelSingleDatePicker({ chooseDate = false }, initialDate = selectedDate) {
        date = it.toString(); chooseDate = false
    }
    if (chooseTime) TravelTimePicker({ chooseTime = false }, initialTime = selectedTime) { time = it; chooseTime = false }

}

@Composable
private fun TravelReminderSettingRow(label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    HorizontalDivider(color = OguriTheme.colors.borderSubtle)
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).noRippleClickable(enabled = enabled, onClick = onClick).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), style = OguriTheme.typography.cardSubtitle)
        Text(value, style = travelTextStyle(15, 18), color = OguriTheme.colors.textSecondary)
        Icon(painterResource(Res.drawable.ic_travel_row_arrow), null, Modifier.size(20.dp), tint = OguriTheme.colors.textPrimary)
    }
}
