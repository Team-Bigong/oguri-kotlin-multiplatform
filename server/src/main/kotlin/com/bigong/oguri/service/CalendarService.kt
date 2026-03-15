package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.BestPeriodResponse
import com.bigong.oguri.dto.CalendarResponse
import com.bigong.oguri.dto.HolidayResponse
import com.bigong.oguri.dto.PeriodDetailResponse
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.PublicHolidayRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 캘린더 및 기간 상세 데이터 제공 서비스
 */
@Service
@Transactional(readOnly = true)
class CalendarService(
    private val memberService: MemberService,
    private val publicHolidayRepository: PublicHolidayRepository,
    private val destinationRepository: DestinationRepository,
    private val homeService: HomeService
) {
    /**
     * 특정 기간(시작~종료)의 상세 정보 및 추천 장소 조회
     */
    fun getPeriodDetail(
        startDate: LocalDate,
        endDate: LocalDate,
        userCountry: String,
        memberId: String
    ): PeriodDetailResponse {
        // 1. 공휴일 정보 로드
        val allHolidays = publicHolidayRepository.findAll()
        val holidayMap = allHolidays.associateBy { it.holidayDate }

        // 2. 해당 기간 내 공휴일 명칭 추출 (isActualHoliday인 것만)
        val holidayNames = mutableListOf<String>()
        var curr = startDate
        while (!curr.isAfter(endDate)) {
            holidayMap[curr]?.let { if (it.isActualHoliday) holidayNames.add(it.name) }
            curr = curr.plusDays(1)
        }
        val finalHolidays = if (holidayNames.isEmpty()) listOf("주말") else holidayNames.distinct()

        // 3. 총 휴가 일수 및 연차 사용 개수 계산
        val totalTripCount = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val dayOffCount = calculateUsedDayOff(startDate, endDate, holidayMap)

        // 4. 해당 기간에 최적화된 추천 장소 7개 계산 (HomeService 로직 재사용)
        val allDestinations = destinationRepository.findAllWithCountryAndImages()
        val recommendedPlaces = homeService.calculateRecommendedPlaces(startDate, allDestinations, userCountry, totalTripCount)

        return PeriodDetailResponse(
            startDate = startDate,
            endDate = endDate,
            holiday = finalHolidays,
            dayOffCount = dayOffCount,
            totalTripCount = totalTripCount,
            places = recommendedPlaces
        )
    }

    /**
     * 특정 기간 동안 실제로 사용하게 되는 연차 개수 계산
     */
    private fun calculateUsedDayOff(start: LocalDate, end: LocalDate, holidayMap: Map<LocalDate, PublicHoliday>): Int {
        var count = 0
        var curr = start
        while (!curr.isAfter(end)) {
            // 평일(월~금)이면서 공휴일이 아닌 날만 연차 소진으로 계산
            if (curr.dayOfWeek != DayOfWeek.SATURDAY && curr.dayOfWeek != DayOfWeek.SUNDAY && !holidayMap.containsKey(curr)) {
                count++
            }
            curr = curr.plusDays(1)
        }
        return count
    }

    /**
     * 특정 년월의 캘린더 데이터 조회
     */
    fun getCalendarData(yearMonth: YearMonth, memberId: String, dayOffCount: Int?): CalendarResponse {
        val targetDayOff = dayOffCount ?: memberService.getPreferredDayOff(memberId)
        val allHolidays = publicHolidayRepository.findAll()
        val holidayMap = allHolidays.associateBy { it.holidayDate }

        val monthHolidays = allHolidays
            .filter { YearMonth.from(it.holidayDate) == yearMonth }
            .map { HolidayResponse(date = it.holidayDate, label = it.name) }

        val bestPeriods = findTopPeriodsInMonth(yearMonth, targetDayOff, holidayMap, limit = 3)

        return CalendarResponse(
            dayOffCount = targetDayOff,
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
        val searchWindow = userDayOff * 2 + 10

        for (i in 0 until daysInMonth) {
            val currentStart = startOfMonth.plusDays(i.toLong())
            var usedDayOff = 0
            var currentEnd = currentStart

            val maxRange = if (i + searchWindow < daysInMonth) i + searchWindow else daysInMonth
            for (j in i until maxRange) {
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
