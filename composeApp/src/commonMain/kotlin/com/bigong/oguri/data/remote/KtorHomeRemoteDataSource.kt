package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.HomeImageUrlCollection
import com.bigong.oguri.data.remote.model.request.GetRecommendPeriodsRequest
import com.bigong.oguri.data.remote.model.response.AdvertisementResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import com.bigong.oguri.data.remote.model.response.RecommendPeriodResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import io.ktor.util.date.GMTDate
import kotlin.random.Random
import kotlinx.coroutines.delay

@Inject
class KtorHomeRemoteDataSource(
    private val httpClient: HttpClient,
    private val homeImageUrlCollection: HomeImageUrlCollection,
) : HomeRemoteDataSource {
    override suspend fun getRecommendPeriodResponses(): List<RecommendPeriodResponse> {
        val request: GetRecommendPeriodsRequest = GetRecommendPeriodsRequest(
            requestedAtTimestamp = GMTDate().timestamp,
            method = HttpMethod.Get.value,
        )
        val simulatedRequestUrlBuilder = URLBuilder().apply {
            takeFrom("https://api.oguri.app/v1/home/recommend-period")
            parameters.append("requestedAt", request.requestedAtTimestamp.toString())
            parameters.append("method", request.method)
        }
        if (simulatedRequestUrlBuilder.host.isEmpty() || httpClient.hashCode() == 0) {
            return emptyList()
        }

        delay(280)
        return createDummyRecommendPeriods(homeImageUrlCollection = homeImageUrlCollection)
    }

    private fun createDummyRecommendPeriods(
        homeImageUrlCollection: HomeImageUrlCollection,
    ): List<RecommendPeriodResponse> {
        fun randomHotelImageUrl(): String {
            return homeImageUrlCollection.hotelImageUrls[Random.nextInt(homeImageUrlCollection.hotelImageUrls.size)]
        }

        fun randomPlaneImageUrl(): String {
            return homeImageUrlCollection.planeImageUrls[Random.nextInt(homeImageUrlCollection.planeImageUrls.size)]
        }

        fun randomActivityImageUrl(): String {
            return homeImageUrlCollection.activityImageUrls[Random.nextInt(homeImageUrlCollection.activityImageUrls.size)]
        }

        val placeThumbnailUrl =
            "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg"

        val commonPlaces = listOf(
            PlaceResponse(
                id = 1L,
                country = "필리핀",
                city = "보라카이",
                summary = "화이트 비치 물빛이 가장 또렷해지는 시기예요",
                thumbnailUrl = placeThumbnailUrl,
            ),
            PlaceResponse(
                id = 2L,
                country = "스페인",
                city = "바르셀로나",
                summary = "가우디 건축과 바다 산책을 함께 즐기기 좋아요",
                thumbnailUrl = placeThumbnailUrl,
            ),
            PlaceResponse(
                id = 3L,
                country = "미국",
                city = "샌프란시스코",
                summary = "언덕과 바다 풍경이 가장 또렷해지는 시기예요",
                thumbnailUrl = placeThumbnailUrl,
            ),
        )

        return listOf(
            RecommendPeriodResponse(
                rank = 1,
                isSaved = false,
                startDate = "2026-02-28",
                endDate = "2026-03-04",
                holiday = listOf("삼일절"),
                dayOffCount = 2,
                totalTripCount = 5,
                places = commonPlaces,
                advertisements = listOf(
                    AdvertisementResponse(platform = "hotel", url = randomHotelImageUrl()),
                    AdvertisementResponse(platform = "plane", url = randomPlaneImageUrl()),
                    AdvertisementResponse(platform = "activity", url = randomActivityImageUrl()),
                ),
            ),
            RecommendPeriodResponse(
                rank = 2,
                isSaved = false,
                startDate = "2026-05-01",
                endDate = "2026-05-06",
                holiday = listOf("근로자의날", "어린이날"),
                dayOffCount = 2,
                totalTripCount = 6,
                places = commonPlaces,
                advertisements = listOf(
                    AdvertisementResponse(platform = "hotel", url = randomHotelImageUrl()),
                    AdvertisementResponse(platform = "plane", url = randomPlaneImageUrl()),
                    AdvertisementResponse(platform = "activity", url = randomActivityImageUrl()),
                ),
            ),
            RecommendPeriodResponse(
                rank = 3,
                isSaved = false,
                startDate = "2026-10-03",
                endDate = "2026-10-09",
                holiday = listOf("개천절", "한글날"),
                dayOffCount = 3,
                totalTripCount = 7,
                places = commonPlaces,
                advertisements = listOf(
                    AdvertisementResponse(platform = "hotel", url = randomHotelImageUrl()),
                    AdvertisementResponse(platform = "plane", url = randomPlaneImageUrl()),
                    AdvertisementResponse(platform = "activity", url = randomActivityImageUrl()),
                ),
            ),
        )
    }
}
