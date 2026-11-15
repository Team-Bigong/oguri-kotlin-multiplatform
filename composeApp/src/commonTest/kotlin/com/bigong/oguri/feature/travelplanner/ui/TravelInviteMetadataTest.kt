package com.bigong.oguri.feature.travelplanner.ui

import com.bigong.oguri.data.remote.model.response.TravelMembersResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class TravelInviteMetadataTest {
    @Test
    fun previousServerResponseKeepsExpirationOptional() {
        val response = Json.decodeFromString<TravelMembersResponse>("""{"currentMemberId":"owner","members":[],"hasActiveInvite":true}""")
        assertNull(response.inviteExpiresAt)
    }

    @Test
    fun activeInviteExpiryKeepsItsInstantAcrossOffsetFormatting() {
        val response = Json.decodeFromString<TravelMembersResponse>("""{"currentMemberId":"owner","members":[],"hasActiveInvite":true,"inviteExpiresAt":"2026-12-01T09:00:00+09:00"}""")
        assertEquals(Instant.parse("2026-12-01T00:00:00Z"), response.inviteExpiresAt?.let(Instant::parse))
    }
}
