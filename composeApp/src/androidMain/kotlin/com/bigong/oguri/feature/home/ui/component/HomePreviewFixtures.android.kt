package com.bigong.oguri.feature.home.ui.component

import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.RecommendPeriod
import kotlinx.datetime.LocalDate

internal val previewPlace: Place = Place(
    id = 1L,
    country = "필리핀",
    city = "보라카이",
    summary = "화이트 비치 물빛이 가장 또렷해지는 시기예요",
    thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
)

internal val previewRecommendPeriod: RecommendPeriod = RecommendPeriod(
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
