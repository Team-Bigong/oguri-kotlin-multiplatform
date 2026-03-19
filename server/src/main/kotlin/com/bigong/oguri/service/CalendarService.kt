package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.CalendarPeriodRecommendationResponse
import com.bigong.oguri.dto.CalendarResponse
import com.bigong.oguri.dto.PeriodDetailResponse
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.PublicHolidayRepository
import com.bigong.oguri.repository.SavedRecommendationRepository
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
    private val savedRecommendationRepository: SavedRecommendationRepository,
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
        memberId: String,
        page: Int,
        size: Int
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
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)

        // 4. 해당 기간에 최적화된 추천 장소 전체 계산 후 페이지 단위로 분할
        val allDestinations = destinationRepository.findAllWithCountryAndImages()
        val allRecommendedPlaces = homeService.calculateRecommendedPlacesAll(startDate, allDestinations, userCountry, totalTripCount)
        val offset = normalizedPage * normalizedSize
        val pagedPlaces = allRecommendedPlaces.drop(offset).take(normalizedSize)
        val hasNext = offset + pagedPlaces.size < allRecommendedPlaces.size

        return PeriodDetailResponse(
            startDate = startDate,
            endDate = endDate,
            holiday = finalHolidays,
            dayOffCount = dayOffCount,
            totalTripCount = totalTripCount,
            page = normalizedPage,
            size = normalizedSize,
            hasNext = hasNext,
            places = pagedPlaces
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

    fun getCalendarData(
        yearMonth: YearMonth,
        memberId: String,
        dayOffCount: Int?,
        page: Int,
        size: Int
    ): CalendarResponse {
        val targetDayOff = (dayOffCount ?: memberService.getPreferredDayOff(memberId)).coerceAtLeast(1)
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
        val allHolidays = publicHolidayRepository.findAll()
        val holidayMap = allHolidays.associateBy { it.holidayDate }
        val savedPeriodKeys = savedRecommendationRepository.findAllByMemberId(memberId).map { recommendation ->
            buildPeriodKey(
                startDate = recommendation.startDate,
                endDate = recommendation.endDate
            )
        }.toSet()

        val periodCandidates = findTopPeriods(
            startYearMonth = yearMonth,
            userDayOff = targetDayOff,
            holidayMap = holidayMap,
            monthRangeCount = RECOMMENDATION_MONTH_RANGE,
            periodLimitPerMonth = PERIOD_LIMIT_PER_MONTH
        )

        val sortedPeriods = periodCandidates.sortedWith(
            compareByDescending<VacationCandidate> { it.totalDays }
                .thenBy { it.usedDayOffCount }
                .thenByDescending { it.holidayCount }
                .thenBy { it.start }
        )

        val offset = normalizedPage * normalizedSize
        val pagedPeriods = sortedPeriods.drop(offset).take(normalizedSize)
        val hasNext = offset + pagedPeriods.size < sortedPeriods.size

        return CalendarResponse(
            dayOffCount = targetDayOff,
            page = normalizedPage,
            size = normalizedSize,
            hasNext = hasNext,
            periods = pagedPeriods.map { period ->
                CalendarPeriodRecommendationResponse(
                    startDate = period.start,
                    endDate = period.end,
                    totalTripCount = period.totalDays,
                    holidayCount = period.holidayCount,
                    dayOffCount = period.usedDayOffCount,
                    holidays = period.holidayNames.ifEmpty { listOf(DEFAULT_HOLIDAY_NAME) },
                    isSaved = savedPeriodKeys.contains(
                        buildPeriodKey(
                            startDate = period.start,
                            endDate = period.end
                        )
                    )
                )
            }
        )
    }

    private fun findTopPeriods(
        startYearMonth: YearMonth,
        userDayOff: Int,
        holidayMap: Map<LocalDate, PublicHoliday>,
        monthRangeCount: Int,
        periodLimitPerMonth: Int
    ): List<VacationCandidate> {
        val candidates = mutableListOf<VacationCandidate>()
        val searchWindow = userDayOff * 2 + 10

        repeat(monthRangeCount) { monthOffset ->
            val currentYearMonth = startYearMonth.plusMonths(monthOffset.toLong())
            val startOfMonth = currentYearMonth.atDay(1)
            val endOfMonth = currentYearMonth.atEndOfMonth()
            val daysInMonth = ChronoUnit.DAYS.between(startOfMonth, endOfMonth).toInt() + 1
            val monthlyCandidates = mutableListOf<VacationCandidate>()

            for (dayIndex in 0 until daysInMonth) {
                val currentStart = startOfMonth.plusDays(dayIndex.toLong())
                var usedDayOffCount = 0
                var currentEnd = currentStart
                var holidayCount = 0
                val holidayNames = linkedSetOf<String>()

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
                    } else {
                        if (usedDayOffCount < userDayOff) {
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
                        VacationCandidate(
                            start = currentStart,
                            end = currentEnd,
                            totalDays = totalDays,
                            usedDayOffCount = usedDayOffCount,
                            holidayCount = holidayCount,
                            holidayNames = holidayNames.toList()
                        )
                    )
                }
            }

            val selectedMonthlyCandidates = monthlyCandidates
                .distinctBy { candidate -> buildPeriodKey(candidate.start, candidate.end) }
                .sortedWith(
                    compareByDescending<VacationCandidate> { it.totalDays }
                        .thenBy { it.usedDayOffCount }
                        .thenByDescending { it.holidayCount }
                        .thenBy { it.start }
                ).take(periodLimitPerMonth)
            candidates.addAll(selectedMonthlyCandidates)
        }
        return candidates.distinctBy { candidate -> buildPeriodKey(candidate.start, candidate.end) }
    }

    private fun isOffDay(date: LocalDate, holidayMap: Map<LocalDate, PublicHoliday>): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY ||
               date.dayOfWeek == DayOfWeek.SUNDAY ||
               holidayMap.containsKey(date)
    }

    private data class VacationCandidate(
        val start: LocalDate,
        val end: LocalDate,
        val totalDays: Int,
        val usedDayOffCount: Int,
        val holidayCount: Int,
        val holidayNames: List<String>
    )

    private fun buildPeriodKey(startDate: LocalDate, endDate: LocalDate): String = "${startDate}_${endDate}"

    private companion object {
        private const val RECOMMENDATION_MONTH_RANGE = 12
        private const val PERIOD_LIMIT_PER_MONTH = 6
        private const val MIN_PAGE_SIZE = 1
        private const val MAX_PAGE_SIZE = 50
        private const val DEFAULT_HOLIDAY_NAME = "주말"
    }
}
