package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.domain.SavedRecommendation
import com.bigong.oguri.dto.SaveRecommendationRequest
import com.bigong.oguri.repository.PublicHolidayRepository
import com.bigong.oguri.repository.SavedRecommendationRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
@Transactional
class SavedRecommendationService(
    private val savedRecommendationRepository: SavedRecommendationRepository,
    private val publicHolidayRepository: PublicHolidayRepository,
) {
    /**
     * 연휴 기간 저장
     */
    fun save(
        request: SaveRecommendationRequest,
        memberId: String,
    ) {
        validateDateRange(request.startDate, request.endDate)
        val existing =
            savedRecommendationRepository.findByMemberIdAndStartDateAndEndDate(
                memberId,
                request.startDate,
                request.endDate,
            )
        if (existing != null) return

        val holidayMap = publicHolidayRepository.findAll().associateBy { holiday -> holiday.holidayDate }
        val totalTripCount = ChronoUnit.DAYS.between(request.startDate, request.endDate).toInt() + 1
        val dayOffCount = calculateUsedDayOff(request.startDate, request.endDate, holidayMap)

        val entity =
            SavedRecommendation(
                memberId = memberId,
                startDate = request.startDate,
                endDate = request.endDate,
                dayOffCount = dayOffCount,
                totalTripCount = totalTripCount,
            )
        savedRecommendationRepository.save(entity)
    }

    /**
     * 연휴 기간 삭제 (저장 취소)
     */
    fun delete(
        request: SaveRecommendationRequest,
        memberId: String,
    ) {
        savedRecommendationRepository.deleteByMemberIdAndStartDateAndEndDate(
            memberId,
            request.startDate,
            request.endDate,
        )
    }

    private fun validateDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ) {
        if (endDate.isBefore(startDate)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다.")
        }
    }

    private fun calculateUsedDayOff(
        startDate: LocalDate,
        endDate: LocalDate,
        holidayMap: Map<LocalDate, PublicHoliday>,
    ): Int {
        var usedDayOffCount = 0
        var date = startDate
        while (!date.isAfter(endDate)) {
            val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            val isPublicHoliday = holidayMap.containsKey(date)
            if (!isWeekend && !isPublicHoliday) {
                usedDayOffCount++
            }
            date = date.plusDays(1)
        }
        return usedDayOffCount
    }
}
