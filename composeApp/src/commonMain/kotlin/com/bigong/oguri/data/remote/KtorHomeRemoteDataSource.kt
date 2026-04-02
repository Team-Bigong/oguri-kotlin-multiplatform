package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.BASE_URL
import com.bigong.oguri.data.remote.model.request.ManageSavedRecommendationRequest
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
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

@Inject
class KtorHomeRemoteDataSource(
    private val httpClient: HttpClient,
    private val authRequestExecutor: AuthRequestExecutor,
) : HomeRemoteDataSource {
    override suspend fun getRecommendPeriodResponses(userCountry: String): List<RecommendPeriodResponse> {
        val requestUrl = "$BASE_URL$HOME_API_PATH"
        return authRequestExecutor.execute {
            httpClient
                .get(requestUrl) {
                    parameter(HOME_USER_COUNTRY_QUERY_NAME, userCountry)
                }.body()
        }
    }

    override suspend fun saveRecommendation(request: ManageSavedRecommendationRequest) {
        val requestUrl = "$BASE_URL$MEMBER_SAVED_RECOMMENDATIONS_API_PATH"
        authRequestExecutor.execute {
            httpClient.post(requestUrl) {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(request)
            }
        }
    }

    override suspend fun deleteRecommendation(request: ManageSavedRecommendationRequest) {
        val requestUrl = "$BASE_URL$MEMBER_SAVED_RECOMMENDATIONS_API_PATH"
        authRequestExecutor.execute {
            httpClient.delete(requestUrl) {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(request)
            }
        }
    }

    private companion object {
        private const val HOME_API_PATH = "/api/v1/home"
        private const val MEMBER_SAVED_RECOMMENDATIONS_API_PATH = "/api/v1/members/saved-recommendations"
        private const val HOME_USER_COUNTRY_QUERY_NAME = "userCountry"
    }
}
