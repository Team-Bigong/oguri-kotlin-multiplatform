package com.bigong.oguri.feature.perioddetail.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bigong.oguri.domain.model.CalendarPeriodDetail
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.usecase.GetCalendarPeriodDetailUseCase

internal class PeriodDetailPlacePagingSource(
    private val getCalendarPeriodDetailUseCase: GetCalendarPeriodDetailUseCase,
    private val startDate: String,
    private val endDate: String,
    private val pageSize: Int,
    private val onFirstPageResolved: (CalendarPeriodDetail) -> Unit,
) : PagingSource<Int, Place>() {
    override fun getRefreshKey(state: PagingState<Int, Place>): Int? {
        val anchorPosition: Int = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Place> {
        val page: Int = params.key ?: INITIAL_PAGE
        return runCatching {
            getCalendarPeriodDetailUseCase(
                startDate = startDate,
                endDate = endDate,
                page = page,
                size = pageSize,
            )
        }.fold(
            onSuccess = { detail: CalendarPeriodDetail ->
                if (page == INITIAL_PAGE) {
                    onFirstPageResolved(detail)
                }
                LoadResult.Page(
                    data = detail.places,
                    prevKey = if (page == INITIAL_PAGE) null else page - 1,
                    nextKey = if (detail.hasNext) page + 1 else null,
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
