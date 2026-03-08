package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint50
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange20
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.util.HapticType
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.util.extension.perform
import com.bigong.oguri.domain.model.CalendarHoliday
import com.bigong.oguri.domain.model.CalendarPeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_day_fri
import oguri.composeapp.generated.resources.calendar_day_mon
import oguri.composeapp.generated.resources.calendar_day_sat
import oguri.composeapp.generated.resources.calendar_day_sun
import oguri.composeapp.generated.resources.calendar_day_thu
import oguri.composeapp.generated.resources.calendar_day_tue
import oguri.composeapp.generated.resources.calendar_day_wed
import org.jetbrains.compose.resources.stringResource

private val PERIOD_HIGHLIGHT_CORNER_RADIUS_DP = 8.dp
private val SELECTED_DATE_EDGE_SHAPE = RoundedCornerShape(size = PERIOD_HIGHLIGHT_CORNER_RADIUS_DP)

@Composable
fun CalendarMonthGrid(
    year: Int,
    month: Int,
    holidays: List<CalendarHoliday>,
    periods: List<CalendarPeriod>,
    selectedPeriod: CalendarPeriod?,
    selectedDate: LocalDate?,
    todayDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val holidayNameByDate = holidays.associate { holiday: CalendarHoliday -> holiday.date to holiday.name }
    val dayCells = dayCells(year = year, month = month)
    val dayLabels =
        listOf(
            stringResource(Res.string.calendar_day_sun),
            stringResource(Res.string.calendar_day_mon),
            stringResource(Res.string.calendar_day_tue),
            stringResource(Res.string.calendar_day_wed),
            stringResource(Res.string.calendar_day_thu),
            stringResource(Res.string.calendar_day_fri),
            stringResource(Res.string.calendar_day_sat),
        )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        ) {
            dayLabels.forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    modifier = Modifier.weight(1f),
                    style = OguriTheme.typography.labelSmall,
                    color = Neutral50,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        dayCells.chunked(size = 7).forEach { weekCells: List<DayCell> ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            ) {
                weekCells.forEachIndexed { index: Int, dayCell: DayCell ->
                    val periodForDate = dayCell.date.findPeriod(periods = periods)
                    val isSelected = selectedPeriod != null && periodForDate?.id == selectedPeriod.id
                    val hasRecommendation = periodForDate != null
                    val hasSamePeriodLeft =
                        if (index == 0) {
                            false
                        } else {
                            weekCells[index - 1].date.findPeriod(periods = periods)?.id == periodForDate?.id && hasRecommendation
                        }
                    val hasSamePeriodRight =
                        if (index == weekCells.lastIndex) {
                            false
                        } else {
                            weekCells[index + 1].date.findPeriod(periods = periods)?.id == periodForDate?.id && hasRecommendation
                        }
                    CalendarDayCell(
                        modifier = Modifier.weight(1f),
                        dayCell = dayCell,
                        holidayName = holidayNameByDate[dayCell.date],
                        isToday = dayCell.date == todayDate,
                        isSelected = isSelected,
                        isInRecommendation = hasRecommendation,
                        hasSamePeriodLeft = hasSamePeriodLeft,
                        hasSamePeriodRight = hasSamePeriodRight,
                        isSelectedDate = dayCell.date == selectedDate,
                        onClick = onDateClick,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CalendarDayCell(
    modifier: Modifier = Modifier,
    dayCell: DayCell,
    holidayName: String?,
    isToday: Boolean,
    isSelected: Boolean,
    isInRecommendation: Boolean,
    hasSamePeriodLeft: Boolean,
    hasSamePeriodRight: Boolean,
    isSelectedDate: Boolean,
    onClick: (LocalDate) -> Unit,
) {
    val dayTextColor =
        when {
            isToday -> Mint70
            dayCell.isCurrentMonth.not() -> Neutral40
            holidayName != null || dayCell.date.dayOfWeek == DayOfWeek.SUNDAY || dayCell.date.dayOfWeek == DayOfWeek.SATURDAY -> Orange50
            else -> Neutral70
        }

    Box(
        modifier =
            modifier
                .height(height = 42.dp)
                .noRippleClickable(
                    onClick = {
                        HapticType.Selection.perform()
                        onClick(dayCell.date)
                    },
                ),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(
                            start = if (isInRecommendation && !hasSamePeriodLeft) 6.dp else 0.dp,
                            end = if (isInRecommendation && !hasSamePeriodRight) 6.dp else 0.dp,
                        ).background(
                            color =
                                when {
                                    isSelected -> Mint50.copy(alpha = 0.5f)
                                    else -> Color.Transparent
                                },
                            shape = selectedDateBackgroundShape(isInRecommendation, hasSamePeriodLeft, hasSamePeriodRight),
                        ).recommendationDashedBorder(
                            isSelected = isSelected,
                            isInRecommendation = isInRecommendation,
                            hasSamePeriodLeft = hasSamePeriodLeft,
                            hasSamePeriodRight = hasSamePeriodRight,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = dayCell.label,
                    style = OguriTheme.typography.labelSmall,
                    color = dayTextColor,
                    textDecoration = if (isToday) TextDecoration.Underline else TextDecoration.None,
                )
            }

            if (!holidayName.isNullOrBlank() && dayCell.isCurrentMonth) {
                Text(
                    text = holidayName,
                    style = OguriTheme.typography.labelSmall,
                    color = Orange50,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp).widthIn(max = 46.dp),
                )
            } else if (isSelectedDate) {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

private data class DayCell(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val label: String,
)

private fun dayCells(
    year: Int,
    month: Int,
): List<DayCell> {
    val firstDayOfMonth = LocalDate.parse("$year-${month.toString().padStart(length = 2, padChar = '0')}-01")
    val startOffset =
        when (firstDayOfMonth.dayOfWeek) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }
    val startDate = firstDayOfMonth.minus(startOffset, DateTimeUnit.DAY)

    val daysInMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day
    val totalVisibleDays = startOffset + daysInMonth
    val rowCount = if (totalVisibleDays <= 35) 5 else 6
    val calendarCellCount = rowCount * 7

    return List(calendarCellCount) { index: Int ->
        val date = startDate.plus(index, DateTimeUnit.DAY)
        val monthNumber = date.month.ordinal + 1
        DayCell(
            date = date,
            isCurrentMonth = monthNumber == month,
            label = if (monthNumber == month) date.day.toString() else "$monthNumber.${date.day}",
        )
    }
}

private fun selectedDateBackgroundShape(
    isInRecommendation: Boolean,
    hasSamePeriodLeft: Boolean,
    hasSamePeriodRight: Boolean,
): Shape {
    if (!isInRecommendation) {
        return CircleShape
    }
    return when {
        !hasSamePeriodLeft && !hasSamePeriodRight -> SELECTED_DATE_EDGE_SHAPE
        !hasSamePeriodLeft -> RoundedCornerShape(topStart = PERIOD_HIGHLIGHT_CORNER_RADIUS_DP, bottomStart = PERIOD_HIGHLIGHT_CORNER_RADIUS_DP)
        !hasSamePeriodRight -> RoundedCornerShape(topEnd = PERIOD_HIGHLIGHT_CORNER_RADIUS_DP, bottomEnd = PERIOD_HIGHLIGHT_CORNER_RADIUS_DP)
        else -> RoundedCornerShape(size = 0.dp)
    }
}

private fun Modifier.recommendationDashedBorder(
    isSelected: Boolean,
    isInRecommendation: Boolean,
    hasSamePeriodLeft: Boolean,
    hasSamePeriodRight: Boolean,
): Modifier {
    if (isSelected || !isInRecommendation) {
        return this
    }

    return drawBehind {
        val strokeWidthPx = 1.dp.toPx()
        val inset = strokeWidthPx / 2f
        val highlightColor = Orange20.copy(alpha = 0.5f)
        val dashEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
        val radiusPx = PERIOD_HIGHLIGHT_CORNER_RADIUS_DP.toPx()
        val lineStartX = if (hasSamePeriodLeft) inset else inset + radiusPx
        val lineEndX = if (hasSamePeriodRight) size.width - inset else size.width - inset - radiusPx

        if (lineEndX > lineStartX) {
            drawLine(
                color = highlightColor,
                start = androidx.compose.ui.geometry.Offset(x = lineStartX, y = inset),
                end = androidx.compose.ui.geometry.Offset(x = lineEndX, y = inset),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round,
                pathEffect = dashEffect,
            )

            drawLine(
                color = highlightColor,
                start = androidx.compose.ui.geometry.Offset(x = lineStartX, y = size.height - inset),
                end = androidx.compose.ui.geometry.Offset(x = lineEndX, y = size.height - inset),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round,
                pathEffect = dashEffect,
            )
        }

        val arcDiameter = radiusPx * 2f
        if (!hasSamePeriodLeft) {
            val leftArcPath =
                Path().apply {
                    addArc(
                        oval =
                            androidx.compose.ui.geometry.Rect(
                                left = 0f,
                                top = inset,
                                right = arcDiameter,
                                bottom = size.height - inset,
                            ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 180f,
                    )
                }
            drawPath(
                path = leftArcPath,
                color = highlightColor,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, pathEffect = dashEffect),
            )
        }

        if (!hasSamePeriodRight) {
            val rightArcPath =
                Path().apply {
                    addArc(
                        oval =
                            androidx.compose.ui.geometry.Rect(
                                left = size.width - arcDiameter,
                                top = inset,
                                right = size.width,
                                bottom = size.height - inset,
                            ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 180f,
                    )
                }
            drawPath(
                path = rightArcPath,
                color = highlightColor,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, pathEffect = dashEffect),
            )
        }
    }
}

private fun LocalDate.findPeriod(periods: List<CalendarPeriod>): CalendarPeriod? =
    periods.firstOrNull { period: CalendarPeriod ->
        this in period.startDate..period.endDate
    }
