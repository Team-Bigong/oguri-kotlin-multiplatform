package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.request.GetPlaceDetailRequest
import com.bigong.oguri.data.remote.model.response.ExperienceResponse
import com.bigong.oguri.data.remote.model.response.PlaceDetailResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import kotlinx.coroutines.delay

@Inject
class KtorPlaceDetailRemoteDataSource(
    private val httpClient: HttpClient,
) : PlaceDetailRemoteDataSource {
    override suspend fun getPlaceDetailResponse(placeId: Long): PlaceDetailResponse {
        val request =
            GetPlaceDetailRequest(
                placeId = placeId,
                includeExperiences = true,
            )
        val simulatedRequestUrlBuilder =
            URLBuilder().apply {
                takeFrom("https://api.oguri.app/v1/places/${request.placeId}")
                parameters.append("includeExperiences", request.includeExperiences.toString())
                parameters.append("method", HttpMethod.Get.value)
            }
        if (simulatedRequestUrlBuilder.host.isEmpty() || httpClient.hashCode() == 0) {
            return createDummyPlaceDetail(placeId = placeId)
        }

        delay(220)
        return createDummyPlaceDetail(placeId = placeId)
    }

    private fun createDummyPlaceDetail(placeId: Long): PlaceDetailResponse =
        PlaceDetailResponse(
            id = placeId,
            country = "필리핀",
            city = "보라카이",
            thumbnailUrls =
                listOf(
                    "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                    "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                    "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                ),
            isSaved = false,
            description =
                "화이트 비치로 유명한 보라카이, 필리핀을 대표하는 휴양지예요. 에메랄드빛 바다와 고운 모래사장이 펼쳐져 있어서, **휴식에 집중하고 싶을 때** 특히 잘 어울리는 곳이에요. 특히 건기(11월~5월)에는 하늘이 맑고 파도도 잔잔해서, 보라카이 특유의 투명한 물빛을 더 또렷하게 만날 수 있어요.\n\n낮에는 해변에 앉아 여유를 즐기고, 저녁엔 선셋을 보며 천천히 걸어보세요. 그 순간만으로도 여행 분위기가 완성돼요. 스노클링이나 다이빙 같은 해양 액티비티도 부담 없이 즐길 수 있어서, 짧게 다녀와도 \"아, 제대로 쉬었다\" 싶은 기분이 드는 곳이에요.",
            experiences =
                listOf(
                    ExperienceResponse(
                        title = "다이빙 체험",
                        summary = "맑은 바다 속에서 형형색색 산호와 물고기를 만나요. 초보자도 안전 교육 후 참여할 수 있어 부담이 적어요.",
                        thumbnailUrl = "https://github.com/user-attachments/assets/95525288-bd76-41ad-ad4e-03ddd6c11a1d.png",
                        advertisementUrl = "https://www.klook.com/ko/",
                    ),
                    ExperienceResponse(
                        title = "만다린 베이",
                        summary = "화이트 비치와 가까운 위치의 리조트예요. 수영장과 레스토랑 시설이 좋아 휴식에 집중하기 좋아요.",
                        thumbnailUrl = "https://github.com/user-attachments/assets/02dba4ca-4589-4fc3-8e46-d15bc7b77903.png",
                        advertisementUrl = "https://www.agoda.com/",
                    ),
                    ExperienceResponse(
                        title = "스노클링 투어",
                        summary = "잔잔한 구간 중심 코스로 진행되어 초보도 편하게 참여할 수 있어요.",
                        thumbnailUrl = "https://github.com/user-attachments/assets/b50f048d-0bb8-419e-ab57-fb4b1105f5be.png",
                        advertisementUrl = "https://www.klook.com/ko/",
                    ),
                ),
            flightUrl = "https://www.skyscanner.co.kr/",
            relevantPlaces =
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
