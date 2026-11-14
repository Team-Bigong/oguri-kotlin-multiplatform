package com.bigong.oguri.controller

import com.bigong.oguri.config.AuthContext
import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.config.resolveTravelUsageSession
import com.bigong.oguri.repository.TravelRecommendationEntryKind
import com.bigong.oguri.repository.TravelRecommendationSource
import com.bigong.oguri.repository.TravelUsageRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class TravelRecommendationUsageController(
    private val usage: TravelUsageRepository,
) {
    @PostMapping("/api/v1/travel-recommendation-entries")
    fun record(
        request: HttpServletRequest,
        @RequestBody body: TravelRecommendationEntryRequest,
    ): ResponseEntity<Void> {
        val sessionId =
            request.resolveTravelUsageSessionSafely()
                ?: return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).build()
        val signedIn = request.resolveMemberId() != AuthContext.GUEST_MEMBER_ID
        if (!signedIn && body.kind == TravelRecommendationEntryKind.LOGIN_RESUMED) {
            return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).build()
        }
        usage.recordRecommendationEntry(sessionId, body.source, body.kind, signedIn)
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build()
    }

    private fun HttpServletRequest.resolveTravelUsageSessionSafely(): UUID? =
        try {
            resolveTravelUsageSession()
        } catch (_: IllegalArgumentException) {
            null
        }
}

data class TravelRecommendationEntryRequest(
    val source: TravelRecommendationSource,
    val kind: TravelRecommendationEntryKind,
)
