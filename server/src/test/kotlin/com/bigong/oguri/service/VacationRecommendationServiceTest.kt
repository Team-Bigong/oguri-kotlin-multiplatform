package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class VacationRecommendationServiceTest {
    private val vacationRecommendationService = VacationRecommendationService()

    @Test
    fun `연속된 이전 공휴일을 같은 연휴 기간에 포함한다`() {
        // given
        val laborDay = LocalDate.of(2026, 5, 1)
        val childrenDay = LocalDate.of(2026, 5, 5)
        val holidayMap =
            listOf(
                PublicHoliday(
                    holidayDate = laborDay,
                    name = "노동절",
                    isActualHoliday = true,
                ),
                PublicHoliday(
                    holidayDate = childrenDay,
                    name = "어린이날",
                    isActualHoliday = true,
                ),
            ).associateBy { holiday -> holiday.holidayDate }

        // when
        val recommendedPeriods =
            vacationRecommendationService.findRecommendedPeriods(
                startYearMonth = YearMonth.of(2026, 5),
                userDayOff = 1,
                holidayMap = holidayMap,
                monthRangeCount = 1,
                periodLimitPerMonth = 100,
            )

        // then
        val targetPeriod =
            recommendedPeriods.firstOrNull { period ->
                period.start.isEqual(laborDay) && period.end.isEqual(childrenDay)
            }

        assertNotNull(targetPeriod)
        assertEquals(1, targetPeriod?.usedDayOffCount)
        assertTrue(targetPeriod?.holidayNames?.contains("노동절") == true)
        assertTrue(
            targetPeriod?.holidayDateDetails?.any { holidayDate ->
                holidayDate.date.isEqual(laborDay) &&
                    holidayDate.label == "노동절" &&
                    holidayDate.publicHoliday
            } == true,
        )
        assertTrue(
            recommendedPeriods.none { period ->
                period.start.isEqual(LocalDate.of(2026, 5, 2)) && period.end.isEqual(childrenDay)
            },
        )
    }

    @Test
    fun `월별 추천은 이전 달에 시작하는 더 긴 연휴를 포함한다`() {
        // given
        val previousMonthLeaveDate = LocalDate.of(2026, 4, 30)
        val laborDay = LocalDate.of(2026, 5, 1)
        val childrenDay = LocalDate.of(2026, 5, 5)
        val holidayMap =
            listOf(
                PublicHoliday(
                    holidayDate = laborDay,
                    name = "노동절",
                    isActualHoliday = true,
                ),
                PublicHoliday(
                    holidayDate = childrenDay,
                    name = "어린이날",
                    isActualHoliday = true,
                ),
            ).associateBy { holiday -> holiday.holidayDate }

        // when
        val recommendedPeriods =
            vacationRecommendationService.findRecommendedPeriods(
                startYearMonth = YearMonth.of(2026, 5),
                userDayOff = 2,
                holidayMap = holidayMap,
                monthRangeCount = 1,
                periodLimitPerMonth = 100,
            )

        // then
        val targetPeriod =
            recommendedPeriods.firstOrNull { period ->
                period.start.isEqual(previousMonthLeaveDate) && period.end.isEqual(childrenDay)
            }

        assertNotNull(targetPeriod)
        assertEquals(2, targetPeriod?.usedDayOffCount)
        assertEquals(6, targetPeriod?.totalDays)
        assertTrue(targetPeriod?.holidayNames?.contains("노동절") == true)
        assertTrue(targetPeriod?.holidayNames?.contains("어린이날") == true)
    }

    @Test
    fun `월별 추천은 다음 달로 이어지는 연휴를 포함한다`() {
        // given
        val leaveDate = LocalDate.of(2026, 5, 29)
        val nextMonthHoliday = LocalDate.of(2026, 6, 1)
        val holidayMap =
            listOf(
                PublicHoliday(
                    holidayDate = nextMonthHoliday,
                    name = "다음달 공휴일",
                    isActualHoliday = true,
                ),
            ).associateBy { holiday -> holiday.holidayDate }

        // when
        val recommendedPeriods =
            vacationRecommendationService.findRecommendedPeriods(
                startYearMonth = YearMonth.of(2026, 5),
                userDayOff = 1,
                holidayMap = holidayMap,
                monthRangeCount = 1,
                periodLimitPerMonth = 100,
            )

        // then
        val targetPeriod =
            recommendedPeriods.firstOrNull { period ->
                period.start.isEqual(leaveDate) && period.end.isEqual(nextMonthHoliday)
            }

        assertNotNull(targetPeriod)
        assertEquals(1, targetPeriod?.usedDayOffCount)
        assertEquals(4, targetPeriod?.totalDays)
        assertTrue(targetPeriod?.holidayNames?.contains("다음달 공휴일") == true)
    }
}
