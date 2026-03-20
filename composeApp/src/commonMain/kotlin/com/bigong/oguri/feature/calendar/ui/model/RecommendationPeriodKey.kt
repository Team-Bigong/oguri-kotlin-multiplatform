package com.bigong.oguri.feature.calendar.ui.model

import kotlinx.datetime.LocalDate

fun createRecommendationPeriodKey(
    startDate: LocalDate,
    endDate: LocalDate,
    dayOffCount: Int,
    totalTripCount: Int,
): String = "${startDate}_${endDate}_${dayOffCount}_$totalTripCount"

fun CalendarPeriodCardUiModel.toRecommendationPeriodKey(): String =
    createRecommendationPeriodKey(
        startDate = startDate,
        endDate = endDate,
        dayOffCount = dayOffCount,
        totalTripCount = totalTripCount,
    )
