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
    val dayNumber: Int? = null,
) {
    val colorArgb: Int
        get() = if (selected) 0xff244da5.toInt() else travelDayColor(dayNumber)
}

internal fun travelDayColor(dayNumber: Int?): Int =
    when (dayNumber) {
        null -> 0xff007d69.toInt()
        0 -> 0xff616161.toInt()
        else ->
            when ((dayNumber - 1) % 5) {
                0 -> 0xff007d69.toInt()
                1 -> 0xff244da5.toInt()
                2 -> 0xff9c3e00.toInt()
                3 -> 0xff78429c.toInt()
                else -> 0xffa12655.toInt()
            }
    }

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

internal fun travelOverviewScene(
    visits: List<TravelVisit>,
    places: Map<String, TravelPlace>,
    cityCenter: TravelCoordinates?,
): TravelMapScene =
    TravelMapScene(
        markers =
            visits.mapNotNull { visit ->
                places[visit.details.googlePlaceId]?.coordinates?.let { coordinates ->
                    TravelMapMarker(
                        visit.id,
                        coordinates,
                        if (visit.dayNumber == TravelPlan.UNASSIGNED_DAY) "–" else visit.dayNumber.toString(),
                        selected = false,
                        dayNumber = visit.dayNumber,
                    )
                }
            },
        lines = emptyList(),
        cityCenter = cityCenter,
    )
