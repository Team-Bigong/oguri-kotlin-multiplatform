package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun CalendarRecommendationSectionPreview() {
    val cards =
        listOf(
            CalendarPeriodCardUiModel(
                id = 1L,
                startDate = LocalDate.parse("2026-02-28"),
                endDate = LocalDate.parse("2026-03-04"),
                dDay = 10,
                isSaved = true,
                dayOffCount = 2,
                totalTripCount = 5,
                holidayNames = listOf("삼일절", "대체휴일"),
                holidays = emptyList(),
            ),
            CalendarPeriodCardUiModel(
                id = 2L,
                startDate = LocalDate.parse("2026-03-07"),
                endDate = LocalDate.parse("2026-03-10"),
                dDay = 17,
                isSaved = false,
                dayOffCount = 2,
                totalTripCount = 4,
                holidayNames = listOf("주말"),
                holidays = emptyList(),
            ),
        )
    val pagedPeriodCards = flowOf(PagingData.from(cards)).collectAsLazyPagingItems()

    OguriTheme {
        LazyColumn {
            calendarRecommendationSection(
                pagedPeriodCards = pagedPeriodCards,
                savedStateByPeriodKey = emptyMap(),
                expandedPeriodId = 1L,
                isLoadingNextPage = false,
                listViewportBottomInWindow = 2_000f,
                onCardClick = {},
                onSaveToggleClick = {},
                onDetailClick = {},
                onRequestScrollBy = {},
            )
        }
    }
}
