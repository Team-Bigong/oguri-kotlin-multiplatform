package com.bigong.oguri.data.remote.model.request

import com.bigong.oguri.domain.model.TravelVisitCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateTravelPlanRequest(
    val requestId: String,
    val city: String,
    val timeZone: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val source: TravelPlanSourceRequest? = null,
    val cities: List<String>,
    val cityIds: Map<String, Long> = emptyMap(),
)

@Serializable
internal data class TravelPlanSourceRequest(
    val destinationId: Long?,
    val startDate: String?,
    val endDate: String?,
)

@Serializable
internal data class RecordTravelPlanViewRequest(
    val dayNumber: Int,
)

@Serializable
internal data class TravelVisitRequest(
    val name: String?,
    val location: String?,
    val localTime: String?,
    val note: String?,
    val googlePlaceId: String? = null,
    val category: TravelVisitCategory? = null,
)

@Serializable
internal sealed interface TravelPlanCommandRequest {
    @Serializable
    @SerialName("add_visit")
    data class AddVisit(
        val requestId: String,
        val dayNumber: Int,
        val details: TravelVisitRequest,
        val expectedStructureRevision: Long,
    ) : TravelPlanCommandRequest

    @Serializable
    @SerialName("edit_visit")
    data class EditVisit(
        val requestId: String,
        val visitId: String,
        val expectedVisitRevision: Long,
        val dayNumber: Int,
        val details: TravelVisitRequest,
        val expectedStructureRevision: Long,
        val base: TravelVisitRequest,
        val baseDayNumber: Int,
    ) : TravelPlanCommandRequest

    @Serializable
    @SerialName("remove_visit")
    data class RemoveVisit(
        val requestId: String,
        val visitId: String,
        val expectedVisitRevision: Long,
        val expectedStructureRevision: Long,
    ) : TravelPlanCommandRequest

    @Serializable
    @SerialName("undo_visit")
    data class UndoVisit(
        val requestId: String,
        val originalRequestId: String,
    ) : TravelPlanCommandRequest

    @Serializable
    @SerialName("reorder_day")
    data class ReorderDay(
        val requestId: String,
        val dayNumber: Int,
        val expectedOrderRevision: Long,
        val visitIds: List<String>,
        val expectedStructureRevision: Long,
    ) : TravelPlanCommandRequest
}

@Serializable
internal data class TravelPlaceSearchRequest(
    val query: String,
    val language: String,
    val city: String? = null,
)

@Serializable
internal data class TravelCityTimeZoneRequest(
    val placeId: String,
)

@Serializable
internal data class TravelInviteRequest(
    val token: String,
)

@Serializable
internal data class TravelDetailsRequest(
    val city: String,
    val title: String,
    val timeZone: String,
    val startDate: String,
    val endDate: String,
    val expectedRevision: Long,
    val confirmedUnassignedCount: Int,
    val cities: List<String> = listOf(city),
    val cityIds: Map<String, Long> = emptyMap(),
)

@Serializable
internal data class TravelOwnershipRequest(
    val targetId: String,
)

@Serializable
internal data class TravelChangesSeenRequest(
    val revision: Long,
)

@Serializable
internal data class TravelPlaceBatchRequest(
    val placeIds: List<String>,
    val language: String,
)

@kotlinx.serialization.Serializable
internal data class RecordTravelActivityRequest(
    val kind: String,
)
