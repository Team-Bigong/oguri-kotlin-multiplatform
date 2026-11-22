package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanFailure

internal fun TravelPlannerUiState.withRefreshedPlan(updated: TravelPlan): TravelPlannerUiState {
    val day = selectedDay.takeIf { it in 1..updated.summary.details.dates.dayCount } ?: updated.summary.details.dates.dayCount
    val dayChanged = day != selectedDay
    val removedDraft = visitDraft?.visit?.let { original -> updated.visits.none { it.id == original.id } } == true
    val changedStructure = visitDraft?.let { it.structureRevision != updated.structureRevision } == true
    val needsReload = !canRetrySave && (removedDraft || changedStructure)

    fun visible(id: String?): String? = id?.takeIf { candidate -> updated.visits.any { it.id == candidate && it.dayNumber == day } }
    return copy(
        plan = updated,
        selectedDay = day,
        inspectedVisitId = inspectedVisitId?.takeIf { id -> updated.visits.any { it.id == id } },
        selectedVisitId = visible(selectedVisitId),
        mapFocusedVisitId = visible(mapFocusedVisitId),
        dayPlaces = if (dayChanged) emptyMap() else dayPlaces,
        attemptedDayPlaceIds = if (dayChanged) emptySet() else attemptedDayPlaceIds,
        dayPlacesLanguage = if (dayChanged) null else dayPlacesLanguage,
        dayPlacesIncomplete = !dayChanged && dayPlacesIncomplete,
        isLoadingDayPlaces = !dayChanged && isLoadingDayPlaces,
        viewportRequest = if (dayChanged) viewportRequest + 1 else viewportRequest,
        needsLatestPlan = needsLatestPlan || needsReload,
        failure =
            when {
                canRetrySave -> failure
                removedDraft -> TravelPlanFailure.VISIT_REMOVED
                changedStructure -> TravelPlanFailure.CONFLICT
                else -> failure
            },
    )
}
