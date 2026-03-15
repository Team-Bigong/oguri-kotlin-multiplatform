package com.bigong.oguri.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.domain.model.AdvertisementPlatform
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.RecommendPeriod
import com.bigong.oguri.feature.home.ui.model.HomeUiState
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    OguriTheme {
        HomeScreen(
            homeUiState = HomeUiState(
                isLoading = false,
                isError = false,
                selectedRank = 1,
                savedRankSet = setOf(1),
                recommendPeriods = listOf(
                    RecommendPeriod(
                        rank = 1,
                        isSaved = false,
                        startDate = LocalDate.parse("2026-02-28"),
                        endDate = LocalDate.parse("2026-03-04"),
                        holiday = listOf("삼일절"),
                        dayOffCount = 2,
                        totalTripCount = 5,
                        places = listOf(
                            Place(
                                id = 1L,
                                country = "필리핀",
                                city = "보라카이",
                                summary = "화이트 비치 물빛이 가장 또렷해지는 시기예요",
                                thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                            ),
                            Place(
                                id = 2L,
                                country = "스페인",
                                city = "바르셀로나",
                                summary = "가우디 건축과 바다 산책을 함께 즐기기 좋아요",
                                thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                            ),
                            Place(
                                id = 3L,
                                country = "미국",
                                city = "샌프란시스코",
                                summary = "언덕과 바다 풍경이 가장 또렷해지는 시기예요",
                                thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                            ),
                        ),
                        advertisements = listOf(
                            Advertisement(
                                platform = AdvertisementPlatform.AGODA,
                                url = "https://www.agoda.com/",
                            ),
                            Advertisement(
                                platform = AdvertisementPlatform.SKYSCANNER,
                                url = "https://www.skyscanner.co.kr/",
                            ),
                            Advertisement(
                                platform = AdvertisementPlatform.KLOOK,
                                url = "https://www.klook.com/ko/",
                            ),
                        ),
                    ),
                ),
            ),
            onRankSelected = {},
            onSavedChanged = {},
            onRetryClick = {},
            onAdvertisementClick = {},
            onPlaceClick = { _, _, _ -> },
            onPeriodClick = { _, _ -> },
        )
    }
}
