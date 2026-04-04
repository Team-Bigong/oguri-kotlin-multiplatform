package com.bigong.oguri.feature.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    val pagedPeriodCards = flowOf(PagingData.from(previewPeriodCards())).collectAsLazyPagingItems()
    OguriTheme {
        CalendarScreen(
            calendarUiState =
                CalendarUiState(
                    expandedPeriodId = 1L,
                    selectedDateByPeriodId = mapOf(1L to LocalDate.parse("2026-02-28")),
                ),
            pagedPeriodCards = pagedPeriodCards,
            savedStateByPeriodKey = emptyMap(),
            scrollToTopTrigger = 0,
            onLeaveDaysChanged = {},
            onPeriodFilterChanged = { _, _ -> },
            onCardClick = {},
            onSaveToggleClick = {},
            onDetailClick = {},
        )
    }
}

private fun previewPeriodCards(): List<CalendarPeriodCardUiModel> =
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
