package com.bigong.oguri.service

import com.bigong.oguri.domain.Destination
import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.AdvertisementResponse
import com.bigong.oguri.dto.PlaceResponse
import com.bigong.oguri.dto.RecommendPeriodResponse
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.PublicHolidayRepository
import com.bigong.oguri.repository.SavedRecommendationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

/**
 * 홈 화면 데이터 및 추천 로직을 담당하는 핵심 서비스
 */
@Service
@Transactional(readOnly = true)
class HomeService(
    private val destinationRepository: DestinationRepository,
    private val publicHolidayRepository: PublicHolidayRepository,
    private val savedRecommendationRepository: SavedRecommendationRepository,
    private val memberService: MemberService,
    private val vacationRecommendationService: VacationRecommendationService
) {
    // 공휴일 데이터는 자주 바뀌지 않으므로 서버 메모리에 캐싱하여 성능을 높입니다.
    private var cachedHolidays: Map<LocalDate, PublicHoliday> = emptyMap()
    private var lastHolidayUpdate: LocalDate? = null

    companion object {
        const val WEIGHT_FLIGHT_TIME = 0.6    // 비행시간 가중치 (60%)
        const val WEIGHT_BIG_MAC_INDEX = 0.4  // 물가(빅맥지수) 가중치 (40%)
        const val MAX_RECOMMENDATIONS = 7     // 한 번에 추천할 최대 장소 개수
        
        // 휴가 일수에 따른 비행시간 타겟 기준
        const val MID_TRIP_THRESHOLD = 5     // 5일 이상이면 중거리(동남아 등) 선호
        const val LONG_TRIP_THRESHOLD = 7    // 7일 이상이면 장거리(유럽, 미국 등) 선호
        
        private val NUMBER_ONLY_REGEX = Regex("[^0-9]") // 비행시간 숫자 추출용 정규식
        private const val HOME_RECOMMENDATION_MONTH_RANGE = 12
        private const val HOME_PERIOD_LIMIT_PER_MONTH = 6
        private const val HOME_TOP_RECOMMENDATION_LIMIT = 3
        private const val DEFAULT_HOLIDAY_NAME = "주말"
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
     * 오늘 이후의 최적 연휴 Top 3를 찾고, 각각에 맞는 여행지를 추천합니다.
     */
    fun getHomeData(userCountry: String, memberId: String): List<RecommendPeriodResponse> {
        refreshHolidayCacheIfNeeded()
        
        // 1. 사용자 정보 로드 (선호 연차 및 잔여 연차)
        val memberInfo = memberService.getDayOffInfo(memberId)
        val preferredDayOff = memberInfo.preferredDayOff
        val today = LocalDate.now()
        
        // 2. 사용자가 저장한 연휴 목록 조회
        val savedPeriods = savedRecommendationRepository.findAllByMemberId(memberId)
        
        // 3. 여행지 데이터 로드 (Fetch Join으로 N+1 문제 해결)
        val allDestinations = destinationRepository.findAllWithCountryAndImages()

        // 4. 최적의 연차 사용 기간 탐색 (선호 연차 기준)
        val bestPeriods = vacationRecommendationService.findRecommendedPeriods(
            startYearMonth = YearMonth.from(today),
            userDayOff = preferredDayOff,
            holidayMap = cachedHolidays,
            monthRangeCount = HOME_RECOMMENDATION_MONTH_RANGE,
            periodLimitPerMonth = HOME_PERIOD_LIMIT_PER_MONTH,
            startDateCutoff = today
        )
        val nonOverlappingTopPeriods = selectNonOverlappingTopPeriods(bestPeriods, HOME_TOP_RECOMMENDATION_LIMIT)

        // 5. 광고 데이터 구성 (더미)
        val advertisements = listOf(
            AdvertisementResponse(platform = "agoda", url = "https://www.agoda.com"),
            AdvertisementResponse(platform = "skyscanner", url = "https://www.skyscanner.com/"),
            AdvertisementResponse(platform = "klook", url = "https://www.klook.com/")
        )

        // 6. 각 추천 기간별로 응답 조립
        return nonOverlappingTopPeriods.mapIndexed { index, period ->
            // 해당 기간에 가장 가기 좋은 장소 7개 계산
            val recommendedPlaces = calculateRecommendedPlaces(period.start, allDestinations, userCountry, period.totalDays)

            val isSaved = savedPeriods.any { it.startDate == period.start && it.endDate == period.end }
            RecommendPeriodResponse(
                rank = index + 1,
                isSaved = isSaved,
                startDate = period.start,
                endDate = period.end,
                holiday = period.holidayNames.ifEmpty { listOf(DEFAULT_HOLIDAY_NAME) },
                dayOffCount = period.usedDayOffCount,
                totalTripCount = period.totalDays,
                places = recommendedPlaces,
                advertisements = advertisements
            )
        }
    }

    /**
     * 여행 일수 및 물가를 고려한 맞춤 장소 추천 알고리즘
     */
    fun calculateRecommendedPlaces(
        startDate: LocalDate,
        destinations: List<Destination>,
        userCountry: String,
        totalTripCount: Int
    ): List<PlaceResponse> {
        return calculateRecommendedPlacesAll(startDate, destinations, userCountry, totalTripCount)
            .take(MAX_RECOMMENDATIONS)
    }

    fun calculateRecommendedPlacesAll(
        startDate: LocalDate,
        destinations: List<Destination>,
        userCountry: String,
        totalTripCount: Int
    ): List<PlaceResponse> {
        val targetMonth = startDate.monthValue
        
        // 1단계: 해당 월에 추천되는 장소들만 필터링
        val candidates = destinations.filter { dest ->
            isMonthInRange(targetMonth, dest.recommendStartMonth1, dest.recommendEndMonth1) ||
            isMonthInRange(targetMonth, dest.recommendStartMonth2, dest.recommendEndMonth2)
        }
        if (candidates.isEmpty()) return emptyList()

        // 2단계: 정규화를 위한 최소/최대 지표 파악
        val flightTimes = candidates.map { parseFlightTime(it.flightTimeMinutes, it.flightTime) }
        val bigMacIndices = candidates.map { it.country?.bigMacIndex?.toDouble() ?: 5.0 }
        val minFlight = flightTimes.minOrNull() ?: 0.0
        val maxFlight = flightTimes.maxOrNull() ?: 1.0
        val minBigMac = bigMacIndices.minOrNull() ?: 0.0
        val maxBigMac = bigMacIndices.maxOrNull() ?: 1.0

        // 3단계: 휴가 일수에 따른 비행시간 타겟 설정 (Target Proximity Algorithm)
        val targetFlightValue = when {
            totalTripCount >= LONG_TRIP_THRESHOLD -> 1.0  // 장거리 여행 선호
            totalTripCount >= MID_TRIP_THRESHOLD  -> 0.35 // 중거리 여행 선호
            else -> 0.0                                   // 단거리 여행 선호
        }

        // 4단계: 개별 장소별 점수 산산 및 정규화
        return candidates.map { dest ->
            val flightVal = parseFlightTime(dest.flightTimeMinutes, dest.flightTime)
            val bigMacVal = dest.country?.bigMacIndex?.toDouble() ?: 5.0
            val normalizedFlight = if (maxFlight != minFlight) (flightVal - minFlight) / (maxFlight - minFlight) else 0.0
            
            // 비행 점수: 타겟 거리에 가까울수록 고득점
            val flightScore = 1.0 - abs(normalizedFlight - targetFlightValue)
            // 물가 점수: 저렴할수록 고득점
            val bigMacScore = if (maxBigMac != minBigMac) 1.0 - (bigMacVal - minBigMac) / (maxBigMac - minBigMac) else 1.0
            
            val totalScore = (flightScore * WEIGHT_FLIGHT_TIME) + (bigMacScore * WEIGHT_BIG_MAC_INDEX)
            dest to totalScore
        }
        // 5단계: 정렬 및 최종 선택 (해외 우선 정렬)
        .sortedWith(
            compareBy<Pair<Destination, Double>> { if (it.first.country?.name == userCountry) 1 else 0 }
            .thenByDescending { it.second }
        )
        .map { (dest, _) ->
            val thumbnailUrl = dest.images.find { it.isThumbnail }?.imageUrl ?: ""
            PlaceResponse(
                id = dest.id.toLong(),
                country = dest.country?.name ?: "Unknown",
                city = dest.name,
                summary = dest.summary ?: "",
                thumbnailUrl = thumbnailUrl,
                isSaved = false // 홈 화면에서는 장소별 찜 여부를 표시하지 않음 (기획 요청)
            )
        }
    }

    /**
     * 월 범위 체크 (연도 걸침 로직 포함)
     */
    private fun isMonthInRange(month: Int, start: Int?, end: Int?): Boolean {
        if (start == null || end == null) return false
        return if (start <= end) month in start..end else (month >= start || month <= end)
    }

    /**
     * 비행시간 문자열에서 숫자 추출
     */
    private fun parseFlightTime(flightTimeMinutes: Int?, legacyFlightTime: String?): Double {
        if (flightTimeMinutes != null) {
            return flightTimeMinutes.toDouble()
        }
        if (legacyFlightTime == null) {
            return 0.0
        }
        return legacyFlightTime.replace(NUMBER_ONLY_REGEX, "").toDoubleOrNull() ?: 0.0
    }

    private fun selectNonOverlappingTopPeriods(
        periods: List<VacationRecommendationService.RecommendationPeriod>,
        limit: Int
    ): List<VacationRecommendationService.RecommendationPeriod> {
        val selectedPeriods = mutableListOf<VacationRecommendationService.RecommendationPeriod>()
        for (period in periods) {
            if (selectedPeriods.size >= limit) {
                break
            }
            val isOverlapping = selectedPeriods.any { selected ->
                !selected.end.isBefore(period.start) && !period.end.isBefore(selected.start)
            }
            if (!isOverlapping) {
                selectedPeriods.add(period)
            }
        }
        return selectedPeriods
    }

}
