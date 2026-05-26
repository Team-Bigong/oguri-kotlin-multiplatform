package com.bigong.oguri.core.ui

import com.bigong.oguri.domain.model.Experience
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.PlaceDetail
import com.bigong.oguri.domain.model.PlaceDetailExchangeRateInformation
import com.bigong.oguri.domain.model.PlaceDetailRecommendPeriod
import com.bigong.oguri.domain.model.PlaceDetailTravelInformation
import com.bigong.oguri.domain.model.RecommendPeriod
import kotlinx.datetime.LocalDate

private const val PREVIEW_ACTIVITY_THUMBNAIL_URL =
    "https://firebasestorage.googleapis.com/v0/b/oguri-af89b.firebasestorage.app/o/" +
        "drawable%2Fimg_activity_2.jpg?alt=media&token=d537d230-b208-4a74-8e92-137b09b36945"

internal val previewPlace =
    Place(
        id = 1L,
        country = "필리핀",
        city = "보라카이",
        summary = "화이트 비치 물빛이 가장 또렷해지는 시기예요",
        thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
    )

internal val previewRecommendPeriod =
    RecommendPeriod(
        rank = 1,
        isSaved = false,
        startDate = LocalDate.parse("2026-02-28"),
        endDate = LocalDate.parse("2026-03-04"),
        holiday = listOf("삼일절"),
        dayOffCount = 2,
        totalTripCount = 5,
        places = listOf(previewPlace),
        advertisements = emptyList(),
    )

internal val previewPlaceDetail =
    PlaceDetail(
        id = 1L,
        country = "필리핀",
        city = "보라카이",
        thumbnailUrls =
            listOf(
                "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/b41acf66-b33b-448c-8144-d9aba0df12c0.jpeg",
            ),
        isSaved = false,
        travelInformation =
            PlaceDetailTravelInformation(
                exchangeRateInformation =
                    PlaceDetailExchangeRateInformation(
                        koreanWonAmount = 2400,
                        currencyUnit = 1,
                        currencyCode = "USD",
                        date = "2026-04-08",
                    ),
                relativeCostIndex = 2.46,
                averageTemperature = 16,
                averagePrecipitation = 70.5,
                recommendPeriod =
                    PlaceDetailRecommendPeriod(
                        startMonth = 6,
                        endMonth = 8,
                    ),
            ),
        description = "화이트 비치로 유명한 보라카이, **휴식에 집중하고 싶을 때** 가장 잘 어울리는 곳이에요.",
        experiences =
            listOf(
                Experience(
                    title = "다이빙 체험",
                    summary = "맑은 바다 속 형형색색 풍경을 만날 수 있어요.",
                    thumbnailUrl = PREVIEW_ACTIVITY_THUMBNAIL_URL,
                    advertisementUrl = "https://www.klook.com/ko/",
                ),
            ),
        flightUrl = "https://www.skyscanner.co.kr/",
        relevantPlaces =
            listOf(
                previewPlace,
                previewPlace.copy(id = 2L, city = "바르셀로나", country = "스페인"),
                previewPlace.copy(id = 3L, city = "샌프란시스코", country = "미국"),
            ),
    )
