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
import com.bigong.oguri.repository.TravelCatalogCityRepository
import com.bigong.oguri.repository.TravelCollaborationRepository
import com.bigong.oguri.repository.TravelPlanRepository
import com.bigong.oguri.repository.TravelPlanViewRepository
import com.bigong.oguri.repository.TravelUsageActivity
import com.bigong.oguri.repository.TravelUsageEdit
import com.bigong.oguri.repository.TravelUsageEntry
import com.bigong.oguri.repository.TravelUsageRepository
import com.bigong.oguri.repository.resolveCityReferences
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
    private val usage: TravelUsageRepository,
    private val cityCatalog: TravelCatalogCityRepository,
) {
    @Transactional
    fun create(
        memberId: String,
        request: CreateTravelPlanRequest,
        sessionId: UUID? = null,
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
        val plan =
            plans
                .saveAndFlush(
                    TravelPlan(
                        id = UUID.randomUUID(),
                        ownerId = memberId,
                        creationRequestId = request.requestId,
                        creationFingerprint = fingerprint,
                        city = request.city.trim(),
                        cities = request.cities.map(String::trim),
                        cityReferences = cityCatalog.resolveCityReferences(request.cityIds.orEmpty()),
                        timeZone = request.timeZone.trim(),
                        title = request.title.trim(),
                        startDate = request.startDate,
                        endDate = request.endDate,
                        sourceDestinationId = request.source?.destinationId,
                        sourceStartDate = request.source?.startDate,
                        sourceEndDate = request.source?.endDate,
                    ),
                )
        usage.start(plan, memberId, TravelUsageEntry.CREATED, sessionId)
        return plan.toSummary()
    }

    @Transactional(readOnly = true)
    fun list(memberId: String): List<TravelPlanSummaryResponse> {
        requireMember(memberId)
        val accessible = plans.findAccessible(memberId)
        val memberCounts = collaboration.memberCounts(accessible.map(TravelPlan::id))
        return accessible.map { it.toSummary(memberCounts[it.id]) }
    }

    @Transactional
    fun get(
        memberId: String,
        planId: UUID,
    ): TravelPlanResponse {
        requireMember(memberId)
        val plan = accessiblePlan(memberId, planId)
        return plan.toResponseFor(memberId)
    }

    @Transactional
    fun getIfChanged(
        memberId: String,
        planId: UUID,
        knownRevision: Long,
    ): TravelPlanResponse? {
        require(knownRevision >= 0)
        requireMember(memberId)
        val plan = accessiblePlan(memberId, planId)
        if (plan.revision == knownRevision) return null
        return plan.toResponseFor(memberId)
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
    internal fun cityForPlaces(
        memberId: String,
        planId: UUID,
        selectedCity: String? = null,
    ): TravelPlaceSearchCity {
        requireMember(memberId)
        val city = accessiblePlan(memberId, planId).cityForPlaces(selectedCity)
        return TravelPlaceSearchCity(city.label, city.catalogCity?.let { TravelPlaceSearchCenter(it.latitude, it.longitude) })
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
    fun placeIdsForPlan(
        memberId: String,
        planId: UUID,
    ): List<String> {
        requireMember(memberId)
        return accessiblePlan(memberId, planId).googlePlaceIds()
    }

    @Transactional
    fun execute(
        memberId: String,
        planId: UUID,
        command: TravelPlanCommand,
        sessionId: UUID? = null,
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

        val changeDay =
            when (command) {
                is TravelPlanCommand.AddVisit -> command.dayNumber
                is TravelPlanCommand.EditVisit -> originalVisit?.dayNumber
                is TravelPlanCommand.ReorderDay -> command.dayNumber
                is TravelPlanCommand.RemoveVisit -> original?.dayNumber
                is TravelPlanCommand.UndoVisit -> undo?.second?.dayNumber
            }

        val changeKind =
            when (command) {
                is TravelPlanCommand.AddVisit -> TravelUsageEdit.VISIT_ADDED
                is TravelPlanCommand.EditVisit -> TravelUsageEdit.VISIT_EDITED
                is TravelPlanCommand.ReorderDay -> TravelUsageEdit.DAY_REORDERED
                is TravelPlanCommand.RemoveVisit -> TravelUsageEdit.VISIT_REMOVED
                is TravelPlanCommand.UndoVisit -> TravelUsageEdit.VISIT_UNDONE
            }
        usage.recordEdit(plan, memberId, changeKind, sessionId)
        collaboration.recordMemberCommand(planId, memberId, command.requestId, fingerprint)
        collaboration.recordChange(
            planId,
            memberId,
            plan.revision,
            changeDay,
            changeKind.name,
            visitId =
                when (command) {
                    is TravelPlanCommand.AddVisit -> plan.orderedVisits().single { it.revision == plan.revision }.id
                    is TravelPlanCommand.EditVisit -> command.visitId
                    is TravelPlanCommand.RemoveVisit -> command.visitId
                    is TravelPlanCommand.UndoVisit -> undo?.second?.id
                    is TravelPlanCommand.ReorderDay -> null
                },
        )
        return plan.toResponse()
    }

    @Transactional
    fun recordActivity(
        memberId: String,
        planId: UUID,
        sessionId: UUID,
        kind: TravelUsageActivity,
    ) {
        requireMember(memberId)
        val plan = accessiblePlan(memberId, planId, write = true)
        if (kind == TravelUsageActivity.NEXT_HOLIDAY_OPENED) {
            val today = Instant.now().atZone(ZoneId.of(plan.timeZone)).toLocalDate()
            if (!plan.endDate.isBefore(today)) throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        usage.recordActivity(plan, memberId, sessionId, kind)
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
            MessageDigest.getInstance("SHA-256").digest(objectMapper.writeValueAsBytes(
                if (request is CreateTravelPlanRequest && request.cities == listOf(request.city)) {
                    objectMapper.valueToTree<com.fasterxml.jackson.databind.node.ObjectNode>(request).also { it.remove("cities") }
                } else request,
            )),
        )

    private fun TravelVisitRequest.toDomain(): TravelVisitDetails =
        TravelVisitDetails(
            name = name?.trim(),
            location = location?.trim()?.takeIf(String::isNotEmpty),
            localTime = localTime,
            note = note?.trim()?.takeIf(String::isNotEmpty),
            googlePlaceId = googlePlaceId,
            category = if (category == com.bigong.oguri.domain.TravelVisitCategory.FLIGHT) com.bigong.oguri.domain.TravelVisitCategory.TRANSPORT else category,
        )

    private fun TravelPlan.toSummary(memberCount: Int? = null): TravelPlanSummaryResponse =
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
            memberCount,
            cityNames(),
            catalogCities().map { (label, catalogCity) -> catalogCity.toSuggestion(label) },
        )

    private fun TravelPlan.toResponseFor(memberId: String): TravelPlanResponse =
        toResponse(lastViewedDay = views.lastDay(memberId, id)?.takeIf(::containsDay))

    private fun TravelPlan.toResponse(lastViewedDay: Int? = null): TravelPlanResponse =
        TravelPlanResponse(
            summary = toSummary(),
            lastViewedDay = lastViewedDay,
            lastModifiedAt = collaboration.lastModifiedAt(id),
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
                        category = visit.category,
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
            deletedDetails?.let { TravelVisitDetails(it.name, it.location, it.localTime, it.note, it.googlePlaceId, it.category) },
        )

    companion object {
        fun from(undo: TravelVisitUndo): StoredVisitUndo =
            StoredVisitUndo(
                undo.id,
                undo.dayNumber,
                undo.position,
                undo.structureRevision,
                undo.targetDay,
                undo.deletedDetails?.let { TravelVisitRequest(it.name, it.location, it.localTime, it.note, it.googlePlaceId, it.category) },
            )
    }
}
