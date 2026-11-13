package com.bigong.oguri.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity
@Table(name = "travel_plans")
class TravelPlan(
    @Id
    val id: UUID,
    ownerId: String,
    @Column(nullable = false)
    val creationRequestId: UUID,
    @Column(nullable = false, length = 64)
    val creationFingerprint: String,
    city: String,
    timeZone: String,
    title: String,
    startDate: LocalDate,
    endDate: LocalDate,
    val sourceDestinationId: Int? = null,
    val sourceStartDate: LocalDate? = null,
    val sourceEndDate: LocalDate? = null,
    val creatorId: String? = ownerId,
) {
    @Column(nullable = false, length = 100)
    var ownerId: String = ownerId
        private set

    @Column(nullable = false, length = 100)
    var city: String = city
        private set

    @Column(nullable = false, length = 100)
    var title: String = title
        private set

    @Column(nullable = false, length = 100)
    var timeZone: String = timeZone
        private set

    @Column(nullable = false)
    var startDate: LocalDate = startDate
        private set

    @Column(nullable = false)
    var endDate: LocalDate = endDate
        private set

    @Column(nullable = false)
    var revision: Long = 0
        private set

    @Column(nullable = false)
    var structureRevision: Long = 0
        private set

    @OneToMany(mappedBy = "plan", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("dayNumber ASC, position ASC")
    private val visits: MutableList<TravelVisit> = mutableListOf()

    @ElementCollection
    @CollectionTable(name = "travel_day_revisions", joinColumns = [JoinColumn(name = "travel_plan_id")])
    @MapKeyColumn(name = "day_number")
    @Column(name = "revision", nullable = false)
    private val dayRevisions: MutableMap<Int, Long> = mutableMapOf()

    @ElementCollection
    @CollectionTable(name = "travel_plan_commands", joinColumns = [JoinColumn(name = "travel_plan_id")])
    @MapKeyColumn(name = "request_id")
    @Column(name = "fingerprint", nullable = false, length = 64)
    private val commands: MutableMap<UUID, String> = mutableMapOf()

    init {
        require(sourceDestinationId == null || sourceDestinationId > 0)
        require(
            sourceStartDate == null &&
                sourceEndDate == null ||
                sourceStartDate != null &&
                sourceEndDate != null &&
                !sourceStartDate.isAfter(sourceEndDate),
        )
        validateDetails(city, title, timeZone, startDate, endDate)
    }

    fun updateDetails(
        city: String,
        title: String,
        timeZone: String,
        startDate: LocalDate,
        endDate: LocalDate,
        expectedRevision: Long,
        confirmedUnassignedCount: Int,
    ) {
        if (revision != expectedRevision) throw TravelPlanConflict("TRIP_STRUCTURE_CHANGED")
        validateDetails(city, title, timeZone, startDate, endDate)
        val dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val unassigned = orderedVisits().filter { it.dayNumber > dayCount }
        if (unassigned.size != confirmedUnassignedCount) throw TravelPlanConflict("TRIP_STRUCTURE_CHANGED")
        this.city = city
        this.title = title
        this.timeZone = timeZone
        this.startDate = startDate
        this.endDate = endDate
        revision++
        structureRevision++
        var position = nextPosition(0)
        unassigned.forEach { visit ->
            dayRevisions[visit.dayNumber] = revision
            visit.moveTo(0, position++)
        }
        if (unassigned.isNotEmpty()) dayRevisions[0] = revision
    }

    fun transferTo(memberId: String) {
        require(memberId != ownerId)
        ownerId = memberId
        revision++
    }

    private fun validateDetails(
        city: String,
        title: String,
        timeZone: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ) {
        require(city.isNotBlank() && city.length <= MAX_TRIP_TEXT_LENGTH)
        require(title.isNotBlank() && title.length <= MAX_TRIP_TEXT_LENGTH)
        require(timeZone in ZoneId.getAvailableZoneIds())
        require(startDate.year in SUPPORTED_YEARS && endDate.year in SUPPORTED_YEARS)
        require(ChronoUnit.DAYS.between(startDate, endDate) in 0 until MAX_TRIP_DAYS.toLong())
    }

    fun orderedVisits(): List<TravelVisit> = visits.sortedWith(compareBy(TravelVisit::dayNumber, TravelVisit::position))

    fun googlePlaceIdsOn(dayNumber: Int): List<String> {
        requireValidDay(dayNumber)
        return visits
            .asSequence()
            .filter { it.dayNumber == dayNumber }
            .mapNotNull(TravelVisit::googlePlaceId)
            .distinct()
            .toList()
    }

    fun dayOrderRevisions(): Map<Int, Long> = dayRevisions.toMap()

    fun hasApplied(
        requestId: UUID,
        fingerprint: String,
    ): Boolean {
        val previous = commands[requestId] ?: return false
        if (previous != fingerprint) throw TravelPlanConflict("REQUEST_ID_REUSED")
        return true
    }

    fun recordCommand(
        requestId: UUID,
        fingerprint: String,
    ) {
        commands[requestId] = fingerprint
    }

    fun addVisit(
        dayNumber: Int,
        details: TravelVisitDetails,
    ) {
        requireValidDay(dayNumber)
        requirePlaceCapacity(dayNumber, details)
        revision++
        visits.add(TravelVisit(UUID.randomUUID(), this, dayNumber, nextPosition(dayNumber), details, revision))
        dayRevisions[dayNumber] = revision
    }

    fun editVisit(
        visitId: UUID,
        expectedRevision: Long,
        dayNumber: Int,
        details: TravelVisitDetails,
        base: TravelVisitDetails? = null,
        baseDayNumber: Int? = null,
    ) {
        requireValidDay(dayNumber)
        val visit = visits.firstOrNull { it.id == visitId } ?: throw TravelPlanConflict("VISIT_REMOVED")
        val previousDay = visit.dayNumber
        if ((base == null || baseDayNumber == null) && visit.revision != expectedRevision) {
            throw TravelPlanConflict("VISIT_CHANGED")
        }
        val nextDay = if (baseDayNumber != null && dayNumber == baseDayNumber) previousDay else dayNumber
        if (baseDayNumber != null && previousDay != baseDayNumber && dayNumber != baseDayNumber) {
            throw TravelPlanConflict("VISIT_CHANGED")
        }
        val merged = if (base == null) details else visit.merge(base, details)
        if (previousDay != nextDay) requirePlaceCapacity(nextDay, merged)
        revision++
        visit.edit(merged, revision)
        if (previousDay != nextDay) {
            visit.moveTo(nextDay, nextPosition(nextDay))
            dayRevisions[previousDay] = revision
            dayRevisions[nextDay] = revision
        }
    }

    fun removeVisit(
        visitId: UUID,
        expectedRevision: Long,
    ) {
        val visit = findVisit(visitId, expectedRevision)
        visits.remove(visit)
        revision++
        dayRevisions[visit.dayNumber] = revision
    }

    fun undoVisit(
        snapshot: TravelVisitUndo,
        changeRevision: Long,
    ) {
        if (!containsDay(snapshot.dayNumber) ||
            structureRevision != snapshot.structureRevision ||
            (dayRevisions[snapshot.dayNumber] ?: 0L) != changeRevision ||
            (snapshot.targetDay != null && dayRevisions[snapshot.targetDay] != changeRevision)
        ) {
            throw TravelPlanConflict("UNDO_NO_LONGER_AVAILABLE")
        }
        val visit = visits.firstOrNull { it.id == snapshot.id }
        if (snapshot.targetDay != null && (visit?.revision != changeRevision || visit.dayNumber != snapshot.targetDay)) {
            throw TravelPlanConflict("UNDO_NO_LONGER_AVAILABLE")
        }
        if (snapshot.targetDay == null && visit != null) throw TravelPlanConflict("UNDO_NO_LONGER_AVAILABLE")
        revision++
        if (visit == null) {
            visits.add(
                TravelVisit(snapshot.id, this, snapshot.dayNumber, snapshot.position, requireNotNull(snapshot.deletedDetails), revision),
            )
        } else {
            visit.moveTo(snapshot.dayNumber, snapshot.position)
            visit.edit(visit.details(), revision)
            dayRevisions[requireNotNull(snapshot.targetDay)] = revision
        }
        dayRevisions[snapshot.dayNumber] = revision
    }

    fun reorderDay(
        dayNumber: Int,
        expectedOrderRevision: Long,
        visitIds: List<UUID>,
    ) {
        requireValidDay(dayNumber)
        if ((dayRevisions[dayNumber] ?: 0L) != expectedOrderRevision) {
            throw TravelPlanConflict("VISIT_ORDER_CHANGED")
        }
        val visitsById = visits.filter { it.dayNumber == dayNumber }.associateBy(TravelVisit::id)
        require(visitIds.size == visitsById.size && visitIds.toSet() == visitsById.keys)
        visitIds.forEachIndexed { position, visitId ->
            visitsById.getValue(visitId).moveTo(dayNumber, position)
        }
        revision++
        dayRevisions[dayNumber] = revision
    }

    private fun requirePlaceCapacity(
        dayNumber: Int,
        details: TravelVisitDetails,
    ) {
        if (details.googlePlaceId != null &&
            visits.count { it.dayNumber == dayNumber && it.googlePlaceId != null } >= MAX_GOOGLE_VISITS_PER_DAY
        ) {
            throw TravelPlanConflict("GOOGLE_VISIT_LIMIT")
        }
    }

    fun containsDay(dayNumber: Int): Boolean = dayNumber.toLong() in 0..ChronoUnit.DAYS.between(startDate, endDate) + 1

    private fun requireValidDay(dayNumber: Int) {
        require(containsDay(dayNumber))
    }

    private fun nextPosition(dayNumber: Int): Int =
        (visits.filter { it.dayNumber == dayNumber }.maxOfOrNull(TravelVisit::position) ?: -1) + 1

    private fun findVisit(
        visitId: UUID,
        expectedRevision: Long,
    ): TravelVisit {
        val visit = visits.firstOrNull { it.id == visitId } ?: throw TravelPlanConflict("VISIT_REMOVED")
        if (visit.revision != expectedRevision) throw TravelPlanConflict("VISIT_CHANGED")
        return visit
    }

    private companion object {
        const val MAX_TRIP_TEXT_LENGTH: Int = 100
        const val MAX_GOOGLE_VISITS_PER_DAY: Int = 30
        const val MAX_TRIP_DAYS: Int = 366
        val SUPPORTED_YEARS: IntRange = 1..9999
    }
}

class TravelPlanConflict(
    val code: String,
) : RuntimeException(code)

data class TravelVisitUndo(
    val id: UUID,
    val dayNumber: Int,
    val position: Int,
    val structureRevision: Long,
    val targetDay: Int?,
    val deletedDetails: TravelVisitDetails?,
)
