package com.bigong.oguri.core.deeplink

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TravelInviteLinksTest {
    private val token = "A".repeat(43)

    @Test
    fun receivesOnlyTheExpectedInvitationUrls() {
        assertEquals(token, parseTravelInvite(travelInviteLink(token)))
        assertEquals(token, parseTravelInvite("oguri://open/trip-invite#$token"))
        assertNull(parseTravelInvite("https://other.example/trip-invite#$token"))
        assertNull(parseTravelInvite("https://$TRAVEL_INVITE_HOST/trip-invite?token=$token"))
        assertNull(parseTravelInvite("https://$TRAVEL_INVITE_HOST/other#$token"))
        assertNull(parseTravelInvite("https://$TRAVEL_INVITE_HOST/trip-invite#short"))
    }
}
