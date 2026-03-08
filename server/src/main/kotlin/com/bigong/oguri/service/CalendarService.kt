package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.BestPeriodResponse
import com.bigong.oguri.dto.CalendarResponse
import com.bigong.oguri.dto.HolidayResponse
import com.bigong.oguri.repository.PublicHolidayRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 캘린더 화면 데이터 제공을 위한 서비스
 */
@Service
@Transactional(readOnly = true)
class CalendarService(
    private val memberService: MemberService,
    private val publicHolidayRepository: PublicHolidayRepository
) {
    /**
     * 특정 년월의 캘린더 데이터 조회 (Top 3 추천 연휴 포함)
     */
    fun getCalendarData(yearMonth: YearMonth, memberId: String): CalendarResponse {
        val dayOffCount = memberService.getDayOffCount(memberId)
        val allHolidays = publicHolidayRepository.findAll()
        val holidayMap = allHolidays.associateBy { it.holidayDate }
        
        val monthHolidays = allHolidays
            .filter { YearMonth.from(it.holidayDate) == yearMonth }
            .map { HolidayResponse(date = it.holidayDate, label = it.name) }

        // 홈 API와 동일한 로직으로 해당 월의 Top 3 구간 탐색
        val bestPeriods = findTopPeriodsInMonth(yearMonth, dayOffCount, holidayMap, limit = 3)

        return CalendarResponse(
            dayOffCount = dayOffCount,
            bestPeriods = bestPeriods,
            holidays = monthHolidays
        )
    }

    /**
     * 해당 월 내에서 가장 길게 쉴 수 있는 겹치지 않는 구간 Top N 탐색
     */
    private fun findTopPeriodsInMonth(
        yearMonth: YearMonth,
        userDayOff: Int,
        holidayMap: Map<LocalDate, PublicHoliday>,
        limit: Int
    ): List<BestPeriodResponse> {
        val startOfMonth = yearMonth.atDay(1)
        val endOfMonth = yearMonth.atEndOfMonth()
        val daysInMonth = ChronoUnit.DAYS.between(startOfMonth, endOfMonth).toInt() + 1

        val candidates = mutableListOf<VacationCandidate>()

        // 1. 해당 월 내의 모든 시작점에 대해 가능한 휴가 구간 수집
        for (i in 0 until daysInMonth) {
            val currentStart = startOfMonth.plusDays(i.toLong())
            var usedDayOff = 0
            var currentEnd = currentStart

            // 시작일부터 월말까지만 탐색
            for (j in i until daysInMonth) {
                val date = startOfMonth.plusDays(j.toLong())
                if (!isOffDay(date, holidayMap)) {
                    if (usedDayOff < userDayOff) usedDayOff++ else break
                }
                currentEnd = date
            }
            
            val totalDays = ChronoUnit.DAYS.between(currentStart, currentEnd).toInt() + 1
            if (totalDays > 0) {
                candidates.add(VacationCandidate(currentStart, currentEnd, totalDays))
            }
        }

        // 2. 정렬 (일수 큰 순 -> 날짜 빠른 순)
        val sortedCandidates = candidates
            .distinctBy { it.start.toString() + it.end.toString() }
            .sortedWith(compareByDescending<VacationCandidate> { it.totalDays }.thenBy { it.start })

        // 3. 중복 구간(Overlap) 필터링 - 홈 API 로직과 동일
        val selected = mutableListOf<VacationCandidate>()
        for (candidate in sortedCandidates) {
            if (selected.size >= limit) break
            if (selected.none { it.overlapsWith(candidate) }) {
                selected.add(candidate)
            }
        }

        return selected.map { BestPeriodResponse(it.start, it.end) }
    }

    private fun isOffDay(date: LocalDate, holidayMap: Map<LocalDate, PublicHoliday>): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || 
               date.dayOfWeek == DayOfWeek.SUNDAY || 
               holidayMap.containsKey(date)
    }

    /**
     * 내부 계산용 데이터 클래스
     */
    private data class VacationCandidate(
        val start: LocalDate,
        val end: LocalDate,
        val totalDays: Int
    ) {
        fun overlapsWith(other: VacationCandidate): Boolean {
            return !this.end.isBefore(other.start) && !other.end.isBefore(this.start)
        }
    }
}
