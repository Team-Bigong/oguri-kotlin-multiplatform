package com.bigong.oguri.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@JvmRecord
data class CreateTravelPlanRequest(
    val requestId: UUID,
    val city: String,
    val timeZone: String,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    val source: TravelPlanSourceRequest?,
)

@JvmRecord
data class TravelPlanSourceRequest(
    val destinationId: Int?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)

@JvmRecord
data class RecordTravelPlanViewRequest(
    val dayNumber: Int?,
)

@JvmRecord
data class TravelVisitRequest(
    val name: String?,
    val location: String?,
    val localTime: LocalTime?,
    val note: String?,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    val googlePlaceId: String?,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(TravelPlanCommand.AddVisit::class, name = "add_visit"),
    JsonSubTypes.Type(TravelPlanCommand.EditVisit::class, name = "edit_visit"),
    JsonSubTypes.Type(TravelPlanCommand.UndoVisit::class, name = "undo_visit"),
    JsonSubTypes.Type(TravelPlanCommand.RemoveVisit::class, name = "remove_visit"),
    JsonSubTypes.Type(TravelPlanCommand.ReorderDay::class, name = "reorder_day"),
)
sealed interface TravelPlanCommand {
    val requestId: UUID
    val expectedStructureRevision: Long?

    @JvmRecord
    data class AddVisit(
        override val requestId: UUID,
        val dayNumber: Int?,
        val details: TravelVisitRequest,
        @get:JsonInclude(JsonInclude.Include.NON_NULL)
        override val expectedStructureRevision: Long?,
    ) : TravelPlanCommand

    @JvmRecord
    data class EditVisit(
        override val requestId: UUID,
        val visitId: UUID,
        val expectedVisitRevision: Long?,
        val dayNumber: Int?,
        val details: TravelVisitRequest,
        @get:JsonInclude(JsonInclude.Include.NON_NULL)
        val base: TravelVisitRequest?,
        @get:JsonInclude(JsonInclude.Include.NON_NULL)
        val baseDayNumber: Int?,
        @get:JsonInclude(JsonInclude.Include.NON_NULL)
        override val expectedStructureRevision: Long?,
    ) : TravelPlanCommand

    @JvmRecord
    data class RemoveVisit(
        override val requestId: UUID,
        val visitId: UUID,
        val expectedVisitRevision: Long?,
        @get:JsonInclude(JsonInclude.Include.NON_NULL)
        override val expectedStructureRevision: Long?,
    ) : TravelPlanCommand

    @JvmRecord
    data class UndoVisit(
        override val requestId: UUID,
        val originalRequestId: UUID,
        override val expectedStructureRevision: Long?,
    ) : TravelPlanCommand

    @JvmRecord
    data class ReorderDay(
        override val requestId: UUID,
        val dayNumber: Int?,
        val expectedOrderRevision: Long?,
        val visitIds: List<UUID>,
        @get:JsonInclude(JsonInclude.Include.NON_NULL)
        override val expectedStructureRevision: Long?,
    ) : TravelPlanCommand
}
