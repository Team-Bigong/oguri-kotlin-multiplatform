package com.bigong.oguri.data.repository

import com.bigong.oguri.data.remote.AuthSessionChangedException
import com.bigong.oguri.data.remote.KtorTravelPlanRemoteDataSource
import com.bigong.oguri.data.remote.model.request.CreateTravelPlanRequest
import com.bigong.oguri.data.remote.model.request.TravelDetailsRequest
import com.bigong.oguri.data.remote.model.request.TravelPlanCommandRequest
import com.bigong.oguri.data.remote.model.request.TravelPlanSourceRequest
import com.bigong.oguri.data.remote.model.request.TravelVisitRequest
import com.bigong.oguri.data.remote.model.response.TravelPlanErrorResponse
import com.bigong.oguri.data.remote.model.response.TravelPlanResponse
import com.bigong.oguri.data.remote.model.response.TravelPlanSummaryResponse
import com.bigong.oguri.domain.model.TravelChange
import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelDayPlaces
import com.bigong.oguri.domain.model.TravelInvitePreview
import com.bigong.oguri.domain.model.TravelMember
import com.bigong.oguri.domain.model.TravelMembers
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanException
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.domain.model.TravelRecentChanges
import com.bigong.oguri.domain.model.TravelUsageActivity
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitChange
import com.bigong.oguri.domain.model.TravelVisitDetails
import com.bigong.oguri.domain.repository.TravelCollaborationRepository
import com.bigong.oguri.domain.repository.TravelPlaceRepository
import com.bigong.oguri.domain.repository.TravelPlanRepository
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

