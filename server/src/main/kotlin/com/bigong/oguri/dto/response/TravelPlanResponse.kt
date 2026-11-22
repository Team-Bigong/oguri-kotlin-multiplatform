package com.bigong.oguri.dto.response

import com.bigong.oguri.domain.TravelVisitCategory
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class TravelPlanSummaryResponse(
    val id: UUID,
    val city: String,
    val timeZone: String,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val source: TravelPlanSourceResponse?,
    val memberCount: Int? = null,
    val cities: List<String> = listOf(city),
    val catalogCities: List<TravelCitySuggestion> = emptyList(),
)

data class TravelPlanSourceResponse(
    val destinationId: Int?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)

data class TravelPlanResumeResponse(
    val plan: TravelPlanSummaryResponse?,
)

data class TravelPlanResponse(
    val summary: TravelPlanSummaryResponse,
    val revision: Long,
    val structureRevision: Long,
    val dayOrderRevisions: Map<Int, Long>,
    val visits: List<TravelVisitResponse>,
    val lastViewedDay: Int?,
    val lastModifiedAt: OffsetDateTime? = null,
)

data class TravelVisitResponse(
    val id: UUID,
    val dayNumber: Int,
    val name: String?,
    val location: String?,
    val localTime: String?,
    val note: String?,
    val googlePlaceId: String?,
    val category: TravelVisitCategory? = null,
    val revision: Long,
)

data class TravelPlanErrorResponse(
    val code: String,
)
