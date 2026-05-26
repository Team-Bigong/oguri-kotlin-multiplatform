package com.bigong.oguri.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.Place

@Preview(showBackground = true)
@Composable
private fun WeeklyTopPlaceCarouselPreview() {
    val previewThumbnailUrl =
        "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg"

    OguriTheme {
        WeeklyTopPlaceCarousel(
            places =
                listOf(
                    Place(
                        id = 1L,
                        country = "필리핀",
                        city = "보라카이",
                        summary = "화이트 비치 물빛이 가장 또렷해지는 시기예요",
                        thumbnailUrl = previewThumbnailUrl,
                    ),
                    Place(
                        id = 2L,
                        country = "스페인",
                        city = "바르셀로나",
                        summary = "가우디 건축과 바다 산책을 함께 즐기기 좋아요",
                        thumbnailUrl = previewThumbnailUrl,
                    ),
                    Place(
                        id = 3L,
                        country = "미국",
                        city = "샌프란시스코",
                        summary = "언덕과 바다 풍경이 가장 또렷해지는 시기예요",
                        thumbnailUrl = previewThumbnailUrl,
                    ),
                    Place(
                        id = 4L,
                        country = "호주",
                        city = "시드니",
                        summary = "바다와 도심을 함께 즐기기 좋아요",
                        thumbnailUrl = previewThumbnailUrl,
                    ),
                ),
        )
    }
}
