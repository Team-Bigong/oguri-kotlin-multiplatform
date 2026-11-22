package com.bigong.oguri.feature.travelplanner.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.bigong.oguri.core.designsystem.*
import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelVisit
import kotlin.math.roundToInt

data class TravelMapMarker(
    val id: String,
    val coordinates: TravelCoordinates,
    val label: String?,
    val selected: Boolean,
    val dayNumber: Int? = null,
    val title: String? = null,
) {
    val colorArgb: Int
        get() = if (selected) Mint80.toArgb() else travelDayColor(dayNumber)

    val labelColorArgb: Int
        get() = TravelMapMarkerForeground.toArgb()

    val highlightColorArgb: Int
        get() = Mint70.copy(alpha = 0.28f).toArgb()
}

internal fun travelDayColor(dayNumber: Int?): Int =
    when (((dayNumber ?: 1).coerceAtLeast(1) - 1) % 5) {
        0 -> Mint70
        1 -> Orange40
        2 -> Mint90
        3 -> Orange70
        else -> Neutral70
    }.toArgb()

internal val TravelMapMarkerForeground: Color = LightOguriColorPalette.textOnMint

@ConsistentCopyVisibility
data class TravelMapLine internal constructor(
    val fromVisitId: String,
    val toVisitId: String,
    val start: TravelCoordinates,
    val end: TravelCoordinates,
) {
    val midpoint: TravelCoordinates = start.midpointTo(end)
    private val distanceKilometers: Double = start.distanceTo(end)

    internal val displayDistance: String
        get() {
            val tenths = (distanceKilometers * 10).roundToInt()
            return if (tenths == 0) "<0.1" else "${tenths / 10}.${tenths % 10}"
        }
}

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
                TravelMapMarker(visit.id, it, if (numbered) (index + 1).toString() else null, visit.id == selectedVisitId, title = visit.details.name ?: places[visit.details.googlePlaceId]?.name)
            }
        }
    val lines =
        if (numbered) {
            coordinates.zipWithNext().mapIndexedNotNull { index, (start, end) ->
                if (start == null || end == null || start == end) null
                else TravelMapLine(visits[index].id, visits[index + 1].id, start, end)
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
                        (visits.filter { it.dayNumber == visit.dayNumber }.indexOf(visit) + 1).toString(),
                        selected = false,
                        dayNumber = visit.dayNumber,
                        title = visit.details.name ?: places[visit.details.googlePlaceId]?.name,
                    )
                }
            },
        lines = emptyList(),
        cityCenter = cityCenter,
    )
