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
     * 특정 년월의 캘린더 데이터 조회
     * @param dayOffCount 클라이언트에서 명시적으로 보낸 연차 개수 (null일 경우 서버의 preferred 사용)
     */
    fun getCalendarData(yearMonth: YearMonth, memberId: String, dayOffCount: Int?): CalendarResponse {
        // 1. 계산에 사용할 연차 개수 결정 (파라미터 우선 -> 없으면 서버 저장된 선호값)
        val targetDayOff = dayOffCount ?: memberService.getPreferredDayOff(memberId)

        // 2. 공휴일 정보 로드
        val allHolidays = publicHolidayRepository.findAll()
        val holidayMap = allHolidays.associateBy { it.holidayDate }
        
        val monthHolidays = allHolidays
            .filter { YearMonth.from(it.holidayDate) == yearMonth }
            .map { HolidayResponse(date = it.holidayDate, label = it.name) }

        // 3. 결정된 연차 개수로 최적의 연휴 탐색
        val bestPeriods = findTopPeriodsInMonth(yearMonth, targetDayOff, holidayMap, limit = 3)

        return CalendarResponse(
            dayOffCount = targetDayOff, // 응답에는 실제 계산에 사용된 연차 개수를 반환
            bestPeriods = bestPeriods,
            holidays = monthHolidays
        )
    }

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

        for (i in 0 until daysInMonth) {
            val currentStart = startOfMonth.plusDays(i.toLong())
            var usedDayOff = 0
            var currentEnd = currentStart

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

        val sortedCandidates = candidates
            .distinctBy { it.start.toString() + it.end.toString() }
            .sortedWith(compareByDescending<VacationCandidate> { it.totalDays }.thenBy { it.start })

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
