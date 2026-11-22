package com.bigong.oguri.core.navigation

import com.bigong.oguri.domain.model.TravelPlanSource
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TravelSourceRouteTest {
    private val start = LocalDate(2027, 4, 1)
    private val end = LocalDate(2027, 4, 5)

    @Test
    fun citySourceKeepsItsOriginalRecommendationDates() {
        val source = TravelPlanSource(12, start, end)
        assertEquals(RouteModel.PlaceDetail(12, "2027-04-01", "2027-04-05"), source.toRecommendationRoute())
    }

    @Test
    fun cityWithoutRecommendationDatesDoesNotInventAPeriod() {
        assertEquals(RouteModel.PlaceDetail(12, null, null), TravelPlanSource(12, null, null).toRecommendationRoute())
    }

    @Test
    fun periodSourceOpensTheOriginalPeriod() {
        assertEquals(RouteModel.PeriodDetail("2027-04-01", "2027-04-05"), TravelPlanSource(null, start, end).toRecommendationRoute())
    }

    @Test
    fun invalidOrMissingSourceCannotOpenAnUnrelatedRecommendation() {
        assertNull(TravelPlanSource(null, null, null).toRecommendationRoute())
        assertNull(TravelPlanSource(null, end, start).toRecommendationRoute())
        assertNull(TravelPlanSource(12, start, null).toRecommendationRoute())
        assertNull(TravelPlanSource(-1, start, end).toRecommendationRoute())
    }
}
