package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.request.ManageSavedRecommendationRequest
import com.bigong.oguri.data.remote.model.response.RecommendPeriodResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.post

@Inject
class KtorHomeRemoteDataSource(
    private val httpClient: HttpClient,
) : HomeRemoteDataSource {
    override suspend fun getRecommendPeriodResponses(userCountry: String): List<RecommendPeriodResponse> {
        val requestUrl = "$DEBUG_BASE_URL$HOME_API_PATH"
        return httpClient.get(requestUrl) {
            header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            parameter(HOME_USER_COUNTRY_QUERY_NAME, userCountry)
        }.body()
    }

    override suspend fun saveRecommendation(request: ManageSavedRecommendationRequest) {
        val requestUrl = "$DEBUG_BASE_URL$MEMBER_SAVED_RECOMMENDATIONS_API_PATH"
        httpClient.post(requestUrl) {
            header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            setBody(request)
        }
    }

    override suspend fun deleteRecommendation(request: ManageSavedRecommendationRequest) {
        val requestUrl = "$DEBUG_BASE_URL$MEMBER_SAVED_RECOMMENDATIONS_API_PATH"
        httpClient.delete(requestUrl) {
            header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            setBody(request)
        }
    }

    private companion object {
        private const val HOME_API_PATH: String = "/api/v1/home"
        private const val MEMBER_SAVED_RECOMMENDATIONS_API_PATH: String = "/api/v1/members/saved-recommendations"
        private const val USER_ID_HEADER_NAME: String = "X-USER-ID"
        private const val DEFAULT_USER_ID: String = "GUEST"
        private const val HOME_USER_COUNTRY_QUERY_NAME: String = "userCountry"
    }
}
