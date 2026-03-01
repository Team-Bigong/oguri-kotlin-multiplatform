package com.bigong.oguri.data.remote

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
import kotlinx.coroutines.delay
import kotlin.random.Random

@Inject
class KtorHomeRemoteDataSource(
    private val httpClient: HttpClient,
) : HomeRemoteDataSource {
    override suspend fun getRecommendPeriodResponses(): List<RecommendPeriodResponse> {
        val request =
            GetRecommendPeriodsRequest(
                requestedAtTimestamp = GMTDate().timestamp,
                method = HttpMethod.Get.value,
            )
        val simulatedRequestUrlBuilder =
            URLBuilder().apply {
                takeFrom("https://api.oguri.app/v1/home/recommend-period")
                parameters.append("requestedAt", request.requestedAtTimestamp.toString())
                parameters.append("method", request.method)
            }
        if (simulatedRequestUrlBuilder.host.isEmpty() || httpClient.hashCode() == 0) {
            return emptyList()
        }

        delay(280)
        return createDummyRecommendPeriods()
    }

    private fun createDummyRecommendPeriods(): List<RecommendPeriodResponse> {
        val commonPlaces =
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
                advertisements =
                    listOf(
                        randomDestinationAdvertisementResponse(platform = ADVERTISEMENT_PLATFORM_AGODA),
                        randomDestinationAdvertisementResponse(platform = ADVERTISEMENT_PLATFORM_SKYSCANNER),
                        randomDestinationAdvertisementResponse(platform = ADVERTISEMENT_PLATFORM_KLOOK),
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
                advertisements =
                    listOf(
                        randomDestinationAdvertisementResponse(platform = ADVERTISEMENT_PLATFORM_AGODA),
                        randomDestinationAdvertisementResponse(platform = ADVERTISEMENT_PLATFORM_SKYSCANNER),
                        randomDestinationAdvertisementResponse(platform = ADVERTISEMENT_PLATFORM_KLOOK),
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
                advertisements =
                    listOf(
                        randomDestinationAdvertisementResponse(platform = ADVERTISEMENT_PLATFORM_AGODA),
                        randomDestinationAdvertisementResponse(platform = ADVERTISEMENT_PLATFORM_SKYSCANNER),
                        randomDestinationAdvertisementResponse(platform = ADVERTISEMENT_PLATFORM_KLOOK),
                    ),
            ),
        )
    }

    private fun randomDestinationAdvertisementResponse(platform: String): AdvertisementResponse {
        val destinationUrls: List<String> =
            when (platform) {
                ADVERTISEMENT_PLATFORM_AGODA -> HOTEL_DESTINATION_URLS
                ADVERTISEMENT_PLATFORM_SKYSCANNER -> PLANE_DESTINATION_URLS
                else -> ACTIVITY_DESTINATION_URLS
            }
        return AdvertisementResponse(
            platform = platform,
            url = destinationUrls[Random.nextInt(destinationUrls.size)],
        )
    }
}

private const val ADVERTISEMENT_PLATFORM_AGODA: String = "agoda"
private const val ADVERTISEMENT_PLATFORM_SKYSCANNER: String = "skyscanner"
private const val ADVERTISEMENT_PLATFORM_KLOOK: String = "klook"

private val HOTEL_DESTINATION_URLS: List<String> =
    listOf(
        "https://www.agoda.com/",
        "https://www.booking.com/",
    )

private val PLANE_DESTINATION_URLS: List<String> =
    listOf(
        "https://www.skyscanner.co.kr/",
        "https://www.kayak.com/flights",
    )

private val ACTIVITY_DESTINATION_URLS: List<String> =
    listOf(
        "https://www.klook.com/ko/",
        "https://www.getyourguide.com/",
    )
