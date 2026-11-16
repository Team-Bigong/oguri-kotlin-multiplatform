package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelDates
import kotlinx.datetime.LocalDate
import kotlin.test.*

class TravelDateSelectionTest {
    @Test
    fun selectingTheSameDayTwiceCreatesASingleDayTrip() {
        val date = LocalDate(2026, 12, 22)
        assertEquals(TravelDates(date, date), TravelDateSelection().select(date).select(date).dates)
    }

    @Test
    fun anEarlierSecondDateRestartsTheRange() {
        val earlier = LocalDate(2026, 12, 19)
        assertEquals(TravelDateSelection(earlier), TravelDateSelection(LocalDate(2026, 12, 22)).select(earlier))
    }

    @Test
    fun completedRangesCanBeReplacedAndCanCrossAYearBoundary() {
        val start = LocalDate(2026, 12, 29)
        val end = LocalDate(2027, 1, 3)
        val selection = TravelDateSelection().select(start).select(end)
        assertEquals(6, selection.dates?.dayCount)
        assertEquals(TravelDateSelection(end), selection.select(end))
    }

    @Test
    fun aRangeThatExceedsTheTripLimitCannotBeApplied() {
        val selected = TravelDateSelection(LocalDate(2026, 1, 1), LocalDate(2027, 1, 2))
        assertNull(selected.dates)
        assertNotNull(selected.select(LocalDate(2026, 12, 20)).select(LocalDate(2026, 12, 21)).dates)
    }
}
