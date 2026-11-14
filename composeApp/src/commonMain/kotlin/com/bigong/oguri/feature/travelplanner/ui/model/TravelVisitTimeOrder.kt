package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelVisit

internal fun TravelVisitDraft.hasTimeOrderMismatch(visits: List<TravelVisit>): Boolean {
    if (dayNumber == TravelPlan.UNASSIGNED_DAY) return false
    val time = toDetails().localTime ?: return false
    val dayVisits = visits.filter { it.dayNumber == dayNumber }
    val currentIndex = dayVisits.indexOfFirst { it.id == visit?.id }
    // New visits and visits moved from another date are appended to the target day.
    val index = currentIndex.takeIf { it >= 0 } ?: dayVisits.size
    val before = dayVisits.take(index).mapNotNull { it.details.localTime }.lastOrNull()
    val after = dayVisits.drop(index + 1).firstNotNullOfOrNull { it.details.localTime }
    return (before != null && time < before) || (after != null && time > after)
}
