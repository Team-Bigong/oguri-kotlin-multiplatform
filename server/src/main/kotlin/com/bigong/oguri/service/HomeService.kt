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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
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
    private val memberService: MemberService
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
     * 오늘부터 8개월 이내의 최적 연휴 Top 3를 찾고, 각각에 맞는 여행지를 추천합니다.
     */
    fun getHomeData(userCountry: String, memberId: String): List<RecommendPeriodResponse> {
        refreshHolidayCacheIfNeeded()
        
        // 1. 사용자 정보 로드 (선호 연차 및 잔여 연차)
        val memberInfo = memberService.getDayOffInfo(memberId)
        val preferredDayOff = memberInfo.preferredDayOff
        val remainingDayOff = memberInfo.remainingDayOff
        
        // 2. 사용자가 저장한 연휴 목록 조회
        val savedPeriods = savedRecommendationRepository.findAllByMemberId(memberId)
        
        // 3. 여행지 데이터 로드 (Fetch Join으로 N+1 문제 해결)
        val allDestinations = destinationRepository.findAllWithCountryAndImages()

        // 4. 최적의 연차 사용 기간 탐색 (선호 연차 기준)
        val bestPeriods = findTopVacationPeriods(preferredDayOff, holidayMap = cachedHolidays, limit = 3)

        // 5. 광고 데이터 구성 (더미)
        val advertisements = listOf(
            AdvertisementResponse(platform = "agoda", url = "https://www.agoda.com"),
            AdvertisementResponse(platform = "skyscanner", url = "https://www.skyscanner.com/"),
            AdvertisementResponse(platform = "klook", url = "https://www.klook.com/")
        )

        // 6. 각 추천 기간별로 응답 조립
        return bestPeriods.mapIndexed { index, period ->
            // 해당 기간에 가장 가기 좋은 장소 7개 계산
            val recommendedPlaces = calculateRecommendedPlaces(period.start, allDestinations, userCountry, period.totalDays)

            val isSaved = savedPeriods.any { it.startDate == period.start && it.endDate == period.end }
            val holidayNames = period.holidayObjects.filter { it.isActualHoliday }.map { it.name }.distinct()

            RecommendPeriodResponse(
                rank = index + 1,
                isSaved = isSaved,
                startDate = period.start,
                endDate = period.end,
                holiday = if (holidayNames.isEmpty() && period.totalDays > 0) listOf("주말") else holidayNames,
                dayOffCount = remainingDayOff,
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
        val flightTimes = candidates.map { parseFlightTime(it.flightTime) }
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
            val flightVal = parseFlightTime(dest.flightTime)
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
    private fun parseFlightTime(flightTime: String?): Double {
        if (flightTime == null) return 0.0
        return flightTime.replace(NUMBER_ONLY_REGEX, "").toDoubleOrNull() ?: 0.0
    }

    /**
     * 겹치지 않는 최적의 연휴 구간 Top 3 탐색
     */
    private fun findTopVacationPeriods(
        userDayOff: Int,
        holidayMap: Map<LocalDate, PublicHoliday>,
        limit: Int
    ): List<VacationPeriod> {
        val now = LocalDate.now()
        val endSearchDate = now.plusMonths(8)
        val daysToSearch = ChronoUnit.DAYS.between(now, endSearchDate).toInt() + 1

        val candidates = mutableListOf<VacationPeriod>()
        val searchWindow = userDayOff * 2 + 10

        // 슬라이딩 윈도우 방식으로 매일의 최적 휴가 구간 계산
        for (i in 0 until daysToSearch) {
            val currentStart = now.plusDays(i.toLong())
            var usedDayOff = 0
            var currentEnd = currentStart
            val holidayIndicesInPeriod = mutableListOf<PublicHoliday>()

            val maxRange = if (i + searchWindow < daysToSearch) i + searchWindow else daysToSearch
            for (j in i until maxRange) {
                val date = now.plusDays(j.toLong())
                if (!isOffDay(date, holidayMap)) {
                    if (usedDayOff < userDayOff) usedDayOff++ else break
                } else {
                    holidayMap[date]?.let { holidayIndicesInPeriod.add(it) }
                }
                currentEnd = date
            }
            
            val totalDays = ChronoUnit.DAYS.between(currentStart, currentEnd).toInt() + 1
            if (totalDays > 0) {
                candidates.add(VacationPeriod(currentStart, currentEnd, totalDays, holidayIndicesInPeriod.toList()))
            }
        }

        // 정렬 및 중복 구간 필터링
        val sortedCandidates = candidates.sortedWith(compareByDescending<VacationPeriod> { it.totalDays }.thenBy { it.start })
        val selected = mutableListOf<VacationPeriod>()
        for (candidate in sortedCandidates) {
            if (selected.size >= limit) break
            if (selected.none { it.overlapsWith(candidate) }) {
                selected.add(candidate)
            }
        }
        return selected
    }

    private fun isOffDay(date: LocalDate, holidayMap: Map<LocalDate, PublicHoliday>): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY || holidayMap.containsKey(date)
    }

    data class VacationPeriod(
        val start: LocalDate,
        val end: LocalDate,
        val totalDays: Int,
        val holidayObjects: List<PublicHoliday>
    ) {
        fun overlapsWith(other: VacationPeriod): Boolean {
            return !this.end.isBefore(other.start) && !other.end.isBefore(this.start)
        }
    }
}
