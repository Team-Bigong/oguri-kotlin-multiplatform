package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.response.PlaceDetailResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post

@Inject
class KtorPlaceDetailRemoteDataSource(
    private val httpClient: HttpClient,
    private val authRequestExecutor: AuthRequestExecutor,
) : PlaceDetailRemoteDataSource {
    override suspend fun getPlaceDetailResponse(
        placeId: Long,
        startDate: String?,
        endDate: String?,
        userCountry: String,
    ): PlaceDetailResponse {
        val requestUrl = "$DEBUG_BASE_URL$DESTINATION_API_PATH/$placeId"
        return authRequestExecutor.execute {
            httpClient
                .get(requestUrl) {
                    parameter(USER_COUNTRY_QUERY_NAME, userCountry)
                    if (startDate != null) {
                        parameter(START_DATE_QUERY_NAME, startDate)
                    }
                    if (endDate != null) {
                        parameter(END_DATE_QUERY_NAME, endDate)
                    }
                }.body()
        }
    }

    override suspend fun saveDestination(placeId: Long) {
        authRequestExecutor.execute {
            httpClient.post("$DEBUG_BASE_URL$MEMBER_SAVED_DESTINATIONS_API_PATH/$placeId")
        }
    }

    override suspend fun deleteSavedDestination(placeId: Long) {
        authRequestExecutor.execute {
            httpClient.delete("$DEBUG_BASE_URL$MEMBER_SAVED_DESTINATIONS_API_PATH/$placeId")
        }
    }

    private companion object {
        private const val DESTINATION_API_PATH = "/api/v1/destinations"
        private const val MEMBER_SAVED_DESTINATIONS_API_PATH = "/api/v1/members/saved-destinations"
        private const val USER_COUNTRY_QUERY_NAME = "userCountry"
        private const val START_DATE_QUERY_NAME = "startDate"
        private const val END_DATE_QUERY_NAME = "endDate"
    }
}
