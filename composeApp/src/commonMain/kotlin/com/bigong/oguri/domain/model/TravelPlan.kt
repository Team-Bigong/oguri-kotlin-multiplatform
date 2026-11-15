package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TravelDates(
    val start: LocalDate,
    val endInclusive: LocalDate,
) {
    val dayCount: Int = (endInclusive.toEpochDays() - start.toEpochDays() + 1).toInt()

    init {
        require(start.year in SUPPORTED_YEARS && endInclusive.year in SUPPORTED_YEARS)
        require(dayCount in 1..MAX_DAYS)
    }

    fun dateOn(dayNumber: Int): LocalDate {
        require(dayNumber in 1..dayCount)
        return LocalDate.fromEpochDays(start.toEpochDays() + dayNumber - 1)
    }

    companion object {
        const val MAX_DAYS: Int = 366
        val SUPPORTED_YEARS: IntRange = 1..9999
    }
}

@Serializable
data class TravelPlanDetails(
    val city: String,
    val timeZone: TimeZone,
    val title: String,
    val dates: TravelDates,
    val source: TravelPlanSource? = null,
    val cities: List<String> = listOf(city),
) {
    init {
        require(cities.size in 1..10 && cities.distinct().size == cities.size && cities.first() == city)
        require(cities.all { it.isNotBlank() && it.length <= MAX_TEXT_LENGTH })
        require(city.isNotBlank() && city.length <= MAX_TEXT_LENGTH)
        require(title.isNotBlank() && title.length <= MAX_TEXT_LENGTH)
    }

    companion object {
        const val MAX_TEXT_LENGTH: Int = 100
    }
}

@Serializable
data class TravelPlanSource(
    val destinationId: Long?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)

@Serializable
data class TravelPlanSummary(
    val id: String,
    val details: TravelPlanDetails,
    val memberCount: Int? = null,
)

@Serializable
data class TravelPlan(
    val summary: TravelPlanSummary,
    val revision: Long,
    val dayOrderRevisions: Map<Int, Long>,
    val visits: List<TravelVisit>,
    val lastViewedDay: Int? = null,
    val structureRevision: Long = 0,
    val lastModifiedAt: Instant? = null,
) {
    fun orderRevision(dayNumber: Int): Long = dayOrderRevisions[dayNumber] ?: 0

    companion object {
        const val UNASSIGNED_DAY: Int = 0
    }
}

@Serializable
data class TravelVisit(
    val id: String,
    val dayNumber: Int,
    val details: TravelVisitDetails,
    val revision: Long,
)

@Serializable
data class TravelVisitDetails(
    val name: String?,
    val location: String?,
    val localTime: LocalTime?,
    val note: String?,
    val googlePlaceId: String? = null,
    val category: TravelVisitCategory? = null,
) {
    companion object {
        const val MAX_NAME_LENGTH: Int = 150
        const val MAX_LOCATION_LENGTH: Int = 250
        const val MAX_NOTE_LENGTH: Int = 1000
    }
}

@Serializable
sealed interface TravelVisitChange {
    @Serializable
    data class Add(
        val dayNumber: Int,
        val details: TravelVisitDetails,
        val structureRevision: Long = 0,
    ) : TravelVisitChange

    @Serializable
    data class Edit(
        val visit: TravelVisit,
        val dayNumber: Int,
        val details: TravelVisitDetails,
        val structureRevision: Long = 0,
    ) : TravelVisitChange

    @Serializable
    data class Remove(
        val visit: TravelVisit,
        val structureRevision: Long = 0,
    ) : TravelVisitChange

    @Serializable
    data class Undo(
        val originalRequestId: String,
    ) : TravelVisitChange

    @Serializable
    data class Reorder(
        val dayNumber: Int,
        val expectedOrderRevision: Long,
        val visitIds: List<String>,
        val structureRevision: Long = 0,
    ) : TravelVisitChange {
        fun rebasedVisitIds(plan: TravelPlan): List<String>? {
            if (dayNumber !in 1..plan.summary.details.dates.dayCount || structureRevision != plan.structureRevision) return null

            val requestedIds = visitIds.toSet()
            val currentIds = plan.visits.filter { it.dayNumber == dayNumber }.map(TravelVisit::id)
            val currentIdSet = currentIds.toSet()
            if (requestedIds.size != visitIds.size || currentIdSet.size != currentIds.size || !currentIdSet.containsAll(requestedIds)) return null

            val requestedOrder = visitIds.iterator()
            return currentIds.map { id -> if (id in requestedIds) requestedOrder.next() else id }
        }
    }
}

enum class TravelPlanFailure {
    SIGN_IN_REQUIRED,
    UNAVAILABLE,
    CONFLICT,
    VISIT_REMOVED,
    UNDO_UNAVAILABLE,
    VISIT_LIMIT,
    RATE_LIMIT,
    INVITE_EXPIRED,
    INVITE_REVOKED,
    INVITE_TRIP_DELETED,
    INVALID,
    CONNECTION,
}

class TravelPlanException(
    val failure: TravelPlanFailure,
    cause: Throwable? = null,
) : Exception(cause)
