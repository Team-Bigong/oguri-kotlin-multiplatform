package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.request.DeleteMyPageSavedPlaceRequest
import com.bigong.oguri.data.remote.model.request.DeleteMyPageSelectedPeriodRequest
import com.bigong.oguri.data.remote.model.request.UpdateMyPageLeaveDaysRequest
import com.bigong.oguri.data.remote.model.response.MyPageResponse
import com.bigong.oguri.data.remote.model.response.MyPageSelectedPeriodResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay

@Inject
class KtorMyPageRemoteDataSource(
    private val httpClient: HttpClient,
) : MyPageRemoteDataSource {
    private var cachedMyPageResponse: MyPageResponse = createDummyMyPageResponse()

    override suspend fun getMyPageResponse(): MyPageResponse {
        val requestUrl = "$DEBUG_BASE_URL$MY_PAGE_API_PATH"
        val response =
            runCatching {
                val httpResponse = httpClient.get(requestUrl)
                if (httpResponse.status.isSuccess()) {
                    httpResponse.body<MyPageResponse>()
                } else {
                    null
                }
            }.getOrNull()

        if (response != null) {
            cachedMyPageResponse = response
            return response
        }

        delay(180)
        return cachedMyPageResponse
    }

    override suspend fun updateLeaveDays(request: UpdateMyPageLeaveDaysRequest): MyPageResponse {
        val requestUrl = "$DEBUG_BASE_URL$MY_PAGE_LEAVE_DAYS_API_PATH"
        val response =
            runCatching {
                val httpResponse =
                    httpClient.patch(requestUrl) {
                        setBody(request)
                    }
                if (httpResponse.status.isSuccess()) {
                    httpResponse.body<MyPageResponse>()
                } else {
                    null
                }
            }.getOrNull()

        if (response != null) {
            cachedMyPageResponse = response
            return response
        }

        delay(140)
        cachedMyPageResponse =
            cachedMyPageResponse.copy(
                remainingLeaveDays = request.remainingLeaveDays,
                preferredLeaveDays = request.preferredLeaveDays,
            )
        return cachedMyPageResponse
    }

    override suspend fun deleteSelectedPeriod(request: DeleteMyPageSelectedPeriodRequest): MyPageResponse {
        val requestUrl = "$DEBUG_BASE_URL$MY_PAGE_SELECTED_PERIODS_API_PATH/${request.periodId}"
        val response =
            runCatching {
                val httpResponse = httpClient.delete(requestUrl)
                if (httpResponse.status.isSuccess()) {
                    httpResponse.body<MyPageResponse>()
                } else {
                    null
                }
            }.getOrNull()

        if (response != null) {
            cachedMyPageResponse = response
            return response
        }

        delay(120)
        cachedMyPageResponse =
            cachedMyPageResponse.copy(
                selectedPeriods =
                    cachedMyPageResponse.selectedPeriods.filterNot { selectedPeriodResponse: MyPageSelectedPeriodResponse ->
                        selectedPeriodResponse.id == request.periodId
                    },
            )
        return cachedMyPageResponse
    }

    override suspend fun deleteSavedPlace(request: DeleteMyPageSavedPlaceRequest): MyPageResponse {
        val requestUrl = "$DEBUG_BASE_URL$MY_PAGE_SAVED_PLACES_API_PATH/${request.placeId}"
        val response =
            runCatching {
                val httpResponse = httpClient.delete(requestUrl)
                if (httpResponse.status.isSuccess()) {
                    httpResponse.body<MyPageResponse>()
                } else {
                    null
                }
            }.getOrNull()

        if (response != null) {
            cachedMyPageResponse = response
            return response
        }

        delay(120)
        cachedMyPageResponse =
            cachedMyPageResponse.copy(
                savedPlaces =
                    cachedMyPageResponse.savedPlaces.filterNot { placeResponse: PlaceResponse ->
                        placeResponse.id == request.placeId
                    },
            )
        return cachedMyPageResponse
    }

    private fun createDummyMyPageResponse(): MyPageResponse {
        return MyPageResponse(
            nickname = "똘똘한 모험가",
            remainingLeaveDays = 15,
            preferredLeaveDays = 3,
            selectedPeriods =
                listOf(
                    MyPageSelectedPeriodResponse(
                        id = 1L,
                        startDate = "2026-02-28",
                        endDate = "2026-03-04",
                        totalTripCount = 3,
                        dayOffCount = 2,
                    ),
                    MyPageSelectedPeriodResponse(
                        id = 2L,
                        startDate = "2027-02-28",
                        endDate = "2027-03-04",
                        totalTripCount = 3,
                        dayOffCount = 2,
                    ),
                ),
            savedPlaces =
                listOf(
                    PlaceResponse(
                        id = 1L,
                        country = "필리핀",
                        city = "보라카이",
                        summary = "화이트 비치 물빛이 가장 또렷해지는 시기예요",
                        thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                    ),
                    PlaceResponse(
                        id = 2L,
                        country = "스페인",
                        city = "바르셀로나",
                        summary = "가우디 건축과 바다 산책을 함께 즐기기 좋아요",
                        thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/b41acf66-b33b-448c-8144-d9aba0df12c0.jpeg",
                    ),
                    PlaceResponse(
                        id = 3L,
                        country = "미국",
                        city = "샌프란시스코",
                        summary = "언덕과 바다 풍경이 가장 또렷해지는 시기예요",
                        thumbnailUrl = "https://ozimg.flyasiana.com/temp/image/20190417/249589c9-d0eb-4e2b-a0d2-c8cc84b94e14.jpeg",
                    ),
                ),
        )
    }

    private companion object {
        private const val MY_PAGE_API_PATH: String = "/api/v1/mypage"
        private const val MY_PAGE_LEAVE_DAYS_API_PATH: String = "/api/v1/mypage/leave-days"
        private const val MY_PAGE_SELECTED_PERIODS_API_PATH: String = "/api/v1/mypage/selected-periods"
        private const val MY_PAGE_SAVED_PLACES_API_PATH: String = "/api/v1/mypage/saved-places"
    }
}
