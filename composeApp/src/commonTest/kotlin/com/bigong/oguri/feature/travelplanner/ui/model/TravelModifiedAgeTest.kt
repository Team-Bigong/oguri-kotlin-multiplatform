package com.bigong.oguri.feature.travelplanner.ui.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class TravelModifiedAgeTest {
    private val now = Instant.parse("2027-04-01T10:00:00Z")

    @Test
    fun futureServerTimeDoesNotProduceANegativeAge() {
        assertEquals(TravelModifiedAge(TravelModifiedUnit.JUST_NOW), travelModifiedAge(now + 1.minutes, now))
    }

    @Test
    fun minuteAndHourBoundariesUseWholeUnits() {
        assertEquals(TravelModifiedAge(TravelModifiedUnit.MINUTES, 59), travelModifiedAge(now - 59.minutes, now))
        assertEquals(TravelModifiedAge(TravelModifiedUnit.HOURS, 1), travelModifiedAge(now - 60.minutes, now))
        assertEquals(TravelModifiedAge(TravelModifiedUnit.HOURS, 23), travelModifiedAge(now - 23.hours, now))
    }

    @Test
    fun olderChangesUseDays() {
        assertEquals(TravelModifiedAge(TravelModifiedUnit.DAYS, 1), travelModifiedAge(now - 24.hours, now))
        assertEquals(TravelModifiedAge(TravelModifiedUnit.DAYS, 7), travelModifiedAge(now - 7.days, now))
    }
}
