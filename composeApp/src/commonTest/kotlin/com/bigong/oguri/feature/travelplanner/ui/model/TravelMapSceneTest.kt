package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelVisit
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
    fun overviewLabelsDaysKeepsRepeatedVisitsAndNeverConnectsDates() {
        val visits =
            listOf(
                visit("first", station),
                visit("repeat", station).copy(dayNumber = 2),
                visit("third", park).copy(dayNumber = 3),
                visit("manual", null).copy(dayNumber = 3),
            )
        val scene = travelOverviewScene(visits, places, station.coordinates)
        assertEquals(listOf("first", "repeat", "third"), scene.markers.map { it.id })
        assertEquals(listOf("1", "1", "1"), scene.markers.map { it.label })
        assertEquals(listOf(1, 2, 3), scene.markers.map { it.dayNumber })
        assertEquals(
            3,
            scene.markers
                .map { it.colorArgb }
                .distinct()
                .size,
        )
        assertEquals(station.coordinates, scene.cityCenter)
        assertTrue(scene.lines.isEmpty())
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
