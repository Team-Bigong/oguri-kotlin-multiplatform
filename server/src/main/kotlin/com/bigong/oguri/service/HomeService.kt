package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.domain.SearchLog
import com.bigong.oguri.dto.response.*
import com.bigong.oguri.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * 홈 화면 데이터 구성을 담당하는 서비스
 */
@Service
@Transactional(readOnly = true)
class HomeService(
    private val destinationRepository: DestinationRepository,
    private val publicHolidayRepository: PublicHolidayRepository,
    private val savedRecommendationRepository: SavedRecommendationRepository,
    private val savedDestinationRepository: SavedDestinationRepository,
    private val searchLogRepository: SearchLogRepository,
    private val memberService: MemberService,
    private val vacationRecommendationService: VacationRecommendationService,
    private val placeRecommendationService: PlaceRecommendationService,
) {
    // 공휴일 데이터는 자주 바뀌지 않으므로 서버 메모리에 캐싱하여 성능을 높입니다.
    private var cachedHolidays: Map<LocalDate, PublicHoliday> = emptyMap()
    private var lastHolidayUpdate: LocalDate? = null

    companion object {
        private const val HOME_RECOMMENDATION_MONTH_RANGE = 12
        private const val HOME_PERIOD_LIMIT_PER_MONTH = 6
        private const val HOME_TOP_RECOMMENDATION_LIMIT = 3
        private const val DEFAULT_HOLIDAY_NAME = "주말"
        private const val WEEKLY_TOP_PLACES_LIMIT = 5
        private const val TRENDING_SEARCH_TERMS_LIMIT = 7
    }

    /**
     * 하루에 한 번 또는 서버 기동 시 공휴일 캐시를 업데이트합니다.
     */
    private fun refreshHolidayCacheIfNeeded() {
        val today = LocalDate.now()
        if (lastHolidayUpdate != today || cachedHolidays.isEmpty()) {
            cachedHolidays = publicHolidayRepository.findAll().associateBy { it.holidayDate }
            lastHolidayUpdate = today
        }
    }

    /**
     * 홈 화면 메인 API 로직
     */
    fun getHomeData(
        userCountry: String,
        memberId: String,
    ): List<HomeRecommendPeriodResponse> {
        refreshHolidayCacheIfNeeded()

        // 1. 사용자 정보 로드 (선호 연차 및 잔여 연차)
        val memberInfo = memberService.getDayOffInfo(memberId)
        val preferredDayOff = memberInfo.preferredDayOff
        val today = LocalDate.now()
        val savedPeriods = savedRecommendationRepository.findAllByMemberId(memberId)
        val allDestinations = destinationRepository.findAllWithCountryAndImages()

        val bestPeriods = vacationRecommendationService.findRecommendedPeriods(
            startYearMonth = YearMonth.from(today),
            userDayOff = preferredDayOff,
            holidayMap = cachedHolidays,
            monthRangeCount = HOME_RECOMMENDATION_MONTH_RANGE,
            periodLimitPerMonth = HOME_PERIOD_LIMIT_PER_MONTH,
            startDateCutoff = today,
        )
        val nonOverlappingTopPeriods = selectNonOverlappingTopPeriods(bestPeriods, HOME_TOP_RECOMMENDATION_LIMIT)

        return nonOverlappingTopPeriods.mapIndexed { index, period ->
            val recommendedPlaces = placeRecommendationService.calculateRecommendedPlaces(period.start, allDestinations, userCountry, period.totalDays)
            val destinationsById = allDestinations.associateBy { it.id }
            val advertisements = placeRecommendationService.buildAdvertisementsFromPlaces(recommendedPlaces, destinationsById)
            val isSaved = savedPeriods.any { it.startDate == period.start && it.endDate == period.end }

            HomeRecommendPeriodResponse(
                rank = index + 1,
                isSaved = isSaved,
                startDate = period.start,
                endDate = period.end,
                holiday = period.holidayNames.ifEmpty { listOf(DEFAULT_HOLIDAY_NAME) },
                dayOffCount = period.usedDayOffCount,
                totalTripCount = period.totalDays,
                places = recommendedPlaces,
                advertisements = advertisements,
            )
        }
    }

    /**
     * 이번 주 인기 여행지 조회 API 로직
     */
    fun getWeeklyTopPlaces(): HomeWeeklyTopResponse {
        val allDestinations = destinationRepository.findAllWithCountryAndImages()
        val since = LocalDateTime.now().minusDays(7)
        val topIds = savedDestinationRepository.findTopDestinationIdsByCountAndNameAsc(since, WEEKLY_TOP_PLACES_LIMIT)
        val destinationsById = allDestinations.associateBy { it.id }

        val weeklyTopPlaces = topIds.mapNotNull { id ->
            destinationsById[id]?.let { dest ->
                val thumbnailUrl = dest.images.find { it.isThumbnail }?.imageUrl ?: ""
                HomeWeeklyTopPlaceResponse(
                    id = dest.id.toLong(),
                    country = dest.country?.name ?: "Unknown",
                    city = dest.name,
                    summary = dest.summary ?: "",
                    thumbnailUrl = thumbnailUrl,
                )
            }
        }

        return HomeWeeklyTopResponse(weeklyTopPlaces = weeklyTopPlaces)
    }

    /**
     * 이번 달 인기 추천 기간 조회 API 로직
     */
    fun getMonthlyTopPeriods(): List<HomeMonthlyTopPeriodResponse> {
        refreshHolidayCacheIfNeeded()
        val since = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        val topPeriodsData = savedRecommendationRepository.findTopSavedPeriodsByCount(since, 3)

        return topPeriodsData.map { data ->
            val start = (data[0] as java.sql.Date).toLocalDate()
            val end = (data[1] as java.sql.Date).toLocalDate()
            val dayOff = (data[2] as Number).toInt()
            val total = (data[3] as Number).toInt()
            val holidayCount = total - dayOff

            HomeMonthlyTopPeriodResponse(
                startDate = start,
                endDate = end,
                totalTripCount = total,
                holidayCount = holidayCount,
                dayOffCount = dayOff,
            )
        }
    }

    /**
     * 검색어 로깅 로직
     */
    @Transactional
    fun logSearchTerm(memberId: String, query: String) {
        if (query.isBlank()) return
        searchLogRepository.save(SearchLog(memberId = memberId, query = query.trim()))
    }

    /**
     * 인기 검색어 조회 로직
     */
    fun getTrendingSearchTerms(): List<String> {
        val since = LocalDateTime.now().minusDays(7)
        return searchLogRepository.findTrendingSearchTerms(since, TRENDING_SEARCH_TERMS_LIMIT)
    }

    /**
     * 검색 자동완성 추천 로직
     */
    fun getAutocompleteSuggestions(query: String): List<SearchAutocompleteItemResponse> {
        if (query.isBlank()) return emptyList()

        val pageable = PageRequest.of(0, 10)
        val destinations = destinationRepository.findAutocompleteSuggestions(query.trim(), pageable)

        return destinations.map { dest ->
            SearchAutocompleteItemResponse(
                id = dest.id.toLong(),
                destinationName = dest.name,
                countryName = dest.country?.name ?: "Unknown",
            )
        }
    }

    private fun selectNonOverlappingTopPeriods(
        periods: List<VacationRecommendationService.RecommendationPeriod>,
        limit: Int,
    ): List<VacationRecommendationService.RecommendationPeriod> {
        val selectedPeriods = mutableListOf<VacationRecommendationService.RecommendationPeriod>()
        for (period in periods) {
            if (selectedPeriods.size >= limit) {
                break
            }
            val isOverlapping =
                selectedPeriods.any { selected ->
                    !selected.end.isBefore(period.start) && !period.end.isBefore(selected.start)
                }
            if (!isOverlapping) {
                selectedPeriods.add(period)
            }
        }
        return selectedPeriods
    }
}
