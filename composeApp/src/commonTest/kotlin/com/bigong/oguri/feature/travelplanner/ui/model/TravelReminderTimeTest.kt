package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelReminderSettings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

class TravelReminderTimeTest {
    private val settings =
        TravelReminderSettings(
            dates = TravelDates(LocalDate(2027, 4, 1), LocalDate(2027, 4, 3)),
            tripTimeZone = TimeZone.of("Asia/Seoul"),
            canSchedule = true,
            reminder = null,
        )
    private val now = Instant.parse("2027-03-01T00:00:00Z")

    @Test
    fun reminderUsesTheChosenReceivingTimeZone() {
        val time = assertNotNull(parseTravelReminderTime("2027-03-30", "11:30", "America/Los_Angeles", settings, now))
        assertEquals(Instant.parse("2027-03-30T18:30:00Z"), time.localDateTime.toInstant(time.timeZone))
    }

    @Test
    fun departureBoundaryUsesTheTripTimeZone() {
        assertNull(parseTravelReminderTime("2027-03-31", "08:00", "America/Los_Angeles", settings, now))
        assertNotNull(parseTravelReminderTime("2027-03-31", "07:59", "America/Los_Angeles", settings, now))
    }

    @Test
    fun pastAndCurrentTimeCannotBeScheduled() {
        assertNull(parseTravelReminderTime("2027-02-28", "23:59", "UTC", settings, now))
        assertNull(parseTravelReminderTime("2027-03-01", "00:00", "UTC", settings, now))
    }

    @Test
    fun invalidDateTimeAndTimeZoneKeepTheFormUnsavable() {
        assertNull(parseTravelReminderTime("2027-02-29", "12:00", "UTC", settings, now))
        assertNull(parseTravelReminderTime("2027-03-30", "25:00", "UTC", settings, now))
        assertNull(parseTravelReminderTime("2027-03-30", "12:00", "Unknown/Zone", settings, now))
    }

    @Test
    fun daylightSavingGapCannotSilentlyMoveTheReminder() {
        assertNull(parseTravelReminderTime("2027-03-14", "02:30", "America/New_York", settings, now))
        assertNotNull(parseTravelReminderTime("2027-03-14", "03:30", "America/New_York", settings, now))
    }
}
