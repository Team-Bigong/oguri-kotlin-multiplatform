package com.bigong.oguri.controller

import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.config.resolveTravelUsageSession
import com.bigong.oguri.domain.TravelPlanConflict
import com.bigong.oguri.dto.response.TravelPlanErrorResponse
import com.bigong.oguri.service.IssuedTravelInvite
import com.bigong.oguri.service.JoinedTravelPlan
import com.bigong.oguri.service.TravelCollaborationService
import com.bigong.oguri.service.TravelDetailsChange
import com.bigong.oguri.service.TravelInviteException
import com.bigong.oguri.service.TravelInvitePreview
import com.bigong.oguri.service.TravelMembersResponse
import com.bigong.oguri.service.TravelRecentChanges
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class TravelCollaborationController(
    private val service: TravelCollaborationService,
) {
    @GetMapping("/trips/{tripId}/members")
    fun members(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
    ): ResponseEntity<TravelMembersResponse> = noStore(service.members(request.resolveMemberId(), tripId))

    @PostMapping("/trips/{tripId}/invites")
    fun issue(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
    ): ResponseEntity<IssuedTravelInvite> = noStore(service.issue(request.resolveMemberId(), tripId))

    @DeleteMapping("/trips/{tripId}/invites")
    fun revoke(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
    ): ResponseEntity<Void> {
        service.revoke(request.resolveMemberId(), tripId)
        return noContent()
    }

    @DeleteMapping("/trips/{tripId}/members/{memberId}")
    fun remove(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @PathVariable memberId: String,
    ): ResponseEntity<Void> {
        service.remove(request.resolveMemberId(), tripId, memberId)
        return noContent()
    }

    @DeleteMapping("/trips/{tripId}/membership")
    fun leave(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
    ): ResponseEntity<Void> {
        service.leave(request.resolveMemberId(), tripId)
        return noContent()
    }

    @DeleteMapping("/trips/{tripId}")
    fun delete(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
    ): ResponseEntity<Void> {
        service.delete(request.resolveMemberId(), tripId)
        return noContent()
    }

    @PutMapping("/trips/{tripId}/details")
    fun updateDetails(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @RequestBody body: TravelDetailsChange,
    ): ResponseEntity<Void> {
        service.updateDetails(request.resolveMemberId(), tripId, body, request.resolveTravelUsageSession())
        return noContent()
    }

    @PostMapping("/trips/{tripId}/ownership")
    fun offerOwnership(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @RequestBody body: TravelOwnershipRequest,
    ): ResponseEntity<Void> {
        service.offerOwnership(request.resolveMemberId(), tripId, body.targetId)
        return noContent()
    }

    @PostMapping("/trips/{tripId}/ownership/accept")
    fun acceptOwnership(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
    ): ResponseEntity<Void> {
        service.acceptOwnership(request.resolveMemberId(), tripId)
        return noContent()
    }

    @GetMapping("/trips/{tripId}/changes")
    fun recentChanges(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
    ): ResponseEntity<TravelRecentChanges> = noStore(service.recentChanges(request.resolveMemberId(), tripId))

    @PutMapping("/trips/{tripId}/changes/seen")
    fun markChangesSeen(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @RequestBody body: TravelChangesSeenRequest,
    ): ResponseEntity<Void> {
        service.markChangesSeen(request.resolveMemberId(), tripId, requireNotNull(body.revision))
        return noContent()
    }

    @ExceptionHandler(TravelPlanConflict::class)
    fun conflict(error: TravelPlanConflict): ResponseEntity<TravelPlanErrorResponse> =
        ResponseEntity.status(409).cacheControl(CacheControl.noStore()).body(TravelPlanErrorResponse(error.code))

    @PostMapping("/trip-invites/preview")
    fun preview(
        @RequestBody body: TravelInviteRequest,
    ): ResponseEntity<TravelInvitePreview> = noStore(service.preview(body.token))

    @PostMapping("/trip-invites/join")
    fun join(
        request: HttpServletRequest,
        @RequestBody body: TravelInviteRequest,
    ): ResponseEntity<JoinedTravelPlan> = noStore(service.join(request.resolveMemberId(), body.token, request.resolveTravelUsageSession()))

    @ExceptionHandler(TravelInviteException::class)
    fun inviteFailure(error: TravelInviteException): ResponseEntity<TravelPlanErrorResponse> =
        ResponseEntity.status(error.status).cacheControl(CacheControl.noStore()).body(TravelPlanErrorResponse(error.code))

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidRequest(): ResponseEntity<TravelPlanErrorResponse> =
        ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(TravelPlanErrorResponse("INVALID_TRAVEL_REQUEST"))

    private fun <Body> noStore(body: Body): ResponseEntity<Body> = ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body)

    private fun noContent(): ResponseEntity<Void> = ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build()
}

@JvmRecord
data class TravelInviteRequest(
    val token: String,
)

@JvmRecord
data class TravelOwnershipRequest(
    val targetId: String,
)

@JvmRecord
data class TravelChangesSeenRequest(
    val revision: Long?,
)
