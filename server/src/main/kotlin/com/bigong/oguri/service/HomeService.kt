package com.bigong.oguri.service

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

@Service
@Transactional(readOnly = true)
class HomeService(
    private val destinationRepository: DestinationRepository,
    private val destinationImageRepository: DestinationImageRepository,
    private val publicHolidayRepository: PublicHolidayRepository
) {
    fun getHomeData(dayOffCount: Int): List<RecommendPeriodResponse> {
        val holidayList = publicHolidayRepository.findAll()
        val holidayMap = holidayList.associateBy { it.holidayDate }

        val bestPeriods = findTopVacationPeriods(dayOffCount, holidayMap, limit = 3)

        val destinations = destinationRepository.findAll()
        val places = destinations.map { destination ->
            val thumbnail = destinationImageRepository.findByDestinationIdAndIsThumbnailTrue(destination.id)
            PlaceResponse(
                id = destination.id.toLong(),
                country = destination.country?.name ?: "Unknown",
                city = destination.name,
                summary = destination.summary ?: "",
                thumbnailUrl = thumbnail?.imageUrl ?: ""
            )
        }

        val advertisements = listOf(
            AdvertisementResponse(platform = "google", url = "https://www.google.com"),
            AdvertisementResponse(platform = "naver", url = "https://www.naver.com")
        )

        return bestPeriods.mapIndexed { index, period ->
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
                places = places,
                advertisements = advertisements
            )
        }
    }

    private fun findTopVacationPeriods(
        userDayOff: Int,
        holidayMap: Map<LocalDate, PublicHoliday>,
        limit: Int
    ): List<VacationPeriod> {
        val now = LocalDate.now()
        val endSearchDate = now.plusYears(1)
        val daysToSearch = ChronoUnit.DAYS.between(now, endSearchDate).toInt() + 1

        val candidates = mutableListOf<VacationPeriod>()

        // 1. 모든 가능한 구간 수집
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

        // 2. 정렬 (일수 큰 순 -> 날짜 빠른 순)
        val sortedCandidates = candidates
            .distinctBy { it.start.toString() + it.end.toString() }
            .sortedWith(compareByDescending<VacationPeriod> { it.totalDays }.thenBy { it.start })

        // 3. 중복 구간(Overlap) 필터링
        val selected = mutableListOf<VacationPeriod>()
        for (candidate in sortedCandidates) {
            if (selected.size >= limit) break
            
            // 이미 선택된 기간들과 겹치지 않는지 확인
            val isOverlapping = selected.any { it.overlapsWith(candidate) }
            if (!isOverlapping) {
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
        // 두 기간이 겹치는지 확인하는 함수
        fun overlapsWith(other: VacationPeriod): Boolean {
            return !this.end.isBefore(other.start) && !other.end.isBefore(this.start)
        }
    }
}