@Inject
class DefaultTravelPlanRepository internal constructor(
    private val remote: KtorTravelPlanRemoteDataSource,
) : TravelPlanRepository,
    TravelPlaceRepository,
    TravelCollaborationRepository {
    override suspend fun recordActivity(
        planId: String,
        kind: TravelUsageActivity,
    ): Unit = request { remote.recordActivity(planId, kind) }

    override suspend fun list(): List<TravelPlanSummary> = request { remote.list().map { it.toDomain() } }

    override suspend fun resume(): TravelPlanSummary? = request { remote.resume().plan?.toDomain() }

    override suspend fun recordView(
        planId: String,
        dayNumber: Int,
    ): Unit = request { remote.recordView(planId, dayNumber) }

    override suspend fun create(
        requestId: String,
        details: TravelPlanDetails,
    ): TravelPlanSummary =
        request {
            remote
                .create(
                    CreateTravelPlanRequest(
                        requestId = requestId,
                        city = details.city,
                        timeZone = details.timeZone.id,
                        title = details.title,
                        startDate = details.dates.start.toString(),
                        endDate = details.dates.endInclusive.toString(),
                        source =
                            details.source?.let {
                                TravelPlanSourceRequest(it.destinationId, it.startDate?.toString(), it.endDate?.toString())
                            },
                    ),
                ).toDomain()
        }

    override suspend fun get(planId: String): TravelPlan = request { remote.get(planId).toDomain() }

    override suspend fun changeVisit(
        planId: String,
        requestId: String,
        change: TravelVisitChange,
    ): TravelPlan =
        request {
            val body =
                when (change) {
                    is TravelVisitChange.Add ->
                        TravelPlanCommandRequest.AddVisit(
                            requestId,
                            change.dayNumber,
                            change.details.toRequest(),
                            change.structureRevision,
                        )
                    is TravelVisitChange.Edit ->
                        TravelPlanCommandRequest.EditVisit(
                            requestId,
                            change.visit.id,
                            change.visit.revision,
                            change.dayNumber,
                            change.details.toRequest(),
                            change.structureRevision,
                            change.visit.details.toRequest(),
                            change.visit.dayNumber,
                        )
                    is TravelVisitChange.Remove ->
                        TravelPlanCommandRequest.RemoveVisit(
                            requestId,
                            change.visit.id,
                            change.visit.revision,
                            change.structureRevision,
                        )
                    is TravelVisitChange.Undo ->
                        TravelPlanCommandRequest.UndoVisit(requestId, change.originalRequestId)
                    is TravelVisitChange.Reorder ->
                        TravelPlanCommandRequest.ReorderDay(
                            requestId,
                            change.dayNumber,
                            change.expectedOrderRevision,
                            change.visitIds,
                            change.structureRevision,
                        )
                }
            remote.changeVisit(planId, body).toDomain()
        }

    override suspend fun cities(
        query: String,
        language: String,
    ): List<TravelCitySuggestion> = request { remote.cities(query, language).map { TravelCitySuggestion(it.id, it.label) } }

    override suspend fun cityTimeZone(placeId: String): String? = request { remote.cityTimeZone(placeId).timeZone }

    override suspend fun search(
        planId: String,
        query: String,
        language: String,
    ): List<TravelPlace> = request { remote.searchPlaces(planId, query, language).map { it.toDomain() } }

    override suspend fun forPlan(
        planId: String,
        language: String,
        placeIds: List<String>,
    ): TravelDayPlaces =
        request {
            val response = remote.planPlaces(planId, language, placeIds)
            TravelDayPlaces(response.places.associate { it.id to it.toDomain() }, response.incomplete)
        }

    override suspend fun forDay(
        planId: String,
        dayNumber: Int,
        language: String,
    ): TravelDayPlaces =
        request {
            val response = remote.dayPlaces(planId, dayNumber, language)
            TravelDayPlaces(response.places.associate { it.id to it.toDomain() }, response.incomplete)
        }

    override suspend fun members(planId: String): TravelMembers =
        request {
            val response = remote.members(planId)
            TravelMembers(
                response.currentMemberId,
                response.members.map { TravelMember(it.id, it.nickname, it.owner) },
                response.ownershipTarget,
            )
        }

    override suspend fun issue(planId: String): String = request { remote.issueInvite(planId).token }

    override suspend fun revoke(planId: String): Unit = request { remote.revokeInvite(planId) }

    override suspend fun remove(
        planId: String,
        memberId: String,
    ): Unit = request { remote.removeMember(planId, memberId) }

    override suspend fun leave(planId: String): Unit = request { remote.leave(planId) }

    override suspend fun delete(planId: String): Unit = request { remote.deletePlan(planId) }

    override suspend fun updateDetails(
        planId: String,
        details: TravelPlanDetails,
        revision: Long,
        unassignedCount: Int,
    ): Unit =
        request {
            remote.updateDetails(
                planId,
                TravelDetailsRequest(
                    details.city,
                    details.title,
                    details.timeZone.id,
                    details.dates.start.toString(),
                    details.dates.endInclusive.toString(),
                    revision,
                    unassignedCount,
                ),
            )
        }

    override suspend fun offerOwnership(
        planId: String,
        targetId: String,
    ): Unit = request { remote.offerOwnership(planId, targetId) }

    override suspend fun acceptOwnership(planId: String): Unit = request { remote.acceptOwnership(planId) }

    override suspend fun recentChanges(planId: String): TravelRecentChanges =
        request {
            val response = remote.recentChanges(planId)
            TravelRecentChanges(
                throughRevision = response.throughRevision,
                changes =
                    response.changes.map {
                        TravelChange(
                            it.revision,
                            it.dayNumber,
                            it.kind,
                            it.visitId,
                            it.occurredAt?.let(kotlin.time.Instant::parse),
                            it.currentDayNumber,
                            it.visitName,
                        )
                    },
                dates =
                    response.startDate?.let { start ->
                        response.endDate?.let { end -> TravelDates(LocalDate.parse(start), LocalDate.parse(end)) }
                    },
                timeZone = response.timeZone?.let(TimeZone::of),
            )
        }

    override suspend fun markChangesSeen(
        planId: String,
        revision: Long,
    ): Unit = request { remote.markChangesSeen(planId, revision) }

    override suspend fun preview(token: String): TravelInvitePreview =
        request {
            val response = remote.previewInvite(token)
            TravelInvitePreview(response.title, LocalDate.parse(response.startDate), LocalDate.parse(response.endDate), response.invitedBy)
        }

    override suspend fun join(token: String): String = request { remote.joinInvite(token).planId }

    private suspend fun <Result> request(block: suspend () -> Result): Result =
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: AuthSessionChangedException) {
            throw TravelPlanException(TravelPlanFailure.SIGN_IN_REQUIRED, exception)
        } catch (exception: ClientRequestException) {
            val failure =
                when (exception.response.status) {
                    HttpStatusCode.Unauthorized -> TravelPlanFailure.SIGN_IN_REQUIRED
                    HttpStatusCode.NotFound, HttpStatusCode.Forbidden -> TravelPlanFailure.UNAVAILABLE
                    HttpStatusCode.Gone ->
                        when (errorCode(exception)) {
                            "INVITE_EXPIRED" -> TravelPlanFailure.INVITE_EXPIRED
                            "INVITE_REVOKED" -> TravelPlanFailure.INVITE_REVOKED
                            "INVITE_TRIP_DELETED" -> TravelPlanFailure.INVITE_TRIP_DELETED
                            else -> TravelPlanFailure.UNAVAILABLE
                        }
                    HttpStatusCode.Conflict ->
                        when (errorCode(exception)) {
                            "GOOGLE_VISIT_LIMIT" -> TravelPlanFailure.VISIT_LIMIT
                            "UNDO_NO_LONGER_AVAILABLE" -> TravelPlanFailure.UNDO_UNAVAILABLE
                            "VISIT_REMOVED" -> TravelPlanFailure.VISIT_REMOVED
                            else -> TravelPlanFailure.CONFLICT
                        }
                    HttpStatusCode.TooManyRequests -> TravelPlanFailure.RATE_LIMIT
                    HttpStatusCode.BadRequest -> TravelPlanFailure.INVALID
                    else -> TravelPlanFailure.CONNECTION
                }
            throw TravelPlanException(failure, exception)
        } catch (exception: Exception) {
            throw TravelPlanException(TravelPlanFailure.CONNECTION, exception)
        }

    private suspend fun errorCode(exception: ClientRequestException): String? =
        try {
            exception.response.body<TravelPlanErrorResponse>().code
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }

    private fun TravelPlanSummaryResponse.toDomain(): TravelPlanSummary =
        TravelPlanSummary(
            id = id,
            details =
                TravelPlanDetails(
                    city,
                    TimeZone.of(timeZone),
                    title,
                    TravelDates(LocalDate.parse(startDate), LocalDate.parse(endDate)),
                    source?.let {
                        TravelPlanSource(it.destinationId, it.startDate?.let(LocalDate::parse), it.endDate?.let(LocalDate::parse))
                    },
                ),
        )

    private fun TravelPlanResponse.toDomain(): TravelPlan =
        TravelPlan(
            summary = summary.toDomain(),
            revision = revision,
            dayOrderRevisions = dayOrderRevisions,
            lastViewedDay = lastViewedDay,
            structureRevision = structureRevision,
            lastModifiedAt = lastModifiedAt?.let(kotlin.time.Instant::parse),
            visits =
                visits.map { visit ->
                    TravelVisit(
                        id = visit.id,
                        dayNumber = visit.dayNumber,
                        details =
                            TravelVisitDetails(
                                name = visit.name,
                                location = visit.location,
                                localTime = visit.localTime?.let(LocalTime::parse),
                                note = visit.note,
                                googlePlaceId = visit.googlePlaceId,
                            ),
                        revision = visit.revision,
                    )
                },
        )

    private fun TravelVisitDetails.toRequest(): TravelVisitRequest =
        TravelVisitRequest(name, location, localTime?.toString(), note, googlePlaceId)
}
