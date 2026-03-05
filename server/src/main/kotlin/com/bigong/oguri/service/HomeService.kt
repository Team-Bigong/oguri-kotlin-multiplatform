package com.bigong.oguri.service

import com.bigong.oguri.domain.Destination
import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.AdvertisementResponse
import com.bigong.oguri.dto.PlaceResponse
import com.bigong.oguri.dto.RecommendPeriodResponse
import com.bigong.oguri.repository.DestinationImageRepository
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.PublicHolidayRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 홈 화면 데이터 제공을 위한 서비스 클래스
 */
@Service
@Transactional(readOnly = true)
class HomeService(
    private val destinationRepository: DestinationRepository,
    private val destinationImageRepository: DestinationImageRepository,
    private val publicHolidayRepository: PublicHolidayRepository
) {
    companion object {
        const val WEIGHT_FLIGHT_TIME = 0.6    // 비행시간 가중치 (60%)
        const val WEIGHT_BIG_MAC_INDEX = 0.4  // 빅맥지수 가중치 (40%)
        const val MAX_RECOMMENDATIONS = 7     // 최대 추천 장소 개수
    }

    /**
     * 사용자의 연차 개수와 거주 국가에 따른 맞춤 홈 데이터 조회
     */
    fun getHomeData(dayOffCount: Int, userCountry: String): List<RecommendPeriodResponse> {
        // 1. 모든 공휴일 정보 로드 및 날짜별 매핑
        val holidayMap = publicHolidayRepository.findAll().associateBy { it.holidayDate }

        // 2. 최적의 연차 사용 기간 Top 3 탐색 (오늘부터 1년치 전수 조사)
        val bestPeriods = findTopVacationPeriods(dayOffCount, holidayMap, limit = 3)

        // 3. 모든 여행지 정보 로드 (추천 계산용)
        val allDestinations = destinationRepository.findAll()

        // 4. 광고 데이터 (현재 더미 데이터, 추후 DB화 가능)
        val advertisements = listOf(
            AdvertisementResponse(platform = "google", url = "https://www.google.com"),
            AdvertisementResponse(platform = "naver", url = "https://www.naver.com")
        )

        // 5. 각 추천 기간별로 최적의 여행지 목록을 조립하여 응답 생성
        return bestPeriods.mapIndexed { index, period ->
            // 해당 휴가 기간(시작일 기준)에 날씨/분위기가 가장 좋은 장소 계산
            val recommendedPlaces = calculateRecommendedPlaces(period.start, allDestinations, userCountry)

            // 휴가 기간 내에 포함된 공휴일 명칭 추출 (중복 제거)
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
     * 정규화 점수 기반 추천 장소 계산 로직
     */
    private fun calculateRecommendedPlaces(
        startDate: LocalDate,
        destinations: List<Destination>,
        userCountry: String
    ): List<PlaceResponse> {
        val targetMonth = startDate.monthValue

        // [단계 1] 추천 시기 필터링: 여행 시작 월이 장소의 추천 시즌(1차 또는 2차)에 포함되는지 확인
        val candidates = destinations.filter { dest ->
            isMonthInRange(targetMonth, dest.recommendStartMonth1, dest.recommendEndMonth1) ||
            isMonthInRange(targetMonth, dest.recommendStartMonth2, dest.recommendEndMonth2)
        }

        if (candidates.isEmpty()) return emptyList()

        // [단계 2] 정규화(Normalization) 준비: 후보군 내 비행시간과 빅맥지수의 최소/최대값을 구함
        val flightTimes = candidates.map { parseFlightTime(it.flightTime) }
        val bigMacIndices = candidates.map { it.country?.bigMacIndex?.toDouble() ?: 5.0 }

        val minFlight = flightTimes.minOrNull() ?: 0.0
        val maxFlight = flightTimes.maxOrNull() ?: 1.0
        val minBigMac = bigMacIndices.minOrNull() ?: 0.0
        val maxBigMac = bigMacIndices.maxOrNull() ?: 1.0

        // [단계 3] 점수 계산 및 가중치 적용
        return candidates.map { dest ->
            val flightVal = parseFlightTime(dest.flightTime)
            val bigMacVal = dest.country?.bigMacIndex?.toDouble() ?: 5.0

            // 0~1 사이 점수로 변환 (값이 낮을수록 좋은 지표이므로 1에서 뺌)
            val flightScore = if (maxFlight != minFlight) 1.0 - (flightVal - minFlight) / (maxFlight - minFlight) else 1.0
            val bigMacScore = if (maxBigMac != minBigMac) 1.0 - (bigMacVal - minBigMac) / (maxBigMac - minBigMac) else 1.0

            // 가중치 합산 (비행시간 60% + 빅맥지수 40%)
            val totalScore = (flightScore * WEIGHT_FLIGHT_TIME) + (bigMacScore * WEIGHT_BIG_MAC_INDEX)

            dest to totalScore
        }
        // [단계 4] 정렬: 1순위 - 거주 국가 여부(해외 우선), 2순위 - 추천 점수 높은 순
        .sortedWith(
            compareBy<Pair<Destination, Double>> { if (it.first.country?.name == userCountry) 1 else 0 }
            .thenByDescending { it.second }
        )
        .take(MAX_RECOMMENDATIONS)
        .map { (dest, _) ->
            val thumbnail = destinationImageRepository.findByDestinationIdAndIsThumbnailTrue(dest.id)
            PlaceResponse(
                id = dest.id.toLong(),
                country = dest.country?.name ?: "Unknown",
                city = dest.name,
                summary = dest.summary ?: "",
                thumbnailUrl = thumbnail?.imageUrl ?: ""
            )
        }
    }

    /**
     * 추천 월 범위 체크 (연도 걸침 로직 포함)
     */
    private fun isMonthInRange(month: Int, start: Int?, end: Int?): Boolean {
        if (start == null || end == null) return false
        return if (start <= end) {
            month in start..end
        } else {
            // 예: 12월 ~ 2월 같은 경우
            month !in (end + 1)..<start
        }
    }

    /**
     * 비행시간 문자열에서 숫자만 추출 (예: "95분" -> 95.0)
     */
    private fun parseFlightTime(flightTime: String?): Double {
        if (flightTime == null) return 0.0
        return flightTime.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
    }

    /**
     * 현재부터 1년 내에서 가장 길게 쉴 수 있는 겹치지 않는 구간 Top 3 탐색
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

        // 1. 모든 시작 날짜별로 가능한 최대 휴가 구간 수집 (슬라이딩 윈도우)
        for (i in 0 until daysToSearch) {
            val currentStart = now.plusDays(i.toLong())
            var usedDayOff = 0
            var currentEnd = currentStart
            val currentHolidayObjects = mutableListOf<PublicHoliday>()

            for (j in i until daysToSearch) {
                val date = now.plusDays(j.toLong())
                if (!isOffDay(date, holidayMap)) {
                    if (usedDayOff < userDayOff) usedDayOff++ else break
                } else {
                    holidayMap[date]?.let { currentHolidayObjects.add(it) }
                }
                currentEnd = date
            }

            val totalDays = ChronoUnit.DAYS.between(currentStart, currentEnd).toInt() + 1
            if (totalDays > 0) {
                candidates.add(VacationPeriod(currentStart, currentEnd, totalDays, currentHolidayObjects.toList()))
            }
        }

        // 2. 일수 내림차순, 날짜 오름차순으로 정렬
        val sortedCandidates = candidates
            .distinctBy { it.start.toString() + it.end.toString() }
            .sortedWith(compareByDescending<VacationPeriod> { it.totalDays }.thenBy { it.start })

        // 3. 중복 구간 필터링 (선택된 구간과 겹치지 않는 다음 구간 탐색)
        val selected = mutableListOf<VacationPeriod>()
        for (candidate in sortedCandidates) {
            if (selected.size >= limit) break
            if (selected.none { it.overlapsWith(candidate) }) {
                selected.add(candidate)
            }
        }

        return selected
    }

    /**
     * 해당 날짜가 쉬는 날(주말 또는 공휴일)인지 판별
     */
    private fun isOffDay(date: LocalDate, holidayMap: Map<LocalDate, PublicHoliday>): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY ||
               date.dayOfWeek == DayOfWeek.SUNDAY ||
               holidayMap.containsKey(date)
    }

    /**
     * 휴가 기간 데이터 클래스
     */
    data class VacationPeriod(
        val start: LocalDate,
        val end: LocalDate,
        val totalDays: Int,
        val holidayObjects: List<PublicHoliday>
    ) {
        /**
         * 두 기간이 겹치는지 여부 확인
         */
        fun overlapsWith(other: VacationPeriod): Boolean {
            return !this.end.isBefore(other.start) && !other.end.isBefore(this.start)
        }
    }
}
