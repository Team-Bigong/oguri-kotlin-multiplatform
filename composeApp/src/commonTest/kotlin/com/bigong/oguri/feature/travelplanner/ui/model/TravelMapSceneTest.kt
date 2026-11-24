package com.bigong.oguri.feature.travelplanner.ui.model

import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitCategory
import com.bigong.oguri.domain.model.TravelVisitDetails
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TravelMapSceneTest {
    private val station = place("station", 34.70)
    private val museum = place("museum", 34.71)
    private val park = place("park", 34.72)
    private val places = listOf(station, museum, park).associateBy(TravelPlace::id)

    @Test
    fun reorderedVisitsUseTheSameNumbersAndLinesAsTheList() {
        val visits = listOf(visit("museum", museum), visit("park", park), visit("station", station))
        val scene = travelMapScene(1, visits, places, "park", null)

        assertEquals(listOf("museum", "park", "station"), scene.markers.map(TravelMapMarker::id))
        assertEquals(listOf("1", "2", "3"), scene.markers.map(TravelMapMarker::label))
        assertEquals(listOf("park"), scene.markers.filter(TravelMapMarker::selected).map(TravelMapMarker::id))
        assertEquals(
            listOf(listOf(museum.coordinates, park.coordinates), listOf(park.coordinates, station.coordinates)),
            scene.lines.map { listOf(it.start, it.end) },
        )
        assertEquals(listOf("museum" to "park", "park" to "station"), scene.lines.map { it.fromVisitId to it.toVisitId })
        assertEquals(listOf("1.1", "2.2"), scene.lines.map { it.displayDistance })
    }

    @Test
    fun missingCoordinatesBreakTheLineWithoutRenumberingTheOtherVisits() {
        val visits = listOf(visit("first", station), visit("manual", null), visit("third", museum), visit("fourth", park))
        val scene = travelMapScene(2, visits, places, null, null)

        assertEquals(listOf("1", "3", "4"), scene.markers.map(TravelMapMarker::label))
        val line = scene.lines.single()
        assertEquals("third" to "fourth", line.fromVisitId to line.toVisitId)
        assertEquals(museum.coordinates, line.start)
        assertEquals(park.coordinates, line.end)
    }

    @Test
    fun distanceLabelUsesTheShortArcAcrossTheDateLine() {
        val start = TravelCoordinates(0.0, 179.9)
        val end = TravelCoordinates(0.0, -179.9)
        val line = TravelMapLine("start", "end", start, end)

        assertEquals("22.2", line.displayDistance)
        assertEquals(0.0, line.midpoint.latitude, 0.000001)
        assertEquals(180.0, kotlin.math.abs(line.midpoint.longitude), 0.000001)
    }

    @Test
    fun shortDistancesKeepTheirUnitWithoutRoundingToZero() {
        val line = TravelMapLine("start", "end", TravelCoordinates(0.0, 0.0), TravelCoordinates(0.0, 0.0001))

        assertEquals("<0.1", line.displayDistance)
    }

    @Test
    fun unassignedAndIdenticalCoordinatesDoNotCreateConnections() {
        val distinct = listOf(visit("first", station), visit("second", museum))
        val repeated = listOf(visit("first", station), visit("second", station))

        assertTrue(travelMapScene(TravelPlan.UNASSIGNED_DAY, distinct, places, null, null).lines.isEmpty())
        assertTrue(travelMapScene(1, repeated, places, null, null).lines.isEmpty())
    }

    @Test
    fun overviewColorsPinsByDateAndKeepsRepeatedVisitsSeparate() {
        val first = visit("first", station).let { it.copy(details = it.details.copy(category = TravelVisitCategory.TRANSPORT)) }
        val visits =
            listOf(
                first,
                first.copy(id = "repeat", dayNumber = 2),
                visit("third", park).copy(dayNumber = 3),
                visit("manual", null).copy(dayNumber = 3),
            )
        val scene = travelOverviewScene(visits, places, station.coordinates)
        assertEquals(listOf("first", "repeat", "third"), scene.markers.map { it.id })
        assertEquals(listOf("1", "1", "1"), scene.markers.map { it.label })
        assertEquals(listOf(1, 2, 3), scene.markers.map { it.dayNumber })
        val colors = (1..3).map(::travelDayMarkerColors)
        assertEquals(colors.map { it.fill.toArgb() }, scene.markers.map { it.colorArgb })
        assertEquals(colors.map { it.number.toArgb() }, scene.markers.map { it.labelColorArgb })
        assertEquals(station.coordinates, scene.cityCenter)
        assertTrue(scene.lines.isEmpty())
    }

    @Test
    fun selectingADailyMarkerPreservesItsCategoryColorAndNumberContrast() {
        val visit = visit("museum", museum).let { it.copy(details = it.details.copy(category = TravelVisitCategory.SIGHTSEEING)) }
        val marker = travelMapScene(1, listOf(visit), places, visit.id, null).markers.single()
        val unselectedMarker = travelMapScene(1, listOf(visit), places, null, null).markers.single()
        val colors = travelVisitMarkerColors(visit.details.category)

        assertTrue(marker.selected)
        assertEquals(colors.fill.toArgb(), marker.colorArgb)
        assertEquals(unselectedMarker.colorArgb, marker.colorArgb)
        assertEquals(colors.number.toArgb(), marker.labelColorArgb)
    }

    @Test
    fun overviewDateColorsCycleThroughTheSixVividPaletteColors() {
        val categories = listOf(
            TravelVisitCategory.SIGHTSEEING,
            TravelVisitCategory.RESTAURANT,
            TravelVisitCategory.TRANSPORT,
            TravelVisitCategory.CAFE,
            TravelVisitCategory.LODGING,
            TravelVisitCategory.SHOPPING,
        )
        val expected = categories.map(::travelVisitMarkerColors)
        assertEquals(expected, (1..6).map(::travelDayMarkerColors))
        assertEquals(6, expected.map { it.fill }.distinct().size)
        assertEquals(expected.first(), travelDayMarkerColors(7))
        assertEquals(expected.first(), travelDayMarkerColors(TravelPlan.UNASSIGNED_DAY))
    }

    @Test
    fun categoryNumbersHaveEnoughContrastOnEveryFill() {
        val categories = TravelVisitCategory.entries.filterNot { it == TravelVisitCategory.FLIGHT }
        val fills = categories.map { travelVisitMarkerColors(it).fill }
        assertEquals(categories.size, fills.distinct().size)
        assertEquals(travelVisitMarkerColors(TravelVisitCategory.TRANSPORT), travelVisitMarkerColors(TravelVisitCategory.FLIGHT))
        assertEquals(travelVisitMarkerColors(TravelVisitCategory.OTHER), travelVisitMarkerColors(null))
        for (category in categories) {
            val colors = travelVisitMarkerColors(category)
            val luminance = listOf(colors.fill.luminance(), colors.number.luminance()).sorted()
            val contrast = (luminance[1] + 0.05f) / (luminance[0] + 0.05f)
            assertTrue(contrast >= 4.5f, "$category marker number contrast: $contrast")
        }
    }

    @Test
    fun markerTitlesIdentifyThePlaceAndPreserveCustomNames() {
        val original = visit("station", station)
        val custom = visit("museum", museum).copy(details = visit("museum", museum).details.copy(name = "Meeting point"))
        val visits = listOf(original, custom)
        assertEquals(listOf("station", "Meeting point"), travelMapScene(1, visits, places, null, null).markers.map { it.title })
        assertEquals(listOf("station", "Meeting point"), travelOverviewScene(visits, places, null).markers.map { it.title })
    }

    private fun place(
        id: String,
        latitude: Double,
    ): TravelPlace = TravelPlace(id, id, null, TravelCoordinates(latitude, 135.5), emptyList())

    private fun visit(
        id: String,
        place: TravelPlace?,
    ): TravelVisit = TravelVisit(id, 1, TravelVisitDetails(null, null, null, null, place?.id), 1L)
}
