package com.bigong.oguri.service

import com.bigong.oguri.config.AuthContext
import com.bigong.oguri.domain.TravelPlan
import com.bigong.oguri.domain.TravelPlanConflict
import com.bigong.oguri.domain.TravelVisitDetails
import com.bigong.oguri.domain.TravelVisitUndo
import com.bigong.oguri.dto.request.CreateTravelPlanRequest
import com.bigong.oguri.dto.request.TravelPlanCommand
import com.bigong.oguri.dto.request.TravelVisitRequest
import com.bigong.oguri.dto.response.TravelPlanResponse
import com.bigong.oguri.dto.response.TravelPlanSourceResponse
import com.bigong.oguri.dto.response.TravelPlanSummaryResponse
import com.bigong.oguri.dto.response.TravelVisitResponse
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.MemberRepository
import com.bigong.oguri.repository.TravelCollaborationRepository
import com.bigong.oguri.repository.TravelPlanRepository
import com.bigong.oguri.repository.TravelPlanViewRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.util.HexFormat
import java.util.UUID

@Service
class TravelPlanService(
    private val plans: TravelPlanRepository,
    private val members: MemberRepository,
    private val objectMapper: ObjectMapper,
    private val destinations: DestinationRepository,
    private val views: TravelPlanViewRepository,
    private val collaboration: TravelCollaborationRepository,
) {
    @Transactional
    fun create(
        memberId: String,
        request: CreateTravelPlanRequest,
    ): TravelPlanSummaryResponse {
        // The member row serializes creation retries, including the first insert.
        if (memberId == AuthContext.GUEST_MEMBER_ID || members.findForTravelCreation(memberId) == null) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        val fingerprint = fingerprint(request)
        val existing = plans.findByCreatorIdAndCreationRequestId(memberId, request.requestId)
        if (existing != null) {
            if (existing.creationFingerprint != fingerprint) throw TravelPlanConflict("REQUEST_ID_REUSED")
            if (existing.ownerId != memberId && !collaboration.isMember(existing.id, memberId)) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            }
            return existing.toSummary()
        }
        request.source?.destinationId?.let { require(destinations.existsById(it)) }
        return plans
            .save(
                TravelPlan(
                    id = UUID.randomUUID(),
                    ownerId = memberId,
                    creationRequestId = request.requestId,
                    creationFingerprint = fingerprint,
                    city = request.city.trim(),
                    timeZone = request.timeZone.trim(),
                    title = request.title.trim(),
                    startDate = request.startDate,
                    endDate = request.endDate,
                    sourceDestinationId = request.source?.destinationId,
                    sourceStartDate = request.source?.startDate,
                    sourceEndDate = request.source?.endDate,
                ),
            ).toSummary()
    }

    @Transactional(readOnly = true)
    fun list(memberId: String): List<TravelPlanSummaryResponse> {
        requireMember(memberId)
        return plans.findAccessible(memberId).map { it.toSummary() }
    }

    @Transactional
    fun get(
        memberId: String,
        planId: UUID,
    ): TravelPlanResponse {
        requireMember(memberId)
        val plan = accessiblePlan(memberId, planId)
        return plan.toResponse(lastViewedDay = views.lastDay(memberId, planId)?.takeIf(plan::containsDay))
    }

    @Transactional(readOnly = true)
    fun resume(memberId: String): TravelPlanSummaryResponse? {
        requireMember(memberId)
        val now = Instant.now()
        val current =
            plans.findAccessible(memberId).filter { plan ->
                !plan.endDate.isBefore(now.atZone(ZoneId.of(plan.timeZone)).toLocalDate())
            }
        val (inProgress, upcoming) =
            current.partition { plan ->
                !plan.startDate.isAfter(now.atZone(ZoneId.of(plan.timeZone)).toLocalDate())
            }
        val candidates = inProgress.ifEmpty { upcoming }
        if (candidates.isEmpty()) return null
        val opened = views.lastOpenedByPlan(memberId)
        return (candidates.filter { it.id in opened }.maxByOrNull { opened.getValue(it.id) } ?: candidates.first()).toSummary()
    }

    @Transactional
    fun recordView(
        memberId: String,
        planId: UUID,
        dayNumber: Int,
    ) {
        requireMember(memberId)
        val plan = accessiblePlan(memberId, planId)
        require(plan.containsDay(dayNumber))
        views.record(memberId, planId, dayNumber)
    }

    @Transactional
    fun cityForPlaces(
        memberId: String,
        planId: UUID,
    ): String {
        requireMember(memberId)
        return accessiblePlan(memberId, planId).city
    }

    @Transactional
    fun placeIdsForDay(
        memberId: String,
        planId: UUID,
        dayNumber: Int,
    ): List<String> {
        requireMember(memberId)
        val plan = accessiblePlan(memberId, planId)
        return plan.googlePlaceIdsOn(dayNumber)
    }

    @Transactional
    fun execute(
        memberId: String,
        planId: UUID,
        command: TravelPlanCommand,
    ): TravelPlanResponse {
        requireMember(memberId)
        val plan = accessiblePlan(memberId, planId, write = true)
        val fingerprint = fingerprint(command)
        val previous = collaboration.memberCommand(planId, memberId, command.requestId)
        if (previous != null) {
            if (previous != fingerprint) throw TravelPlanConflict("REQUEST_ID_REUSED")
            return plan.toResponse()
        }
        if (plan.creatorId == memberId && plan.hasApplied(command.requestId, fingerprint)) return plan.toResponse()
        command.expectedStructureRevision?.let {
            if (it != plan.structureRevision) throw TravelPlanConflict("TRIP_STRUCTURE_CHANGED")
        }
        val originalVisit =
            when (command) {
                is TravelPlanCommand.EditVisit -> plan.orderedVisits().firstOrNull { it.id == command.visitId }
                is TravelPlanCommand.RemoveVisit -> plan.orderedVisits().firstOrNull { it.id == command.visitId }
                else -> null
            }
        val original =
            originalVisit?.let {
                TravelVisitUndo(
                    it.id,
                    it.dayNumber,
                    it.position,
                    plan.structureRevision,
                    null,
                    it.details().takeIf { command is TravelPlanCommand.RemoveVisit },
                )
            }
        val undo =
            if (command is TravelPlanCommand.UndoVisit) {
                val stored =
                    collaboration.takeVisitUndo(planId, memberId, command.originalRequestId)
                        ?: throw TravelPlanConflict("UNDO_NO_LONGER_AVAILABLE")
                stored.first to objectMapper.readValue(stored.second, StoredVisitUndo::class.java).toDomain()
            } else {
                null
            }
        val changeDay =
            when (command) {
                is TravelPlanCommand.AddVisit -> command.dayNumber
                is TravelPlanCommand.EditVisit -> command.dayNumber
                is TravelPlanCommand.ReorderDay -> command.dayNumber
                is TravelPlanCommand.RemoveVisit -> original?.dayNumber
                is TravelPlanCommand.UndoVisit -> undo?.second?.dayNumber
            }

        when (command) {
            is TravelPlanCommand.AddVisit ->
                plan.addVisit(requireNotNull(command.dayNumber), command.details.toDomain())
            is TravelPlanCommand.EditVisit ->
                plan.editVisit(
                    visitId = command.visitId,
                    expectedRevision = requireNotNull(command.expectedVisitRevision),
                    dayNumber = requireNotNull(command.dayNumber),
                    details = command.details.toDomain(),
                    base = command.base?.toDomain(),
                    baseDayNumber = command.baseDayNumber,
                )
            is TravelPlanCommand.RemoveVisit ->
                plan.removeVisit(command.visitId, requireNotNull(command.expectedVisitRevision))
            is TravelPlanCommand.ReorderDay ->
                plan.reorderDay(
                    dayNumber = requireNotNull(command.dayNumber),
                    expectedOrderRevision = requireNotNull(command.expectedOrderRevision),
                    visitIds = command.visitIds,
                )
            is TravelPlanCommand.UndoVisit -> {
                val (revision, snapshot) = requireNotNull(undo)
                plan.undoVisit(snapshot, revision)
            }
        }

        if (original != null && (command is TravelPlanCommand.RemoveVisit || originalVisit.dayNumber != original.dayNumber)) {
            val target = if (command is TravelPlanCommand.RemoveVisit) null else originalVisit.dayNumber
            val snapshot = StoredVisitUndo.from(original.copy(targetDay = target))
            collaboration.storeVisitUndo(planId, memberId, command.requestId, plan.revision, objectMapper.writeValueAsString(snapshot))
        }

        collaboration.recordMemberCommand(planId, memberId, command.requestId, fingerprint)
        collaboration.recordChange(
            planId,
            memberId,
            plan.revision,
            changeDay,
            when (command) {
                is TravelPlanCommand.AddVisit -> "VISIT_ADDED"
                is TravelPlanCommand.EditVisit -> "VISIT_EDITED"
                is TravelPlanCommand.ReorderDay -> "DAY_REORDERED"
                is TravelPlanCommand.RemoveVisit -> "VISIT_REMOVED"
                is TravelPlanCommand.UndoVisit -> "VISIT_UNDONE"
            },
        )
        return plan.toResponse()
    }

    internal fun accessiblePlan(
        memberId: String,
        planId: UUID,
        write: Boolean = false,
    ): TravelPlan {
        val plan =
            (if (write) plans.findForUpdate(planId) else plans.findForRead(planId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        if (plan.ownerId != memberId && !collaboration.isMember(planId, memberId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
        return plan
    }

    internal fun requireMember(memberId: String) {
        if (memberId == AuthContext.GUEST_MEMBER_ID || !members.existsById(memberId)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    private fun fingerprint(request: Any): String =
        HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(objectMapper.writeValueAsBytes(request)),
        )

    private fun TravelVisitRequest.toDomain(): TravelVisitDetails =
        TravelVisitDetails(
            name = name?.trim(),
            location = location?.trim()?.takeIf(String::isNotEmpty),
            localTime = localTime,
            note = note?.trim()?.takeIf(String::isNotEmpty),
            googlePlaceId = googlePlaceId,
        )

    private fun TravelPlan.toSummary(): TravelPlanSummaryResponse =
        TravelPlanSummaryResponse(
            id,
            city,
            timeZone,
            title,
            startDate,
            endDate,
            if (sourceDestinationId == null && sourceStartDate == null) {
                null
            } else {
                TravelPlanSourceResponse(sourceDestinationId, sourceStartDate, sourceEndDate)
            },
        )

    private fun TravelPlan.toResponse(lastViewedDay: Int? = null): TravelPlanResponse =
        TravelPlanResponse(
            summary = toSummary(),
            lastViewedDay = lastViewedDay,
            revision = revision,
            structureRevision = structureRevision,
            dayOrderRevisions = dayOrderRevisions(),
            visits =
                orderedVisits().map { visit ->
                    TravelVisitResponse(
                        id = visit.id,
                        dayNumber = visit.dayNumber,
                        name = visit.name,
                        location = visit.location,
                        localTime = visit.localTime?.toString(),
                        note = visit.note,
                        googlePlaceId = visit.googlePlaceId,
                        revision = visit.revision,
                    )
                },
        )
}

@JvmRecord
data class StoredVisitUndo(
    val id: UUID,
    val dayNumber: Int,
    val position: Int,
    val structureRevision: Long,
    val targetDay: Int?,
    val deletedDetails: TravelVisitRequest?,
) {
    fun toDomain(): TravelVisitUndo =
        TravelVisitUndo(
            id,
            dayNumber,
            position,
            structureRevision,
            targetDay,
            deletedDetails?.let { TravelVisitDetails(it.name, it.location, it.localTime, it.note, it.googlePlaceId) },
        )

    companion object {
        fun from(undo: TravelVisitUndo): StoredVisitUndo =
            StoredVisitUndo(
                undo.id,
                undo.dayNumber,
                undo.position,
                undo.structureRevision,
                undo.targetDay,
                undo.deletedDetails?.let { TravelVisitRequest(it.name, it.location, it.localTime, it.note, it.googlePlaceId) },
            )
    }
}
