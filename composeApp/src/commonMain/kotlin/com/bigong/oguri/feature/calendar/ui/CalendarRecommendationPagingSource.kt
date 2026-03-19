package com.bigong.oguri.feature.calendar.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bigong.oguri.domain.model.CalendarPeriod
import com.bigong.oguri.domain.model.CalendarRecommendation
import com.bigong.oguri.domain.usecase.CalculateDDayUseCase
import com.bigong.oguri.domain.usecase.GetCalendarRecommendationUseCase
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal class CalendarRecommendationPagingSource(
    private val getCalendarRecommendationUseCase: GetCalendarRecommendationUseCase,
    private val calculateDDayUseCase: CalculateDDayUseCase,
    private val selectedYear: Int,
    private val selectedMonth: Int,
    private val dayOffCount: Int?,
    private val pageSize: Int,
    private val onDayOffCountResolved: (Int) -> Unit,
    private val onPageLoaded: (List<CalendarPeriodCardUiModel>) -> Unit,
) : PagingSource<Int, CalendarPeriodCardUiModel>() {
    override fun getRefreshKey(state: PagingState<Int, CalendarPeriodCardUiModel>): Int? {
        val anchorPosition: Int = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CalendarPeriodCardUiModel> {
        val page: Int = params.key ?: INITIAL_PAGE
        return runCatching {
            getCalendarRecommendationUseCase(
                year = selectedYear,
                month = selectedMonth,
                dayOffCount = dayOffCount,
                page = page,
                size = pageSize,
            )
        }.fold(
            onSuccess = { recommendation: CalendarRecommendation ->
                onDayOffCountResolved(recommendation.dayOffCount)
                val cards: List<CalendarPeriodCardUiModel> = recommendation.toPeriodCards(calculateDDayUseCase)
                onPageLoaded(cards)
                LoadResult.Page(
                    data = cards,
                    prevKey = if (page == INITIAL_PAGE) null else page - 1,
                    nextKey = if (recommendation.hasNext) page + 1 else null,
                )
            },
            onFailure = { throwable: Throwable ->
                LoadResult.Error(throwable)
            },
        )
    }

    private companion object {
        private const val INITIAL_PAGE = 0
    }
}

private fun CalendarRecommendation.toPeriodCards(
    calculateDDayUseCase: CalculateDDayUseCase,
): List<CalendarPeriodCardUiModel> =
    periods.map { period: CalendarPeriod ->
        val todayDate =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date

        CalendarPeriodCardUiModel(
            id = period.id,
            startDate = period.startDate,
            endDate = period.endDate,
            dDay = calculateDDayUseCase(todayDate = todayDate, targetDate = period.startDate),
            isSaved = period.isSaved,
            dayOffCount = period.dayOffCount,
            totalTripCount = period.totalTripCount,
            holidayNames = period.holidayNames,
        )
    }
