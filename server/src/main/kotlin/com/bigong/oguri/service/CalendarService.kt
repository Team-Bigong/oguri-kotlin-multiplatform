package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.response.CalendarPeriodRecommendationResponse
import com.bigong.oguri.dto.response.CalendarResponse
import com.bigong.oguri.dto.response.PeriodDetailResponse
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.PublicHolidayRepository
import com.bigong.oguri.repository.SavedRecommendationRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
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
    private val homeService: HomeService,
    private val vacationRecommendationService: VacationRecommendationService,
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
        size: Int,
    ): PeriodDetailResponse {
        // 1. 공휴일 정보 로드
        val allHolidays = publicHolidayRepository.findAll()
        val holidayMap = allHolidays.associateBy { it.holidayDate }

        // 2. 해당 기간 내 공휴일 명칭 추출
        val actualHolidayNames = linkedSetOf<String>()
        val nonActualHolidayNames = linkedSetOf<String>()
        var curr = startDate
        while (!curr.isAfter(endDate)) {
            holidayMap[curr]
                ?.name
                ?.takeIf { holidayName: String -> holidayName.isNotBlank() }
                ?.let { holidayName: String ->
                    if (holidayMap[curr]?.isActualHoliday == true) {
                        actualHolidayNames.add(holidayName)
                    } else {
                        nonActualHolidayNames.add(holidayName)
                    }
                }
            curr = curr.plusDays(1)
        }
        val finalHolidays =
            when {
                actualHolidayNames.isNotEmpty() -> actualHolidayNames.toList()
                nonActualHolidayNames.isNotEmpty() -> nonActualHolidayNames.toList()
                else -> listOf(DEFAULT_HOLIDAY_NAME)
            }

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
            places = pagedPlaces,
        )
    }

    /**
     * 특정 기간 동안 실제로 사용하게 되는 연차 개수 계산
     */
    private fun calculateUsedDayOff(
        start: LocalDate,
        end: LocalDate,
        holidayMap: Map<LocalDate, PublicHoliday>,
    ): Int {
        var count = 0
        var curr = start
        while (!curr.isAfter(end)) {
            // 평일(월~금)이면서 공휴일이 아닌 날만 연차 소진으로 계산
            val isWeekend = curr.dayOfWeek == DayOfWeek.SATURDAY || curr.dayOfWeek == DayOfWeek.SUNDAY
            val isPublicHoliday = holidayMap.containsKey(curr)
            if (!isWeekend && !isPublicHoliday) {
                count++
            }
            curr = curr.plusDays(1)
        }
        return count
    }

    fun getCalendarData(
        year: Int,
        month: Int?,
        memberId: String,
        dayOffCount: Int?,
        page: Int,
        size: Int,
    ): CalendarResponse {
        validateCalendarFilter(year = year, month = month)

        val currentDate: LocalDate = LocalDate.now()
        val startMonth: Int =
            resolveSearchStartMonth(
                year = year,
                month = month,
                currentYear = currentDate.year,
                currentMonth = currentDate.monthValue,
            )
        val startYearMonth: YearMonth = YearMonth.of(year, startMonth)
        val monthRangeCount: Int =
            resolveSearchMonthRangeCount(
                month = month,
                startMonth = startMonth,
            )

        val targetDayOff = (dayOffCount ?: memberService.getPreferredDayOff(memberId)).coerceAtLeast(1)
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
        val allHolidays = publicHolidayRepository.findAll()
        val holidayMap = allHolidays.associateBy { it.holidayDate }
        val savedPeriodKeys =
            savedRecommendationRepository
                .findAllByMemberId(memberId)
                .map { recommendation ->
                    buildPeriodKey(
                        startDate = recommendation.startDate,
                        endDate = recommendation.endDate,
                    )
                }.toSet()

        val periodCandidates =
            findTopPeriods(
                startYearMonth = startYearMonth,
                userDayOff = targetDayOff,
                holidayMap = holidayMap,
                monthRangeCount = monthRangeCount,
                periodLimitPerMonth = PERIOD_LIMIT_PER_MONTH,
                startDateCutoff = currentDate,
            )

        val offset = normalizedPage * normalizedSize
        val pagedPeriods = periodCandidates.drop(offset).take(normalizedSize)
        val hasNext = offset + pagedPeriods.size < periodCandidates.size

        return CalendarResponse(
            dayOffCount = targetDayOff,
            page = normalizedPage,
            size = normalizedSize,
            hasNext = hasNext,
            periods =
                pagedPeriods.map { period ->
                    CalendarPeriodRecommendationResponse(
                        startDate = period.start,
                        endDate = period.end,
                        totalTripCount = period.totalDays,
                        holidayCount = period.holidayCount,
                        dayOffCount = period.usedDayOffCount,
                        holidays = period.holidayNames.ifEmpty { listOf(DEFAULT_HOLIDAY_NAME) },
                        holidayDateDetails = period.holidayDateDetails,
                        isSaved =
                            savedPeriodKeys.contains(
                                buildPeriodKey(
                                    startDate = period.start,
                                    endDate = period.end,
                                ),
                            ),
                    )
                },
        )
    }

    private fun findTopPeriods(
        startYearMonth: YearMonth,
        userDayOff: Int,
        holidayMap: Map<LocalDate, PublicHoliday>,
        monthRangeCount: Int,
        periodLimitPerMonth: Int,
        startDateCutoff: LocalDate,
    ): List<VacationRecommendationService.RecommendationPeriod> =
        vacationRecommendationService.findRecommendedPeriods(
            startYearMonth = startYearMonth,
            userDayOff = userDayOff,
            holidayMap = holidayMap,
            monthRangeCount = monthRangeCount,
            periodLimitPerMonth = periodLimitPerMonth,
            startDateCutoff = startDateCutoff,
        )

    private fun buildPeriodKey(
        startDate: LocalDate,
        endDate: LocalDate,
    ): String = "${startDate}_$endDate"

    private fun validateCalendarFilter(
        year: Int,
        month: Int?,
    ) {
        if (year < MINIMUM_SUPPORTED_YEAR) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "year는 $MINIMUM_SUPPORTED_YEAR 이상이어야 합니다.",
            )
        }
        if (month != null && month !in MONTH_MIN_VALUE..MONTH_MAX_VALUE) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "month는 $MONTH_MIN_VALUE~$MONTH_MAX_VALUE 범위여야 합니다.",
            )
        }
    }

    private fun resolveSearchStartMonth(
        year: Int,
        month: Int?,
        currentYear: Int,
        currentMonth: Int,
    ): Int {
        if (month != null) {
            return month
        }
        return if (year == currentYear) {
            currentMonth
        } else {
            MONTH_MIN_VALUE
        }
    }

    private fun resolveSearchMonthRangeCount(
        month: Int?,
        startMonth: Int,
    ): Int {
        if (month != null) {
            return SINGLE_MONTH_RANGE
        }
        return MONTH_MAX_VALUE - startMonth + 1
    }

    private companion object {
        private const val MINIMUM_SUPPORTED_YEAR = 1
        private const val MONTH_MIN_VALUE = 1
        private const val MONTH_MAX_VALUE = 12
        private const val SINGLE_MONTH_RANGE = 1
        private const val PERIOD_LIMIT_PER_MONTH = 6
        private const val MIN_PAGE_SIZE = 1
        private const val MAX_PAGE_SIZE = 50
        private const val DEFAULT_HOLIDAY_NAME = "주말"
    }
}
