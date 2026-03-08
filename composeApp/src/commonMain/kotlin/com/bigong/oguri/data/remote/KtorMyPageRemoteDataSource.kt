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
import com.bigong.oguri.data.remote.model.response.RecommendPeriodResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

@Inject
class KtorMyPageRemoteDataSource(
    private val httpClient: HttpClient,
) : MyPageRemoteDataSource {
    private var cachedMyPageResponse: MyPageResponse? = null

    override suspend fun getMyPageResponse(): MyPageResponse {
        val memberMe =
            httpClient.get("$DEBUG_BASE_URL$MEMBER_ME_API_PATH") {
                header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            }.body<MemberMeResponse>()

        val recommendPeriods =
            httpClient.get("$DEBUG_BASE_URL$HOME_API_PATH") {
                header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
                parameter(HOME_USER_COUNTRY_QUERY_NAME, DEFAULT_USER_COUNTRY)
            }.body<List<RecommendPeriodResponse>>()

        val savedRecommendations =
            recommendPeriods
                .filter { recommendPeriodResponse: RecommendPeriodResponse -> recommendPeriodResponse.saved }
                .map { recommendPeriodResponse: RecommendPeriodResponse ->
                    MyPageSelectedPeriodResponse(
                        id = recommendationId(recommendPeriodResponse),
                        startDate = recommendPeriodResponse.startDate,
                        endDate = recommendPeriodResponse.endDate,
                        totalTripCount = recommendPeriodResponse.totalTripCount,
                        dayOffCount = recommendPeriodResponse.dayOffCount,
                    )
                }

        return MyPageResponse(
            nickname = memberMe.nickname,
            remainingLeaveDays = memberMe.dayOffCount,
            preferredLeaveDays = memberMe.dayOffCount,
            selectedPeriods = savedRecommendations,
            savedPlaces = emptyList(),
        ).also { response ->
            cachedMyPageResponse = response
        }
    }

    override suspend fun updateLeaveDays(request: UpdateMyPageLeaveDaysRequest): MyPageResponse {
        val updatedDayOffCount =
            httpClient.post("$DEBUG_BASE_URL$MEMBER_DAY_OFF_API_PATH") {
                header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
                setBody(UpdateMemberDayOffRequest(dayOffCount = request.remainingLeaveDays))
            }.body<Int>()

        val currentMyPageResponse = cachedMyPageResponse ?: getMyPageResponse()

        return currentMyPageResponse.copy(
            remainingLeaveDays = updatedDayOffCount,
            preferredLeaveDays = updatedDayOffCount,
        ).also { response ->
            cachedMyPageResponse = response
        }
    }

    override suspend fun deleteSelectedPeriod(request: DeleteMyPageSelectedPeriodRequest): MyPageResponse {
        val currentMyPageResponse = cachedMyPageResponse ?: getMyPageResponse()
        val selectedPeriod =
            currentMyPageResponse.selectedPeriods.firstOrNull { selectedPeriodResponse: MyPageSelectedPeriodResponse ->
                selectedPeriodResponse.id == request.periodId
            } ?: return currentMyPageResponse

        httpClient.delete("$DEBUG_BASE_URL$MEMBER_SAVED_RECOMMENDATIONS_API_PATH") {
            header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            setBody(
                ManageSavedRecommendationRequest(
                    startDate = selectedPeriod.startDate,
                    endDate = selectedPeriod.endDate,
                    dayOffCount = selectedPeriod.dayOffCount,
                ),
            )
        }

        return currentMyPageResponse.copy(
            selectedPeriods =
                currentMyPageResponse.selectedPeriods.filterNot { selectedPeriodResponse: MyPageSelectedPeriodResponse ->
                    selectedPeriodResponse.id == request.periodId
                },
        ).also { response ->
            cachedMyPageResponse = response
        }
    }

    override suspend fun deleteSavedPlace(request: DeleteMyPageSavedPlaceRequest): MyPageResponse {
        val currentMyPageResponse = cachedMyPageResponse ?: getMyPageResponse()
        return currentMyPageResponse
    }

    private fun recommendationId(recommendPeriodResponse: RecommendPeriodResponse): Long {
        return "${recommendPeriodResponse.startDate}_${recommendPeriodResponse.endDate}_${recommendPeriodResponse.dayOffCount}".hashCode().toLong()
    }

    private companion object {
        private const val USER_ID_HEADER_NAME: String = "X-USER-ID"
        private const val DEFAULT_USER_ID: String = "GUEST"
        private const val DEFAULT_USER_COUNTRY: String = "대한민국"
        private const val HOME_USER_COUNTRY_QUERY_NAME: String = "userCountry"

        private const val MEMBER_ME_API_PATH: String = "/api/v1/members/me"
        private const val MEMBER_DAY_OFF_API_PATH: String = "/api/v1/members/day-off"
        private const val MEMBER_SAVED_RECOMMENDATIONS_API_PATH: String = "/api/v1/members/saved-recommendations"
        private const val HOME_API_PATH: String = "/api/v1/home"
    }
}
