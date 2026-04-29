package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.response.CalendarHolidayDateResponse
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Service
class VacationRecommendationService {
    fun findRecommendedPeriods(
        startYearMonth: YearMonth,
        userDayOff: Int,
        holidayMap: Map<LocalDate, PublicHoliday>,
        monthRangeCount: Int,
        periodLimitPerMonth: Int,
        startDateCutoff: LocalDate? = null,
    ): List<RecommendationPeriod> {
        val normalizedDayOff = userDayOff.coerceAtLeast(1)
        val candidates = mutableListOf<RecommendationPeriod>()
        val searchWindow = normalizedDayOff * 2 + 10

        repeat(monthRangeCount) { monthOffset ->
            val currentYearMonth = startYearMonth.plusMonths(monthOffset.toLong())
            val startOfMonth = currentYearMonth.atDay(1)
            val endOfMonth = currentYearMonth.atEndOfMonth()
            val daysInMonth = ChronoUnit.DAYS.between(startOfMonth, endOfMonth).toInt() + 1
            val searchStartBoundary = startDateCutoff?.let { maxOf(startOfMonth, it) } ?: startOfMonth
            val monthlyCandidates = mutableListOf<RecommendationPeriod>()

            for (dayIndex in 0 until daysInMonth) {
                val currentStart = startOfMonth.plusDays(dayIndex.toLong())
                if (startDateCutoff != null && currentStart.isBefore(startDateCutoff)) {
                    continue
                }

                val candidateStart =
                    resolveContiguousHolidayStart(
                        currentStart = currentStart,
                        searchStartBoundary = searchStartBoundary,
                        holidayMap = holidayMap,
                    )
                var usedDayOffCount = 0
                var currentEnd = candidateStart
                var holidayCount = 0
                val actualHolidayNames = linkedSetOf<String>()
                val nonActualHolidayNames = linkedSetOf<String>()
                val holidayDateDetails = linkedMapOf<LocalDate, CalendarHolidayDateResponse>()

                val maxSearchEnd = candidateStart.plusDays((searchWindow - 1).toLong())
                val searchEnd = if (maxSearchEnd.isBefore(endOfMonth)) maxSearchEnd else endOfMonth
                var date = candidateStart
                while (!date.isAfter(searchEnd)) {
                    val matchedHoliday = holidayMap[date]
                    val isWeekend = isWeekend(date)
                    val isPublicHoliday = matchedHoliday != null
                    val isHoliday = isWeekend || isPublicHoliday

                    if (isHoliday) {
                        holidayCount++
                        matchedHoliday
                            ?.name
                            ?.takeIf { holidayName: String -> holidayName.isNotBlank() }
                            ?.let { holidayName: String ->
                                if (matchedHoliday.isActualHoliday) {
                                    actualHolidayNames.add(holidayName)
                                } else {
                                    nonActualHolidayNames.add(holidayName)
                                }
                            }
                        holidayDateDetails[date] =
                            CalendarHolidayDateResponse(
                                date = date,
                                label = matchedHoliday?.name ?: DEFAULT_HOLIDAY_NAME,
                                weekend = isWeekend,
                                publicHoliday = isPublicHoliday,
                            )
                    } else {
                        if (usedDayOffCount < normalizedDayOff) {
                            usedDayOffCount++
                        } else {
                            break
                        }
                    }
                    currentEnd = date
                    date = date.plusDays(1)
                }

                val totalDays = ChronoUnit.DAYS.between(candidateStart, currentEnd).toInt() + 1
                if (totalDays >= MINIMUM_RECOMMENDATION_TOTAL_DAYS) {
                    val summaryHolidayNames =
                        when {
                            actualHolidayNames.isNotEmpty() -> actualHolidayNames.toList()
                            nonActualHolidayNames.isNotEmpty() -> nonActualHolidayNames.toList()
                            else -> emptyList()
                        }
                    monthlyCandidates.add(
                        RecommendationPeriod(
                            start = candidateStart,
                            end = currentEnd,
                            totalDays = totalDays,
                            usedDayOffCount = usedDayOffCount,
                            holidayCount = holidayCount,
                            holidayNames = summaryHolidayNames,
                            holidayDateDetails = holidayDateDetails.values.toList(),
                        ),
                    )
                }
            }

            val selectedMonthlyCandidates =
                monthlyCandidates
                    .distinctBy { period -> buildPeriodKey(period.start, period.end) }
                    .sortedWith(buildRecommendationComparator())
                    .take(periodLimitPerMonth)
            candidates.addAll(selectedMonthlyCandidates)
        }

        return candidates
            .distinctBy { period -> buildPeriodKey(period.start, period.end) }
            .sortedWith(buildRecommendationComparator())
    }

    data class RecommendationPeriod(
        val start: LocalDate,
        val end: LocalDate,
        val totalDays: Int,
        val usedDayOffCount: Int,
        val holidayCount: Int,
        val holidayNames: List<String>,
        val holidayDateDetails: List<CalendarHolidayDateResponse>,
    )

    private fun buildPeriodKey(
        startDate: LocalDate,
        endDate: LocalDate,
    ): String = "${startDate}_$endDate"

    private fun resolveContiguousHolidayStart(
        currentStart: LocalDate,
        searchStartBoundary: LocalDate,
        holidayMap: Map<LocalDate, PublicHoliday>,
    ): LocalDate {
        var candidateStart = currentStart
        var previousDate = candidateStart.minusDays(1)
        while (!previousDate.isBefore(searchStartBoundary) && isHoliday(previousDate, holidayMap)) {
            candidateStart = previousDate
            previousDate = candidateStart.minusDays(1)
        }
        return candidateStart
    }

    private fun isHoliday(
        date: LocalDate,
        holidayMap: Map<LocalDate, PublicHoliday>,
    ): Boolean = isWeekend(date) || holidayMap.containsKey(date)

    private fun isWeekend(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    private fun buildRecommendationComparator(): Comparator<RecommendationPeriod> {
        val currentYear = LocalDate.now().year
        return compareByDescending<RecommendationPeriod> { period ->
            weightedTotalTripCount(period, currentYear)
        }.thenBy { it.usedDayOffCount }
            .thenByDescending { it.holidayCount }
            .thenBy { it.start }
    }

    private fun weightedTotalTripCount(
        period: RecommendationPeriod,
        currentYear: Int,
    ): Double {
        val yearWeight =
            if (period.start.year == currentYear) {
                CURRENT_YEAR_WEIGHT
            } else {
                NON_CURRENT_YEAR_WEIGHT
            }
        return period.totalDays * yearWeight
    }

    private companion object {
        private const val DEFAULT_HOLIDAY_NAME = "주말"
        private const val MINIMUM_RECOMMENDATION_TOTAL_DAYS = 3
        private const val CURRENT_YEAR_WEIGHT = 1.0
        private const val NON_CURRENT_YEAR_WEIGHT = 0.85
    }
}
