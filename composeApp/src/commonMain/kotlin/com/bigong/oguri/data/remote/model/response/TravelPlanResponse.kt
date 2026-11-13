package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
internal data class TravelPlanSummaryResponse(
    val id: String,
    val city: String,
    val timeZone: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val source: TravelPlanSourceResponse? = null,
)

@Serializable
internal data class TravelPlanSourceResponse(
    val destinationId: Long?,
    val startDate: String?,
    val endDate: String?,
)

@Serializable
internal data class TravelPlanResumeResponse(
    val plan: TravelPlanSummaryResponse?,
)

@Serializable
internal data class TravelPlanResponse(
    val summary: TravelPlanSummaryResponse,
    val revision: Long,
    val dayOrderRevisions: Map<Int, Long>,
    val visits: List<TravelVisitResponse>,
    val lastViewedDay: Int? = null,
    val structureRevision: Long = 0,
)

@Serializable
internal data class TravelVisitResponse(
    val id: String,
    val dayNumber: Int,
    val name: String?,
    val location: String?,
    val localTime: String?,
    val note: String?,
    val googlePlaceId: String? = null,
    val revision: Long,
)
