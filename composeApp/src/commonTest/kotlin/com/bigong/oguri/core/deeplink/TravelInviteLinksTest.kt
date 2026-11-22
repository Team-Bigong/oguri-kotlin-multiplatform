package com.bigong.oguri.core.deeplink

import io.ktor.http.encodeURLParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TravelInviteLinksTest {
    private val token = "A".repeat(43)

    @Test
    fun receivesOnlyTheExpectedInvitationUrls() {
        assertEquals("https://www.oguri.kr/trip-invite/#$token", travelInviteLink(token))
        val encodedInvite = travelInviteLink(token).encodeURLParameter()
        assertEquals(token, parseTravelInvite("kakao123://kakaolink?deeplink=$encodedInvite"))
        val encodedInviteWithoutFragment = "https://$TRAVEL_INVITE_HOST/trip-invite/".encodeURLParameter()
        assertEquals(token, parseTravelInvite("kakao123://kakaolink?deeplink=$encodedInviteWithoutFragment#$token"))
        assertEquals(TravelInviteLink(null), readTravelInviteLink("https://$TRAVEL_INVITE_HOST/trip-invite/"))
        assertNull(readTravelInviteLink("kakao123://kakaolink?deeplink=https%3A%2F%2Fother.example%2Ftrip-invite%2F#$token"))
        assertEquals(token, parseTravelInvite("https://oguri.kr/trip-invite/#$token"))
        assertEquals(token, parseTravelInvite("https://oguri-kotlin-multiplatform-19hx.onrender.com/trip-invite/#$token"))
        assertEquals(token, parseTravelInvite(travelInviteLink(token)))
        assertEquals(token, parseTravelInvite("https://$TRAVEL_INVITE_HOST/trip-invite#$token"))
        assertEquals(token, parseTravelInvite("oguri://open/trip-invite#$token"))
        assertNull(parseTravelInvite("https://other.example/trip-invite#$token"))
        assertNull(parseTravelInvite("https://$TRAVEL_INVITE_HOST/trip-invite?token=$token"))
        assertNull(parseTravelInvite("https://$TRAVEL_INVITE_HOST/other#$token"))
        assertNull(parseTravelInvite("https://$TRAVEL_INVITE_HOST/trip-invite#short"))
    }
}
