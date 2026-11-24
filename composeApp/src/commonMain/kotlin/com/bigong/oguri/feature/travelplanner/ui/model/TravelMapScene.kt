package com.bigong.oguri.feature.travelplanner.ui.model

import androidx.compose.ui.graphics.toArgb
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Mint90
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.Orange40
import com.bigong.oguri.core.designsystem.Orange70
import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitCategory
import kotlin.math.roundToInt

data class TravelMapMarker(
    val id: String,
    val coordinates: TravelCoordinates,
    val label: String?,
    val selected: Boolean,
    val dayNumber: Int? = null,
    val title: String? = null,
    val category: TravelVisitCategory? = null,
) {
    private val colors: TravelVisitMarkerColors = travelVisitMarkerColors(category)

    val colorArgb: Int = colors.fill.toArgb()
    val labelColorArgb: Int = colors.number.toArgb()
    val highlightColorArgb: Int = colors.fill.copy(alpha = 0.28f).toArgb()

    val dayBorderColorArgb: Int?
        get() = dayNumber?.let(::travelDayColor)
}

internal fun travelDayColor(dayNumber: Int): Int =
    when ((dayNumber.coerceAtLeast(1) - 1) % 5) {
        0 -> Mint70
        1 -> Orange40
        2 -> Mint90
        3 -> Orange70
        else -> Neutral70
    }.toArgb()

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
    val referenceCoordinates: List<TravelCoordinates> = emptyList(),
) {
    val viewportCoordinates: List<TravelCoordinates>
        get() = markers.map { it.coordinates }.ifEmpty { referenceCoordinates }
}

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
            coordinates[index]?.let { point ->
                TravelMapMarker(
                    id = visit.id,
                    coordinates = point,
                    label = if (numbered) (index + 1).toString() else null,
                    selected = visit.id == selectedVisitId,
                    title = visit.details.name ?: places[visit.details.googlePlaceId]?.name,
                    category = visit.details.category,
                )
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
                        category = visit.details.category,
                    )
                }
            },
        lines = emptyList(),
        cityCenter = cityCenter,
    )
