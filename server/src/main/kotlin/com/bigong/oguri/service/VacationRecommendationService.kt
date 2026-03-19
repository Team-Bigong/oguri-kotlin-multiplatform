package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.CalendarHolidayDateResponse
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
        startDateCutoff: LocalDate? = null
    ): List<RecommendationPeriod> {
        val normalizedDayOff = userDayOff.coerceAtLeast(1)
        val candidates = mutableListOf<RecommendationPeriod>()
        val searchWindow = normalizedDayOff * 2 + 10

        repeat(monthRangeCount) { monthOffset ->
            val currentYearMonth = startYearMonth.plusMonths(monthOffset.toLong())
            val startOfMonth = currentYearMonth.atDay(1)
            val endOfMonth = currentYearMonth.atEndOfMonth()
            val daysInMonth = ChronoUnit.DAYS.between(startOfMonth, endOfMonth).toInt() + 1
            val monthlyCandidates = mutableListOf<RecommendationPeriod>()

            for (dayIndex in 0 until daysInMonth) {
                val currentStart = startOfMonth.plusDays(dayIndex.toLong())
                if (startDateCutoff != null && currentStart.isBefore(startDateCutoff)) {
                    continue
                }

                var usedDayOffCount = 0
                var currentEnd = currentStart
                var holidayCount = 0
                val holidayNames = linkedSetOf<String>()
                val holidayDateDetails = linkedMapOf<LocalDate, CalendarHolidayDateResponse>()

                val maxRange = (dayIndex + searchWindow).coerceAtMost(daysInMonth)
                for (windowIndex in dayIndex until maxRange) {
                    val date = startOfMonth.plusDays(windowIndex.toLong())
                    val matchedHoliday = holidayMap[date]
                    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                    val isPublicHoliday = matchedHoliday != null
                    val isHoliday = isWeekend || isPublicHoliday

                    if (isHoliday) {
                        holidayCount++
                        if (matchedHoliday?.isActualHoliday == true) {
                            holidayNames.add(matchedHoliday.name)
                        }
                        holidayDateDetails[date] =
                            CalendarHolidayDateResponse(
                                date = date,
                                label = matchedHoliday?.name ?: DEFAULT_HOLIDAY_NAME,
                                weekend = isWeekend,
                                publicHoliday = isPublicHoliday
                            )
                    } else {
                        if (usedDayOffCount < normalizedDayOff) {
                            usedDayOffCount++
                        } else {
                            break
                        }
                    }
                    currentEnd = date
                }

                val totalDays = ChronoUnit.DAYS.between(currentStart, currentEnd).toInt() + 1
                if (totalDays > 0) {
                    monthlyCandidates.add(
                        RecommendationPeriod(
                            start = currentStart,
                            end = currentEnd,
                            totalDays = totalDays,
                            usedDayOffCount = usedDayOffCount,
                            holidayCount = holidayCount,
                            holidayNames = holidayNames.toList(),
                            holidayDateDetails = holidayDateDetails.values.toList()
                        )
                    )
                }
            }

            val selectedMonthlyCandidates = monthlyCandidates
                .distinctBy { period -> buildPeriodKey(period.start, period.end) }
                .sortedWith(RECOMMENDATION_COMPARATOR)
                .take(periodLimitPerMonth)
            candidates.addAll(selectedMonthlyCandidates)
        }

        return candidates
            .distinctBy { period -> buildPeriodKey(period.start, period.end) }
            .sortedWith(RECOMMENDATION_COMPARATOR)
    }

    data class RecommendationPeriod(
        val start: LocalDate,
        val end: LocalDate,
        val totalDays: Int,
        val usedDayOffCount: Int,
        val holidayCount: Int,
        val holidayNames: List<String>,
        val holidayDateDetails: List<CalendarHolidayDateResponse>
    )

    private fun buildPeriodKey(startDate: LocalDate, endDate: LocalDate): String = "${startDate}_${endDate}"

    private companion object {
        private const val DEFAULT_HOLIDAY_NAME = "주말"
        private val RECOMMENDATION_COMPARATOR =
            compareByDescending<RecommendationPeriod> { it.totalDays }
                .thenBy { it.usedDayOffCount }
                .thenByDescending { it.holidayCount }
                .thenBy { it.start }
    }
}
