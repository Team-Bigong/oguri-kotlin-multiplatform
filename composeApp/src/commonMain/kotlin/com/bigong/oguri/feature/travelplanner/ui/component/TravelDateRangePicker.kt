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
import com.bigong.oguri.core.designsystem.Mint50
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange20
import com.bigong.oguri.core.designsystem.Orange5
import com.bigong.oguri.feature.travelplanner.ui.model.TravelDateSelection
import com.bigong.oguri.feature.travelplanner.ui.travelDateLabel
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dismissSheet = rememberTravelSheetDismiss(sheetState, onDismiss)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        containerColor = colors.backgroundSecondary,
        contentColor = Neutral100,
        scrimColor = TravelSheetScrim,
        dragHandle = { TravelSheetHandle() },
    ) {
        Column(Modifier.fillMaxWidth().height(584.dp)) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.travel_choose_range), Modifier.weight(1f), style = OguriTheme.typography.cardTitle, color = Neutral100)
                TravelIconButton(onClick = dismissSheet) { Icon(painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close), Modifier.size(24.dp)) }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(4.dp))
                val start = selection.start
                val end = selection.end
                val rangeShape = RoundedCornerShape(8.dp)
                if (start != null && end != null && selected == null) {
                    Column(
                        Modifier.fillMaxWidth().background(Orange5, rangeShape)
                            .border(1.dp, Orange20, rangeShape).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            stringResource(Res.string.travel_card_date_range, travelDateLabel(start), travelDateLabel(end)),
                            style = OguriTheme.typography.cardSubtitle,
                            color = Neutral100,
                        )
                        Text(
                            stringResource(Res.string.travel_invalid_dates, TravelDates.MAX_DAYS),
                            color = colors.orange,
                            style = OguriTheme.typography.caption,
                        )
                    }
                } else {
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 44.dp).background(Mint5, rangeShape)
                            .border(1.dp, Mint50, rangeShape).padding(12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        val label = if (selected == null) {
                            start?.let { travelWeekDate(it) } ?: stringResource(Res.string.travel_select_dates)
                        } else {
                            stringResource(
                                Res.string.travel_selected_range,
                                travelWeekDate(selected.start),
                                travelWeekDate(selected.endInclusive),
                                selected.dayCount,
                            )
                        }
                        Text(label, style = OguriTheme.typography.cardSubtitle, color = Neutral100)
                    }
                }
                Spacer(Modifier.height(12.dp))
                TravelMonthCalendar(LocalDate.parse(monthText), selection.start, selection.end, recommendedStart, recommendedEnd, { monthText = it.toString() }, { day ->
                    val next = selection.select(day)
                    startText = next.start?.toString(); endText = next.end?.toString()
                })
                if (recommendedStart != null && recommendedEnd != null) Row(Modifier.padding(top = 12.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(4.dp).background(OguriTheme.colors.mint, CircleShape))
                    Text(stringResource(Res.string.travel_calendar_recommendation, recommendedEnd.day), style = OguriTheme.typography.caption, color = colors.textTertiary)
                }
            }
            TravelBottomAction(enabled = selected != null, onClick = { selected?.let(onSelected) }) { Text(stringResource(Res.string.travel_confirm_dates)) }
        }
    }
}
