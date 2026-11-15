package com.bigong.oguri.feature.travelplanner.ui

import com.bigong.oguri.feature.travelplanner.ui.component.findTravelNoteLinks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TravelNoteLinksTest {
    @Test
    fun keepsLinkRangesSeparateFromNoteTextAndPunctuation() {
        val note = "예약(https://example.com/a(b)?date=12#menu).\n지도 www.example.org/map, 확인"

        val links = findTravelNoteLinks(note)

        assertEquals(listOf("https://example.com/a(b)?date=12#menu", "www.example.org/map"), links.map { note.substring(it.range) })
        assertEquals(listOf("https://example.com/a(b)?date=12#menu", "https://www.example.org/map"), links.map { it.url })
    }

    @Test
    fun skipsUnsupportedSchemesAndMissingHosts() {
        val note = "일반 메모 ftp://example.com intent://settings https:///missing-host www."

        assertTrue(findTravelNoteLinks(note).isEmpty())
    }
}
