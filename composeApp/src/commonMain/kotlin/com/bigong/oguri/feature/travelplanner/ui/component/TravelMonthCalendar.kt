package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.feature.travelplanner.ui.travelWeekDate
import kotlinx.datetime.*
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelMonthCalendar(
    month: LocalDate,
    start: LocalDate?,
    end: LocalDate?,
    recommendedStart: LocalDate?,
    recommendedEnd: LocalDate?,
    onMonth: (LocalDate) -> Unit,
    onDate: (LocalDate) -> Unit,
) {
    val colors = OguriTheme.colors
    val nextMonth = month.plus(1, DateTimeUnit.MONTH)
    val dayCount = nextMonth.minus(1, DateTimeUnit.DAY).day
    val offset = (month.dayOfWeek.ordinal + 1) % 7
    val weekLabels = listOf(Res.string.calendar_day_sun, Res.string.calendar_day_mon, Res.string.calendar_day_tue, Res.string.calendar_day_wed, Res.string.calendar_day_thu, Res.string.calendar_day_fri, Res.string.calendar_day_sat)
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TravelIconButton(onClick = { onMonth(month.minus(1, DateTimeUnit.MONTH)) }) { Icon(painterResource(Res.drawable.ic_travel_back), stringResource(Res.string.travel_previous_month), Modifier.size(24.dp)) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.calendar_month_year, month.year, month.month.ordinal + 1), style = OguriTheme.typography.cardSubtitle, color = colors.textPrimary)
            }
            TravelIconButton(onClick = { onMonth(nextMonth) }) { Icon(painterResource(Res.drawable.ic_travel_back), stringResource(Res.string.travel_next_month), Modifier.size(24.dp).rotate(180f)) }
        }
        Row(Modifier.fillMaxWidth().height(28.dp), verticalAlignment = Alignment.CenterVertically) {
            weekLabels.forEach { label -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(stringResource(label), style = OguriTheme.typography.labelSmall, color = colors.textTertiary) } }
        }
        repeat((offset + dayCount + 6) / 7) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { weekday ->
                    val day = week * 7 + weekday - offset + 1
                    if (day !in 1..dayCount) {
                        Spacer(Modifier.weight(1f).height(48.dp))
                    } else {
                        val date = LocalDate(month.year, month.month, day)
                        val endpoint = date == start || date == end
                        val inside = start != null && end != null && date in start..end
                        val recommended = recommendedStart != null && recommendedEnd != null && date in recommendedStart..recommendedEnd
                        val label = travelWeekDate(date)
                        Box(Modifier.weight(1f).height(48.dp).noRippleClickable(onClick = { onDate(date) })
                            .semantics { contentDescription = label; selected = endpoint || inside }, contentAlignment = Alignment.Center) {
                            if (inside && start != end) {
                                val fill = if (endpoint) .5f else 1f
                                val align = if (date == start) Alignment.CenterEnd else if (date == end) Alignment.CenterStart else Alignment.Center
                                Box(Modifier.fillMaxWidth(fill).height(36.dp).align(align).background(Mint5))
                            }
                            Box(Modifier.size(36.dp).then(if (endpoint) Modifier.background(Mint70, CircleShape) else Modifier), contentAlignment = Alignment.Center) {
                                Text(day.toString(), style = OguriTheme.typography.bodyMedium, color = if (endpoint) colors.textOnMint else colors.textPrimary)
                            }
                            if (recommended && !endpoint && !inside) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp).size(4.dp).background(Mint70, CircleShape))
                        }
                    }
                }
            }
        }
    }
}
