package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.response.PlaceDetailResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

@Inject
class KtorPlaceDetailRemoteDataSource(
    private val httpClient: HttpClient,
) : PlaceDetailRemoteDataSource {
    override suspend fun getPlaceDetailResponse(
        placeId: Long,
        startDate: String?,
        endDate: String?,
        userCountry: String,
    ): PlaceDetailResponse {
        val requestUrl = "$DEBUG_BASE_URL$DESTINATION_API_PATH/$placeId"
        return httpClient.get(requestUrl) {
            header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            parameter(USER_COUNTRY_QUERY_NAME, userCountry)
            if (startDate != null) {
                parameter(START_DATE_QUERY_NAME, startDate)
            }
            if (endDate != null) {
                parameter(END_DATE_QUERY_NAME, endDate)
            }
        }.body()
    }

    private companion object {
        private const val DESTINATION_API_PATH: String = "/api/v1/destinations"
        private const val USER_ID_HEADER_NAME: String = "X-USER-ID"
        private const val DEFAULT_USER_ID: String = "GUEST"
        private const val USER_COUNTRY_QUERY_NAME: String = "userCountry"
        private const val START_DATE_QUERY_NAME: String = "startDate"
        private const val END_DATE_QUERY_NAME: String = "endDate"
    }
}
