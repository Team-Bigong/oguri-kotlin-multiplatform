package com.bigong.oguri.core.navigation

import com.bigong.oguri.domain.model.TravelPlanSource

internal fun TravelPlanSource.toRecommendationRoute(): RouteModel? {
    val start = startDate
    val end = endDate
    if ((start == null) != (end == null) || (start != null && end != null && start > end)) return null
    val destination = destinationId
    return when {
        destination != null && destination > 0 -> RouteModel.PlaceDetail(destination, start?.toString(), end?.toString())
        destination != null -> null
        start != null && end != null -> RouteModel.PeriodDetail(start.toString(), end.toString())
        else -> null
    }
}
