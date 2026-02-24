package com.bigong.oguri.feature.calendar.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.navigation.RouteModel
import com.bigong.oguri.feature.calendar.ui.StrategyCalendarRoute

object CalendarNavGraph {
    private const val DefaultStrategyCalendarYear: Int = 2026

    fun register(
        navGraphBuilder: NavGraphBuilder,
        appGraph: AppGraph,
    ) {
        navGraphBuilder.composable<RouteModel.StrategyCalendar> {
            StrategyCalendarRoute(
                annualLeaveStrategyRepository = appGraph.annualLeaveStrategyRepository,
                year = DefaultStrategyCalendarYear,
            )
        }
    }
}
