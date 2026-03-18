package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint50
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_day_fri
import oguri.composeapp.generated.resources.calendar_day_mon
import oguri.composeapp.generated.resources.calendar_day_sat
import oguri.composeapp.generated.resources.calendar_day_sun
import oguri.composeapp.generated.resources.calendar_day_thu
import oguri.composeapp.generated.resources.calendar_day_tue
import oguri.composeapp.generated.resources.calendar_day_wed
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun CalendarMonthGrid(
    periodCard: CalendarPeriodCardUiModel,
    modifier: Modifier = Modifier,
) {
    val todayDate =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    val holidayByDate = periodCard.holidays.associateBy { holiday -> holiday.date }
    val dominantYearMonth = dominantYearMonth(periodCard.startDate, periodCard.endDate)
    val visibleWeeks = displayWeeks(periodCard = periodCard, dominantYearMonth = dominantYearMonth)

    val weekDayLabels =
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
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDayLabels.forEach { weekDayLabel ->
                Text(
                    text = weekDayLabel,
                    style = OguriTheme.typography.labelMedium,
                    color = Neutral50,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        visibleWeeks.forEachIndexed { weekIndex, weekDays ->
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEachIndexed { dayIndex, date ->
                    val isRecommendedDate = date in periodCard.startDate..periodCard.endDate
                    val isHoliday = holidayByDate[date] != null
                    val isTodayDate = date == todayDate
                    val isWeekendDate =
                        date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                    val hasSamePeriodLeft = dayIndex > 0 && weekDays[dayIndex - 1] in periodCard.startDate..periodCard.endDate
                    val hasSamePeriodRight =
                        dayIndex < weekDays.lastIndex && weekDays[dayIndex + 1] in periodCard.startDate..periodCard.endDate

                    Column(
                        modifier =
                            Modifier
                                .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .padding(
                                        start = if (isRecommendedDate && !hasSamePeriodLeft) 4.dp else 0.dp,
                                        end = if (isRecommendedDate && !hasSamePeriodRight) 4.dp else 0.dp,
                                    ).background(
                                        color = if (isRecommendedDate) Mint50.copy(alpha = 0.5f) else Neutral0,
                                        shape =
                                            when {
                                                !isRecommendedDate -> RoundedCornerShape(0.dp)
                                                !hasSamePeriodLeft && !hasSamePeriodRight -> RoundedCornerShape(999.dp)
                                                !hasSamePeriodLeft -> RoundedCornerShape(topStart = 999.dp, bottomStart = 999.dp)
                                                !hasSamePeriodRight -> RoundedCornerShape(topEnd = 999.dp, bottomEnd = 999.dp)
                                                else -> RoundedCornerShape(0.dp)
                                            },
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = date.toDateLabel(dominantYearMonth = dominantYearMonth),
                                style = OguriTheme.typography.labelSmall,
                                color =
                                    when {
                                        isTodayDate -> Mint70
                                        isWeekendDate || isHoliday -> Orange50
                                        isRecommendedDate -> Neutral70
                                        else -> Neutral50
                                    },
                                textDecoration = if (isTodayDate) androidx.compose.ui.text.style.TextDecoration.Underline else null,
                            )
                        }

                        val holidayName = holidayByDate[date]?.name
                        if (isRecommendedDate && !holidayName.isNullOrBlank()) {
                            Text(
                                text = holidayName,
                                style = OguriTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Orange50,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        } else {
                            Spacer(modifier = Modifier.height(18.dp))
                        }
                    }
                }
            }
            if (weekIndex < visibleWeeks.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun LocalDate.toDateLabel(dominantYearMonth: YearMonth): String =
    if (year == dominantYearMonth.year && month.ordinal + 1 == dominantYearMonth.month) {
        day.toString()
    } else {
        "${month.ordinal + 1}.$day"
    }

private data class YearMonth(
    val year: Int,
    val month: Int,
)

private fun dominantYearMonth(
    startDate: LocalDate,
    endDate: LocalDate,
): YearMonth {
    val countsByMonth = mutableMapOf<YearMonth, Int>()
    var cursor = startDate
    while (cursor <= endDate) {
        val key = YearMonth(cursor.year, cursor.month.ordinal + 1)
        countsByMonth[key] = (countsByMonth[key] ?: 0) + 1
        cursor = cursor.plus(1, DateTimeUnit.DAY)
    }
    return countsByMonth.maxByOrNull { entry -> entry.value }?.key ?: YearMonth(startDate.year, startDate.month.ordinal + 1)
}

private fun displayWeeks(
    periodCard: CalendarPeriodCardUiModel,
    dominantYearMonth: YearMonth,
): List<List<LocalDate>> {
    val firstDateOfMonth = LocalDate.parse("${dominantYearMonth.year}-${dominantYearMonth.month.toString().padStart(2, '0')}-01")
    val lastDateOfMonth = firstDateOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

    val monthGridStartDate =
        firstDateOfMonth.minus(dayOfWeekOffset(firstDateOfMonth.dayOfWeek), DateTimeUnit.DAY)
    val monthGridEndDate =
        lastDateOfMonth.plus(6 - dayOfWeekOffset(lastDateOfMonth.dayOfWeek), DateTimeUnit.DAY)
    val expandedGridStartDate = monthGridStartDate.minus(2, DateTimeUnit.WEEK)
    val expandedGridEndDate = monthGridEndDate.plus(2, DateTimeUnit.WEEK)

    val allDays = mutableListOf<LocalDate>()
    var cursor = expandedGridStartDate
    while (cursor <= expandedGridEndDate) {
        allDays += cursor
        cursor = cursor.plus(1, DateTimeUnit.DAY)
    }

    val allWeeks = allDays.chunked(7)
    val recommendationCountByWeek =
        allWeeks.map { week ->
            week.count { date -> date in periodCard.startDate..periodCard.endDate }
        }
    val recommendationStartWeekIndex = recommendationCountByWeek.indexOfFirst { count -> count > 0 }.coerceAtLeast(0)
    val recommendationEndWeekIndex =
        recommendationCountByWeek
            .indexOfLast { count ->
                count > 0
            }.coerceAtLeast(recommendationStartWeekIndex)
    val highestRecommendationWeekIndex = recommendationCountByWeek.indexOfFirstMax()

    val recommendationSpanWeekCount = recommendationEndWeekIndex - recommendationStartWeekIndex + 1
    val visibleWeekCount = recommendationSpanWeekCount.coerceAtLeast(3).coerceAtMost(allWeeks.size)
    val targetRowIndex =
        if (visibleWeekCount % 2 == 0) {
            (visibleWeekCount / 2) - 1
        } else {
            visibleWeekCount / 2
        }
    val desiredCenteredStartWeekIndex = highestRecommendationWeekIndex - targetRowIndex

    val minimumStartWeekIndex = (recommendationEndWeekIndex - visibleWeekCount + 1).coerceAtLeast(0)
    val maximumStartWeekIndex = minOf(recommendationStartWeekIndex, allWeeks.size - visibleWeekCount)
    val resolvedStartWeekIndex =
        if (minimumStartWeekIndex <= maximumStartWeekIndex) {
            desiredCenteredStartWeekIndex.coerceIn(minimumStartWeekIndex, maximumStartWeekIndex)
        } else {
            desiredCenteredStartWeekIndex.coerceIn(0, (allWeeks.size - visibleWeekCount).coerceAtLeast(0))
        }
    val resolvedEndWeekIndexExclusive = (resolvedStartWeekIndex + visibleWeekCount).coerceAtMost(allWeeks.size)

    return allWeeks.subList(resolvedStartWeekIndex, resolvedEndWeekIndexExclusive)
}

private fun dayOfWeekOffset(dayOfWeek: DayOfWeek): Int =
    when (dayOfWeek) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }

private fun List<Int>.indexOfFirstMax(): Int {
    if (isEmpty()) {
        return 0
    }

    var maxIndex = 0
    var maxValue = first()
    forEachIndexed { index, value ->
        if (value > maxValue) {
            maxValue = value
            maxIndex = index
        }
    }
    return maxIndex
}
