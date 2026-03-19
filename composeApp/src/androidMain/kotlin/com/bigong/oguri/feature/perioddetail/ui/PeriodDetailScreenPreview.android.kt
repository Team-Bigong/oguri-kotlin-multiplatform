package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.CalendarPeriodDetail
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.feature.perioddetail.ui.model.PeriodDetailUiState
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun PeriodDetailScreenPreview() {
    val places =
        listOf(
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
                thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/b41acf66-b33b-448c-8144-d9aba0df12c0.jpeg",
            ),
        )
    val pagedPlaces = flowOf(PagingData.from(places)).collectAsLazyPagingItems()
    OguriTheme {
        PeriodDetailScreen(
            periodDetailUiState =
                PeriodDetailUiState(
                    isLoading = false,
                    isError = false,
                    isSaved = true,
                    periodDetail =
                        CalendarPeriodDetail(
                            startDate = LocalDate.parse("2026-02-28"),
                            endDate = LocalDate.parse("2026-03-04"),
                            holiday = listOf("삼일절"),
                            dayOffCount = 2,
                            totalTripCount = 5,
                            page = 0,
                            size = 10,
                            hasNext = true,
                            places = places,
                        ),
                ),
            pagedPlaces = pagedPlaces,
            onRetryClick = {},
            onBackClick = {},
            onShareClick = {},
            onSaveToggleClick = {},
            onPlaceClick = {},
        )
    }
}
