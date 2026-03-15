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
 * 홈 화면 서비스
 */
@Service
@Transactional(readOnly = true)
class HomeService(
    private val destinationRepository: DestinationRepository,
    private val publicHolidayRepository: PublicHolidayRepository,
    private val savedRecommendationRepository: SavedRecommendationRepository,
    private val memberService: MemberService
) {
    private var cachedHolidays: Map<LocalDate, PublicHoliday> = emptyMap()
    private var lastHolidayUpdate: LocalDate? = null

    companion object {
        const val WEIGHT_FLIGHT_TIME = 0.6
        const val WEIGHT_BIG_MAC_INDEX = 0.4
        const val MAX_RECOMMENDATIONS = 7
        const val MID_TRIP_THRESHOLD = 5
        const val LONG_TRIP_THRESHOLD = 7
        private val NUMBER_ONLY_REGEX = Regex("[^0-9]")
    }

    private fun refreshHolidayCacheIfNeeded() {
        val today = LocalDate.now()
        if (lastHolidayUpdate != today || cachedHolidays.isEmpty()) {
            cachedHolidays = publicHolidayRepository.findAll().associateBy { it.holidayDate }
            lastHolidayUpdate = today
        }
    }

    fun getHomeData(userCountry: String, memberId: String): List<RecommendPeriodResponse> {
        refreshHolidayCacheIfNeeded()
        val memberInfo = memberService.getDayOffInfo(memberId)
        val preferredDayOff = memberInfo.preferredDayOff
        val remainingDayOff = memberInfo.remainingDayOff
        val savedPeriods = savedRecommendationRepository.findAllByMemberId(memberId)
        val allDestinations = destinationRepository.findAllWithCountryAndImages()

        val bestPeriods = findTopVacationPeriods(preferredDayOff, cachedHolidays, limit = 3)

        val advertisements = listOf(
            AdvertisementResponse(platform = "agoda", url = "https://www.agoda.com"),
            AdvertisementResponse(platform = "skyscanner", url = "https://www.skyscanner.com/"),
            AdvertisementResponse(platform = "klook", url = "https://www.klook.com/")
        )

        return bestPeriods.mapIndexed { index, period ->
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
     * 추천 장소 계산 로직 (상세 화면에서도 재사용 가능하도록 공개)
     */
    fun calculateRecommendedPlaces(
        startDate: LocalDate,
        destinations: List<Destination>,
        userCountry: String,
        totalTripCount: Int
    ): List<PlaceResponse> {
        val targetMonth = startDate.monthValue
        val candidates = destinations.filter { dest ->
            isMonthInRange(targetMonth, dest.recommendStartMonth1, dest.recommendEndMonth1) ||
            isMonthInRange(targetMonth, dest.recommendStartMonth2, dest.recommendEndMonth2)
        }
        if (candidates.isEmpty()) return emptyList()

        val flightTimes = candidates.map { parseFlightTime(it.flightTime) }
        val bigMacIndices = candidates.map { it.country?.bigMacIndex?.toDouble() ?: 5.0 }
        val minFlight = flightTimes.minOrNull() ?: 0.0
        val maxFlight = flightTimes.maxOrNull() ?: 1.0
        val minBigMac = bigMacIndices.minOrNull() ?: 0.0
        val maxBigMac = bigMacIndices.maxOrNull() ?: 1.0

        val targetFlightValue = when {
            totalTripCount >= LONG_TRIP_THRESHOLD -> 1.0
            totalTripCount >= MID_TRIP_THRESHOLD  -> 0.35
            else -> 0.0
        }

        return candidates.map { dest ->
            val flightVal = parseFlightTime(dest.flightTime)
            val bigMacVal = dest.country?.bigMacIndex?.toDouble() ?: 5.0
            val normalizedFlight = if (maxFlight != minFlight) (flightVal - minFlight) / (maxFlight - minFlight) else 0.0
            val flightScore = 1.0 - abs(normalizedFlight - targetFlightValue)
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
        return flightTime.replace(NUMBER_ONLY_REGEX, "").toDoubleOrNull() ?: 0.0
    }

    private fun findTopVacationPeriods(
        userDayOff: Int,
        holidayMap: Map<LocalDate, PublicHoliday>,
        limit: Int
    ): List<VacationPeriod> {
        val now = LocalDate.now()
        val endSearchDate = now.plusMonths(8)
        val daysToSearch = ChronoUnit.DAYS.between(now, endSearchDate).toInt() + 1

        val candidates = mutableListOf<VacationPeriod>()

        // [최적화] 탐색 범위를 연차 개수에 따라 유동적으로 설정 (연차 + 주말/공휴일 버퍼)
        // 최대 연차 30일 기준, 약 70일 정도의 탐색 창이면 충분함
        val searchWindow = userDayOff * 2 + 10

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

        val sortedCandidates = candidates
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
