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
 * 성능 및 추천 로직이 최적화된 홈 화면 서비스
 * 휴가 기간별(단/중/장거리) 맞춤 비행시간 추천 엔진 포함
 */
@Service
@Transactional(readOnly = true)
class HomeService(
    private val destinationRepository: DestinationRepository,
    private val publicHolidayRepository: PublicHolidayRepository,
    private val savedRecommendationRepository: SavedRecommendationRepository,
    private val memberService: MemberService
) {
    companion object {
        const val WEIGHT_FLIGHT_TIME = 0.6
        const val WEIGHT_BIG_MAC_INDEX = 0.4
        const val MAX_RECOMMENDATIONS = 7
        
        // 휴가 기간별 구간 임계값
        const val MID_TRIP_THRESHOLD = 5     // 5일 이상이면 중거리 고려
        const val LONG_TRIP_THRESHOLD = 7    // 7일 이상이면 장거리 고려
    }

    /**
     * 홈 화면 데이터 조회
     */
    fun getHomeData(userCountry: String, memberId: String): List<RecommendPeriodResponse> {
        val dayOffCount = memberService.getDayOffCount(memberId)
        val holidayMap = publicHolidayRepository.findAll().associateBy { it.holidayDate }
        val savedPeriods = savedRecommendationRepository.findAllByMemberId(memberId)
        val allDestinations = destinationRepository.findAllWithCountryAndImages()

        val bestPeriods = findTopVacationPeriods(dayOffCount, holidayMap, limit = 3)

        val advertisements = listOf(
            AdvertisementResponse(platform = "google", url = "https://www.google.com"),
            AdvertisementResponse(platform = "naver", url = "https://www.naver.com")
        )

        return bestPeriods.mapIndexed { index, period ->
            val recommendedPlaces = calculateRecommendedPlaces(period.start, allDestinations, userCountry, period.totalDays)

            val isSaved = savedPeriods.any { 
                it.startDate == period.start && it.endDate == period.end 
            }

            val holidayNames = period.holidayObjects
                .filter { it.isActualHoliday }
                .map { it.name }
                .distinct()

            RecommendPeriodResponse(
                rank = index + 1,
                isSaved = isSaved,
                startDate = period.start,
                endDate = period.end,
                holiday = if (holidayNames.isEmpty() && period.totalDays > 0) listOf("주말") else holidayNames,
                dayOffCount = dayOffCount,
                totalTripCount = period.totalDays,
                places = recommendedPlaces,
                advertisements = advertisements
            )
        }
    }

    /**
     * 휴가 일수에 따른 단/중/장거리 맞춤 추천 로직
     */
    private fun calculateRecommendedPlaces(
        startDate: LocalDate,
        destinations: List<Destination>,
        userCountry: String,
        totalTripCount: Int
    ): List<PlaceResponse> {
        val targetMonth = startDate.monthValue

        // 1. 시기 필터링
        val candidates = destinations.filter { dest ->
            isMonthInRange(targetMonth, dest.recommendStartMonth1, dest.recommendEndMonth1) ||
            isMonthInRange(targetMonth, dest.recommendStartMonth2, dest.recommendEndMonth2)
        }

        if (candidates.isEmpty()) return emptyList()

        // 2. 정규화 범위 파악
        val flightTimes = candidates.map { parseFlightTime(it.flightTime) }
        val bigMacIndices = candidates.map { it.country?.bigMacIndex?.toDouble() ?: 5.0 }

        val minFlight = flightTimes.minOrNull() ?: 0.0
        val maxFlight = flightTimes.maxOrNull() ?: 1.0
        val minBigMac = bigMacIndices.minOrNull() ?: 0.0
        val maxBigMac = bigMacIndices.maxOrNull() ?: 1.0

        // [핵심] 휴가 기간에 따른 목표 비행 점수(Target Score) 설정
        val targetFlightValue = when {
            totalTripCount >= LONG_TRIP_THRESHOLD -> 1.0  // 장거리 (7일 이상): 가장 먼 곳 선호
            totalTripCount >= MID_TRIP_THRESHOLD  -> 0.35 // 중거리 (5~6일): 약 5~6시간 비행 거리 선호
            else -> 0.0                                   // 단거리 (5일 미만): 가장 가까운 곳 선호
        }

        // 3. 점수 계산
        return candidates.map { dest ->
            val flightVal = parseFlightTime(dest.flightTime)
            val bigMacVal = dest.country?.bigMacIndex?.toDouble() ?: 5.0

            // 현재 장소의 비행시간 정규화 (0~1)
            val normalizedFlight = if (maxFlight != minFlight) (flightVal - minFlight) / (maxFlight - minFlight) else 0.0
            
            // 목표 점수와의 거리를 계산하여 점수화 (가까울수록 높은 점수)
            val flightScore = 1.0 - abs(normalizedFlight - targetFlightValue)

            // 빅맥지수는 공통적으로 낮을수록 높은 점수
            val bigMacScore = if (maxBigMac != minBigMac) 1.0 - (bigMacVal - minBigMac) / (maxBigMac - minBigMac) else 1.0

            val totalScore = (flightScore * WEIGHT_FLIGHT_TIME) + (bigMacScore * WEIGHT_BIG_MAC_INDEX)
            dest to totalScore
        }
        .sortedWith(
            compareBy<Pair<Destination, Double>> { if (it.first.country?.name == userCountry) 1 else 0 }
            .thenByDescending { it.second }
        )
        .take(MAX_RECOMMENDATIONS)
        .map { (dest, _) ->
            val thumbnailUrl = dest.images.find { it.isThumbnail }?.imageUrl ?: ""
            PlaceResponse(
                id = dest.id.toLong(),
                country = dest.country?.name ?: "Unknown",
                city = dest.name,
                summary = dest.summary ?: "",
                thumbnailUrl = thumbnailUrl
            )
        }
    }

    private fun isMonthInRange(month: Int, start: Int?, end: Int?): Boolean {
        if (start == null || end == null) return false
        return if (start <= end) month in start..end else (month >= start || month <= end)
    }

    private fun parseFlightTime(flightTime: String?): Double {
        if (flightTime == null) return 0.0
        return flightTime.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
    }

    private fun findTopVacationPeriods(
        userDayOff: Int,
        holidayMap: Map<LocalDate, PublicHoliday>,
        limit: Int
    ): List<VacationPeriod> {
        val now = LocalDate.now()
        val endSearchDate = now.plusMonths(8) // 오늘부터 8개월 뒤까지로 탐색 범위 단축
        val daysToSearch = ChronoUnit.DAYS.between(now, endSearchDate).toInt() + 1

        val candidates = mutableListOf<VacationPeriod>()

        for (i in 0 until daysToSearch) {
            val currentStart = now.plusDays(i.toLong())
            var usedDayOff = 0
            var currentEnd = currentStart
            val holidayIndicesInPeriod = mutableListOf<PublicHoliday>()

            for (j in i until (i + 31)) {
                if (j >= daysToSearch) break
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

        val sortedCandidates = candidates
            .distinctBy { it.start.toString() + it.end.toString() }
            .sortedWith(compareByDescending<VacationPeriod> { it.totalDays }.thenBy { it.start })

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
        return date.dayOfWeek == DayOfWeek.SATURDAY || 
               date.dayOfWeek == DayOfWeek.SUNDAY || 
               holidayMap.containsKey(date)
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
