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
    fun getHomeData(dayOffCount: Int): RecommendPeriodResponse {
        // 1. DB에서 공휴일 목록 가져오기
        val holidayList = publicHolidayRepository.findAll()
        val holidayMap = holidayList.associateBy { it.holidayDate }

        // 2. 최적의 휴가 기간 계산 (현재 날짜 이후부터 최대 1년치 탐색)
        val bestPeriod = findBestVacation(dayOffCount, holidayMap)

        // 3. DB에서 여행지 조회
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

        // 4. 광고 더미 데이터
        val advertisements = listOf(
            AdvertisementResponse(platform = "google", url = "https://www.google.com"),
            AdvertisementResponse(platform = "naver", url = "https://www.naver.com")
        )

        return RecommendPeriodResponse(
            rank = 1,
            isSaved = false,
            startDate = bestPeriod.start,
            endDate = bestPeriod.end,
            holiday = bestPeriod.holidays,
            dayOffCount = dayOffCount,
            totalTripCount = bestPeriod.totalDays,
            places = places,
            advertisements = advertisements
        )
    }

    private fun findBestVacation(userDayOff: Int, holidayMap: Map<LocalDate, PublicHoliday>): VacationPeriod {
        val now = LocalDate.now()
        val endSearchDate = now.plusYears(1) // 오늘부터 1년 뒤까지 동적으로 탐색

        val daysToSearch = ChronoUnit.DAYS.between(now, endSearchDate).toInt() + 1

        var maxDays = 0
        var bestStart = now
        var bestEnd = now
        var bestHolidayObjects = listOf<PublicHoliday>()

        for (i in 0 until daysToSearch) {
            val currentStart = now.plusDays(i.toLong())
            var usedDayOff = 0
            var currentEnd = currentStart
            val currentHolidayObjects = mutableListOf<PublicHoliday>()

            for (j in i until daysToSearch) {
                val date = now.plusDays(j.toLong())
                
                if (!isOffDay(date, holidayMap)) {
                    if (usedDayOff < userDayOff) {
                        usedDayOff++
                    } else {
                        break
                    }
                } else {
                    holidayMap[date]?.let { currentHolidayObjects.add(it) }
                }

                currentEnd = date
                val totalDays = ChronoUnit.DAYS.between(currentStart, currentEnd).toInt() + 1
                
                if (totalDays > maxDays) {
                    maxDays = totalDays
                    bestStart = currentStart
                    bestEnd = currentEnd
                    bestHolidayObjects = currentHolidayObjects.toList()
                }
            }
        }

        // 실제 공휴일 당일인 경우만 추출하여 노출 (연휴, 대체공휴일 등은 제외)
        val holidayNames = bestHolidayObjects
            .filter { it.isActualHoliday }
            .map { it.name }
            .distinct()

        // 공휴일 당일 정보가 없으면 "주말"로 표시
        val finalHolidays = if (holidayNames.isEmpty() && maxDays > 0) listOf("주말") else holidayNames

        return VacationPeriod(bestStart, bestEnd, maxDays, finalHolidays)
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
        val holidays: List<String>
    )
}
