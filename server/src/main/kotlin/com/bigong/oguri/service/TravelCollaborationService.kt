package com.bigong.oguri.service

import com.bigong.oguri.domain.TravelPlan
import com.bigong.oguri.repository.MemberRepository
import com.bigong.oguri.repository.TravelChangeRow
import com.bigong.oguri.repository.TravelCollaborationRepository
import com.bigong.oguri.repository.TravelMemberRow
import com.bigong.oguri.repository.TravelPlanRepository
import com.bigong.oguri.repository.TravelReminderRepository
import com.bigong.oguri.repository.TravelUsageEdit
import com.bigong.oguri.repository.TravelUsageEntry
import com.bigong.oguri.repository.TravelUsageRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Base64
import java.util.HexFormat
import java.util.UUID

@Service
class TravelCollaborationService(
    private val planService: TravelPlanService,
    private val plans: TravelPlanRepository,
    private val members: MemberRepository,
    private val collaboration: TravelCollaborationRepository,
    private val reminders: TravelReminderRepository,
    private val usage: TravelUsageRepository,
    @param:Value("\${TRAVEL_INVITE_TTL_DAYS:7}") private val inviteTtlDays: Long,
) {
    private val random: SecureRandom = SecureRandom()

    @Transactional
    fun members(
        memberId: String,
        planId: UUID,
    ): TravelMembersResponse {
        planService.requireMember(memberId)
        val plan = planService.accessiblePlan(memberId, planId)
        return TravelMembersResponse(memberId, collaboration.members(planId, plan.ownerId), collaboration.ownershipTarget(planId))
    }

    @Transactional
    fun issue(
        memberId: String,
        planId: UUID,
    ): IssuedTravelInvite {
        owner(memberId, planId)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(random::nextBytes))
        val expires = now().plusDays(inviteTtlDays.coerceIn(1, 30))
        collaboration.revokeInvites(planId)
        collaboration.issue(planId, memberId, hash(token), expires)
        return IssuedTravelInvite(token, expires)
    }

    @Transactional
    fun revoke(
        memberId: String,
        planId: UUID,
    ) {
        owner(memberId, planId)
        collaboration.revokeInvites(planId)
    }

    @Transactional
    fun remove(
        ownerId: String,
        planId: UUID,
        memberId: String,
    ) {
        owner(ownerId, planId)
        require(ownerId != memberId)
        if (collaboration.remove(planId, memberId)) {
            clearMemberAccess(planId, memberId)
        }
    }

    @Transactional
    fun leave(
        memberId: String,
        planId: UUID,
    ) {
        planService.requireMember(memberId)
        val plan = planService.accessiblePlan(memberId, planId, write = true)
        if (plan.ownerId == memberId) throw TravelInviteException(HttpStatus.CONFLICT, "OWNER_MUST_TRANSFER_OR_DELETE")
        collaboration.remove(planId, memberId)
        clearMemberAccess(planId, memberId)
    }

    @Transactional
    fun delete(
        memberId: String,
        planId: UUID,
    ) {
        plans.delete(owner(memberId, planId))
    }

    @Transactional
    fun preview(token: String): TravelInvitePreview {
        val hash = hash(token)
        val planId = lookupPlanId(hash)
        val plan = plans.findForRead(planId) ?: throw TravelInviteException(HttpStatus.GONE, "INVITE_TRIP_DELETED")
        val invite = collaboration.invite(hash) ?: throw TravelInviteException(HttpStatus.NOT_FOUND, "INVITE_UNKNOWN")
        requireActive(invite.expiresAt, invite.revokedAt)
        val inviter = invite.issuedBy?.let { members.findById(it).orElse(null)?.nickname }
        return TravelInvitePreview(plan.title, plan.startDate, plan.endDate, inviter)
    }

    @Transactional
    fun join(
        memberId: String,
        token: String,
    ): JoinedTravelPlan {
        planService.requireMember(memberId)
        if (members.findForTravelCreation(memberId) == null) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        val hash = hash(token)
        val planId = lookupPlanId(hash)
        val plan = plans.findForUpdate(planId) ?: throw TravelInviteException(HttpStatus.GONE, "INVITE_TRIP_DELETED")
        if (plan.ownerId == memberId || collaboration.isMember(planId, memberId)) return JoinedTravelPlan(planId)
        val invite = collaboration.invite(hash) ?: throw TravelInviteException(HttpStatus.NOT_FOUND, "INVITE_UNKNOWN")
        requireActive(invite.expiresAt, invite.revokedAt)
        collaboration.join(planId, memberId)
        usage.start(plan, plan.ownerId, TravelUsageEntry.EXISTING)
        usage.start(plan, memberId, TravelUsageEntry.JOINED)
        return JoinedTravelPlan(planId)
    }

    @Transactional
    fun updateDetails(
        memberId: String,
        planId: UUID,
        change: TravelDetailsChange,
    ) {
        val plan = owner(memberId, planId)
        val reminderTimeChanged = plan.startDate != change.startDate || plan.endDate != change.endDate || plan.timeZone != change.timeZone
        plan.updateDetails(
            change.city.trim(),
            change.title.trim(),
            change.timeZone,
            change.startDate,
            change.endDate,
            requireNotNull(change.expectedRevision),
            requireNotNull(change.confirmedUnassignedCount),
        )
        if (reminderTimeChanged) reminders.cancelForPlan(planId)
        collaboration.recordChange(planId, memberId, plan.revision, null, "TRIP_UPDATED")
        usage.recordEdit(plan, memberId, TravelUsageEdit.TRIP_UPDATED)
    }

    @Transactional
    fun offerOwnership(
        memberId: String,
        planId: UUID,
        targetId: String,
    ) {
        owner(memberId, planId)
        require(targetId != memberId && collaboration.isMember(planId, targetId))
        collaboration.offerOwnership(planId, targetId)
    }

    @Transactional
    fun acceptOwnership(
        memberId: String,
        planId: UUID,
    ) {
        if (members.findForTravelCreation(memberId) == null) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        val plan = planService.accessiblePlan(memberId, planId, write = true)
        if (collaboration.ownershipTarget(planId) != memberId) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val previousOwner = plan.ownerId
        collaboration.join(planId, previousOwner)
        collaboration.remove(planId, memberId)
        plan.transferTo(memberId)
        collaboration.clearOwnershipOffer(planId)
        collaboration.revokeInvites(planId)
        collaboration.recordChange(planId, memberId, plan.revision, null, "OWNER_CHANGED")
    }

    @Transactional
    fun recentChanges(
        memberId: String,
        planId: UUID,
    ): TravelRecentChanges {
        planService.requireMember(memberId)
        val plan = planService.accessiblePlan(memberId, planId)
        return TravelRecentChanges(
            plan.revision,
            collaboration.recentChanges(planId, memberId, plan.revision),
            plan.startDate.toString(),
            plan.endDate.toString(),
            plan.timeZone,
        )
    }

    @Transactional
    fun markChangesSeen(
        memberId: String,
        planId: UUID,
        revision: Long,
    ) {
        planService.requireMember(memberId)
        val plan = planService.accessiblePlan(memberId, planId)
        require(revision in 0..plan.revision)
        collaboration.markChangesSeen(planId, memberId, revision, 1)
    }

    private fun clearMemberAccess(
        planId: UUID,
        memberId: String,
    ) {
        reminders.cancelForMember(planId, memberId)
        collaboration.removeView(planId, memberId)
        if (collaboration.ownershipTarget(planId) == memberId) collaboration.clearOwnershipOffer(planId)
        collaboration.revokeInvites(planId)
    }

    @Transactional
    fun prepareAccountRemoval(
        memberId: String,
        administrative: Boolean,
    ) {
        if (members.findForTravelCreation(memberId) == null) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        collaboration.lockMemberPlans(memberId).forEach { planId ->
            val plan = plans.findForUpdate(planId) ?: return@forEach
            if (plan.ownerId == memberId) {
                val nextOwner = collaboration.members(planId, memberId).firstOrNull { !it.owner }
                if (nextOwner != null) {
                    if (!administrative) throw ResponseStatusException(HttpStatus.CONFLICT, "TRAVEL_OWNERSHIP_REQUIRED")
                    collaboration.remove(planId, nextOwner.id)
                    plan.transferTo(nextOwner.id)
                    collaboration.clearOwnershipOffer(planId)
                    collaboration.revokeInvites(planId)
                }
            } else {
                collaboration.remove(planId, memberId)
                clearMemberAccess(planId, memberId)
            }
        }
    }

    private fun owner(
        memberId: String,
        planId: UUID,
    ): TravelPlan {
        planService.requireMember(memberId)
        val plan = planService.accessiblePlan(memberId, planId, write = true)
        if (plan.ownerId != memberId) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        return plan
    }

    private fun lookupPlanId(hash: String): UUID {
        val invite = collaboration.invite(hash) ?: throw TravelInviteException(HttpStatus.NOT_FOUND, "INVITE_UNKNOWN")
        return invite.planId ?: throw TravelInviteException(HttpStatus.GONE, "INVITE_TRIP_DELETED")
    }

    private fun requireActive(
        expiresAt: OffsetDateTime,
        revokedAt: OffsetDateTime?,
    ) {
        if (revokedAt != null) throw TravelInviteException(HttpStatus.GONE, "INVITE_REVOKED")
        if (!expiresAt.isAfter(now())) throw TravelInviteException(HttpStatus.GONE, "INVITE_EXPIRED")
    }

    private fun hash(token: String): String {
        if (!TOKEN.matches(token)) throw TravelInviteException(HttpStatus.NOT_FOUND, "INVITE_UNKNOWN")
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.toByteArray()))
    }

    private fun now(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)

    private companion object {
        val TOKEN: Regex = Regex("[A-Za-z0-9_-]{43}")
    }
}

data class TravelMembersResponse(
    val currentMemberId: String,
    val members: List<TravelMemberRow>,
    val ownershipTarget: String?,
)

data class IssuedTravelInvite(
    val token: String,
    val expiresAt: OffsetDateTime,
)

data class TravelInvitePreview(
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val invitedBy: String?,
)

data class JoinedTravelPlan(
    val planId: UUID,
)

class TravelInviteException(
    val status: HttpStatus,
    val code: String,
) : RuntimeException(code)

data class TravelRecentChanges(
    val throughRevision: Long,
    val changes: List<TravelChangeRow>,
    val startDate: String,
    val endDate: String,
    val timeZone: String,
)

@JvmRecord
data class TravelDetailsChange(
    val city: String,
    val title: String,
    val timeZone: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val expectedRevision: Long?,
    val confirmedUnassignedCount: Int?,
)
