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
    fun `recommendation includes previous contiguous public holiday in same vacation period`() {
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

        val recommendedPeriods =
            vacationRecommendationService.findRecommendedPeriods(
                startYearMonth = YearMonth.of(2026, 5),
                userDayOff = 1,
                holidayMap = holidayMap,
                monthRangeCount = 1,
                periodLimitPerMonth = 100,
            )

        val targetPeriod =
            recommendedPeriods.firstOrNull { period ->
                period.start == laborDay && period.end == childrenDay
            }

        assertNotNull(targetPeriod)
        assertEquals(1, targetPeriod?.usedDayOffCount)
        assertTrue(targetPeriod?.holidayNames?.contains("노동절") == true)
        assertTrue(
            targetPeriod?.holidayDateDetails?.any { holidayDate ->
                holidayDate.date == laborDay &&
                    holidayDate.label == "노동절" &&
                    holidayDate.publicHoliday
            } == true,
        )
        assertTrue(
            recommendedPeriods.none { period ->
                period.start == LocalDate.of(2026, 5, 2) && period.end == childrenDay
            },
        )
    }
}
