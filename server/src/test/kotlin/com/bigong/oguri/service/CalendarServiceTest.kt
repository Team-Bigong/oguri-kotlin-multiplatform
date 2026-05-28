package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.PublicHolidayRepository
import com.bigong.oguri.repository.SavedRecommendationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate

class CalendarServiceTest {
    private val memberService: MemberService = mock(MemberService::class.java)
    private val publicHolidayRepository: PublicHolidayRepository = mock(PublicHolidayRepository::class.java)
    private val savedRecommendationRepository: SavedRecommendationRepository = mock(SavedRecommendationRepository::class.java)
    private val destinationRepository: DestinationRepository = mock(DestinationRepository::class.java)
    private val placeRecommendationService: PlaceRecommendationService = mock(PlaceRecommendationService::class.java)
    private val vacationRecommendationService = VacationRecommendationService()
    private val calendarService =
        CalendarService(
            memberService = memberService,
            publicHolidayRepository = publicHolidayRepository,
            savedRecommendationRepository = savedRecommendationRepository,
            destinationRepository = destinationRepository,
            placeRecommendationService = placeRecommendationService,
            vacationRecommendationService = vacationRecommendationService,
        )

    @Test
    fun `월 필터는 페이지 크기만큼 후보를 확보해 중순 이후 일정도 반환한다`() {
        // given
        `when`(publicHolidayRepository.findAll()).thenReturn(
            listOf(
                PublicHoliday(
                    holidayDate = LocalDate.of(2026, 6, 6),
                    name = "현충일",
                    isActualHoliday = true,
                ),
            ),
        )
        `when`(savedRecommendationRepository.findAllByMemberId(MEMBER_ID)).thenReturn(emptyList())

        // when
        val response =
            calendarService.getCalendarData(
                year = 2026,
                month = 6,
                memberId = MEMBER_ID,
                dayOffCount = 2,
                page = 0,
                size = 10,
            )

        // then
        assertEquals(10, response.periods.size)
        assertTrue(response.hasNext)
        assertTrue(
            response.periods.any { period ->
                period.startDate.isAfter(LocalDate.of(2026, 6, 14))
            },
        )
    }

    private companion object {
        private const val MEMBER_ID = "GUEST"
    }
}
