package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.request.DeleteMyPageSavedPlaceRequest
import com.bigong.oguri.data.remote.model.request.DeleteMyPageSelectedPeriodRequest
import com.bigong.oguri.data.remote.model.request.ManageSavedRecommendationRequest
import com.bigong.oguri.data.remote.model.request.UpdateMemberDayOffRequest
import com.bigong.oguri.data.remote.model.request.UpdateMyPageLeaveDaysRequest
import com.bigong.oguri.data.remote.model.response.MemberMeResponse
import com.bigong.oguri.data.remote.model.response.MyPageResponse
import com.bigong.oguri.data.remote.model.response.MyPageSelectedPeriodResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

@Inject
class KtorMyPageRemoteDataSource(
    private val httpClient: HttpClient,
    private val authRequestExecutor: AuthRequestExecutor,
) : MyPageRemoteDataSource {
    private var cachedMyPageResponse: MyPageResponse? = null

    override suspend fun getMyPageResponse(): MyPageResponse {
        val memberMeResponse =
            authRequestExecutor.execute {
                httpClient
                    .get("$DEBUG_BASE_URL$MEMBER_ME_API_PATH") {
                    }.body<MemberMeResponse>()
            }

        return memberMeResponse.toMyPageResponse().also { response -> cachedMyPageResponse = response }
    }

    override suspend fun updateLeaveDays(request: UpdateMyPageLeaveDaysRequest): MyPageResponse {
        authRequestExecutor.execute {
            httpClient.post("$DEBUG_BASE_URL$MEMBER_DAY_OFF_API_PATH") {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    UpdateMemberDayOffRequest(
                        preferredDayOff = request.preferredLeaveDays,
                        remainingDayOff = request.remainingLeaveDays,
                    ),
                )
            }
        }

        val currentMyPageResponse = cachedMyPageResponse ?: getMyPageResponse()

        return currentMyPageResponse
            .copy(
                remainingLeaveDays = request.remainingLeaveDays,
                preferredLeaveDays = request.preferredLeaveDays,
            ).also { response ->
                cachedMyPageResponse = response
            }
    }

    override suspend fun deleteSelectedPeriod(request: DeleteMyPageSelectedPeriodRequest): MyPageResponse {
        val currentMyPageResponse = cachedMyPageResponse ?: getMyPageResponse()
        val selectedPeriod =
            currentMyPageResponse.selectedPeriods.firstOrNull { selectedPeriodResponse ->
                selectedPeriodResponse.id == request.periodId
            } ?: return currentMyPageResponse

        authRequestExecutor.execute {
            httpClient.delete("$DEBUG_BASE_URL$MEMBER_SAVED_RECOMMENDATIONS_API_PATH") {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    ManageSavedRecommendationRequest(
                        startDate = selectedPeriod.startDate,
                        endDate = selectedPeriod.endDate,
                        dayOffCount = selectedPeriod.dayOffCount,
                        totalTripCount = selectedPeriod.totalTripCount,
                    ),
                )
            }
        }

        return currentMyPageResponse
            .copy(
                selectedPeriods =
                    currentMyPageResponse.selectedPeriods.filterNot { selectedPeriodResponse ->
                        selectedPeriodResponse.id == request.periodId
                    },
            ).also { response ->
                cachedMyPageResponse = response
            }
    }

    override suspend fun deleteSavedPlace(request: DeleteMyPageSavedPlaceRequest): MyPageResponse {
        authRequestExecutor.execute {
            httpClient.delete("$DEBUG_BASE_URL$MEMBER_SAVED_DESTINATIONS_API_PATH/${request.placeId}")
        }

        val currentMyPageResponse = cachedMyPageResponse ?: getMyPageResponse()
        return currentMyPageResponse
            .copy(
                savedPlaces = currentMyPageResponse.savedPlaces.filterNot { placeResponse -> placeResponse.id == request.placeId },
            ).also { response ->
                cachedMyPageResponse = response
            }
    }

    override suspend fun withdraw() {
        authRequestExecutor.execute {
            httpClient.delete("$DEBUG_BASE_URL$MEMBER_ME_API_PATH")
        }
        cachedMyPageResponse = null
    }

    private fun recommendationId(
        startDate: String,
        endDate: String,
        dayOffCount: Int,
        totalTripCount: Int,
        index: Int,
    ): Long {
        val key = "${startDate}_${endDate}_${dayOffCount}_$totalTripCount"
        val primaryHash = key.hashCode().toLong() and 0xffffffffL
        val secondaryHash = key.reversed().hashCode().toLong() and 0xffffffffL
        val uniqueLowerBits = (secondaryHash xor index.toLong()) and 0xffffffffL
        return (primaryHash shl 32) or uniqueLowerBits
    }

    private fun MemberMeResponse.toMyPageResponse(): MyPageResponse {
        val selectedPeriodResponses =
            savedPeriods.mapIndexed { index, savedPeriodResponse ->
                MyPageSelectedPeriodResponse(
                    id =
                        recommendationId(
                            startDate = savedPeriodResponse.startDate,
                            endDate = savedPeriodResponse.endDate,
                            dayOffCount = savedPeriodResponse.dayOffCount,
                            totalTripCount = savedPeriodResponse.totalTripCount,
                            index = index,
                        ),
                    startDate = savedPeriodResponse.startDate,
                    endDate = savedPeriodResponse.endDate,
                    totalTripCount = savedPeriodResponse.totalTripCount,
                    dayOffCount = savedPeriodResponse.dayOffCount,
                )
            }

        return MyPageResponse(
            nickname = nickname,
            remainingLeaveDays = remainingDayOff,
            preferredLeaveDays = preferredDayOff,
            selectedPeriods = selectedPeriodResponses,
            savedPlaces = savedPlaces,
        )
    }

    private companion object {
        private const val MEMBER_ME_API_PATH = "/api/v1/members/me"
        private const val MEMBER_DAY_OFF_API_PATH = "/api/v1/members/day-off"
        private const val MEMBER_SAVED_RECOMMENDATIONS_API_PATH = "/api/v1/members/saved-recommendations"
        private const val MEMBER_SAVED_DESTINATIONS_API_PATH = "/api/v1/members/saved-destinations"
    }
}
