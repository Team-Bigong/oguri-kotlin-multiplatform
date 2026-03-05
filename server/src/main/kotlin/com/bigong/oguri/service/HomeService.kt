package com.bigong.oguri.service

import com.bigong.oguri.domain.Destination
import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.AdvertisementResponse
import com.bigong.oguri.dto.PlaceResponse
import com.bigong.oguri.dto.RecommendPeriodResponse
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.PublicHolidayRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 성능 최적화된 홈 화면 서비스
 */
@Service
@Transactional(readOnly = true)
class HomeService(
    private val destinationRepository: DestinationRepository,
    private val publicHolidayRepository: PublicHolidayRepository
) {
    companion object {
        const val WEIGHT_FLIGHT_TIME = 0.6    // 비행시간 가중치 (60%)
        const val WEIGHT_BIG_MAC_INDEX = 0.4  // 빅맥지수 가중치 (40%)
        const val MAX_RECOMMENDATIONS = 7     // 최대 추천 장소 개수
    }

    /**
     * 홈 화면 데이터 조회 (성능 최적화 버전)
     */
    fun getHomeData(dayOffCount: Int, userCountry: String): List<RecommendPeriodResponse> {
        // 1. 모든 공휴일 정보 로드
        val holidayMap = publicHolidayRepository.findAll().associateBy { it.holidayDate }

        // 2. 모든 여행지, 국가, 이미지를 단 하나의 쿼리로 로드 (N+1 문제 해결)
        val allDestinations = destinationRepository.findAllWithCountryAndImages()

        // 3. 최적의 연차 구간 탐색
        val bestPeriods = findTopVacationPeriods(dayOffCount, holidayMap, limit = 3)

        val advertisements = listOf(
            AdvertisementResponse(platform = "google", url = "https://www.google.com"),
            AdvertisementResponse(platform = "naver", url = "https://www.naver.com")
        )

        return bestPeriods.mapIndexed { index, period ->
            val recommendedPlaces = calculateRecommendedPlaces(period.start, allDestinations, userCountry)

            val holidayNames = period.holidayObjects
                .filter { it.isActualHoliday }
                .map { it.name }
                .distinct()

            RecommendPeriodResponse(
                rank = index + 1,
                isSaved = false,
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
     * 추천 장소 계산 (N+1 문제 해결 버전)
     */
    private fun calculateRecommendedPlaces(
        startDate: LocalDate,
        destinations: List<Destination>,
        userCountry: String
    ): List<PlaceResponse> {
        val targetMonth = startDate.monthValue

        // 1. 시기 필터링
        val candidates = destinations.filter { dest ->
            isMonthInRange(targetMonth, dest.recommendStartMonth1, dest.recommendEndMonth1) ||
            isMonthInRange(targetMonth, dest.recommendStartMonth2, dest.recommendEndMonth2)
        }

        if (candidates.isEmpty()) return emptyList()

        // 2. 정규화 준비 (미리 계산)
        val flightTimes = candidates.map { parseFlightTime(it.flightTime) }
        val bigMacIndices = candidates.map { it.country?.bigMacIndex?.toDouble() ?: 5.0 }

        val minFlight = flightTimes.minOrNull() ?: 0.0
        val maxFlight = flightTimes.maxOrNull() ?: 1.0
        val minBigMac = bigMacIndices.minOrNull() ?: 0.0
        val maxBigMac = bigMacIndices.maxOrNull() ?: 1.0

        // 3. 점수 계산 및 정렬
        return candidates.map { dest ->
            val flightVal = parseFlightTime(dest.flightTime)
            val bigMacVal = dest.country?.bigMacIndex?.toDouble() ?: 5.0

            val flightScore = if (maxFlight != minFlight) 1.0 - (flightVal - minFlight) / (maxFlight - minFlight) else 1.0
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
            // [최적화 3] DB를 다시 찌르지 않고 이미 로드된 images 리스트에서 썸네일을 찾음
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

    /**
     * 탐색 알고리즘 최적화
     */
    private fun findTopVacationPeriods(
        userDayOff: Int,
        holidayMap: Map<LocalDate, PublicHoliday>,
        limit: Int
    ): List<VacationPeriod> {
        val now = LocalDate.now()
        val endSearchDate = now.plusYears(1)
        val daysToSearch = ChronoUnit.DAYS.between(now, endSearchDate).toInt() + 1

        val candidates = mutableListOf<VacationPeriod>()

        // [최적화 4] 이중 루프 내 불필요한 객체 생성을 최소화
        for (i in 0 until daysToSearch) {
            val currentStart = now.plusDays(i.toLong())
            var usedDayOff = 0
            var currentEnd = currentStart
            val holidayIndicesInPeriod = mutableListOf<PublicHoliday>()

            for (j in i until (i + 31)) { // 한 번의 휴가가 31일을 넘지 않는다고 가정하여 탐색 범위 축소
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
