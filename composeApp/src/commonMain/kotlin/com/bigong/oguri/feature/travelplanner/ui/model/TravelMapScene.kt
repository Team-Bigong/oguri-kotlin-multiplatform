package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelVisit

data class TravelMapMarker(
    val id: String,
    val coordinates: TravelCoordinates,
    val label: String?,
    val selected: Boolean,
)

data class TravelMapLine(
    val points: List<TravelCoordinates>,
)

data class TravelMapScene(
    val markers: List<TravelMapMarker>,
    val lines: List<TravelMapLine>,
    val cityCenter: TravelCoordinates?,
)

internal fun travelMapScene(
    dayNumber: Int,
    visits: List<TravelVisit>,
    places: Map<String, TravelPlace>,
    selectedVisitId: String?,
    cityCenter: TravelCoordinates?,
): TravelMapScene {
    val coordinates = visits.map { places[it.details.googlePlaceId]?.coordinates }
    val numbered = dayNumber != TravelPlan.UNASSIGNED_DAY
    val markers =
        visits.mapIndexedNotNull { index, visit ->
            coordinates[index]?.let {
                TravelMapMarker(visit.id, it, if (numbered) (index + 1).toString() else null, visit.id == selectedVisitId)
            }
        }
    val lines =
        if (numbered) {
            coordinates.zipWithNext().mapNotNull { (from, to) ->
                if (from == null || to == null || from == to) null else TravelMapLine(listOf(from, to))
            }
        } else {
            emptyList()
        }
    return TravelMapScene(markers, lines, cityCenter)
}
