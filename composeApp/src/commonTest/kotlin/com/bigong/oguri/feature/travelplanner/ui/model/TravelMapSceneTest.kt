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
            scene.lines.map(TravelMapLine::points),
        )
    }

    @Test
    fun missingCoordinatesBreakTheLineWithoutRenumberingTheOtherVisits() {
        val visits = listOf(visit("first", station), visit("manual", null), visit("third", museum), visit("fourth", park))
        val scene = travelMapScene(2, visits, places, null, null)

        assertEquals(listOf("1", "3", "4"), scene.markers.map(TravelMapMarker::label))
        assertEquals(listOf(listOf(museum.coordinates, park.coordinates)), scene.lines.map(TravelMapLine::points))
    }

    @Test
    fun undatedAndRepeatedVisitsRemainSeparateWithoutLines() {
        val visits = listOf(visit("first", station), visit("second", station))
        val scene = travelMapScene(TravelPlan.UNASSIGNED_DAY, visits, places, null, null)

        assertEquals(listOf("first", "second"), scene.markers.map(TravelMapMarker::id))
        assertTrue(scene.markers.all { it.label == null })
        assertTrue(scene.lines.isEmpty())
    }

    @Test
    fun overviewLabelsDaysKeepsRepeatedVisitsAndNeverConnectsDates() {
        val visits =
            listOf(
                visit("first", station),
                visit("repeat", station).copy(dayNumber = 2),
                visit("undated", park).copy(dayNumber = 0),
                visit("manual", null).copy(dayNumber = 3),
            )
        val scene = travelOverviewScene(visits, places, station.coordinates)
        assertEquals(listOf("first", "repeat", "undated"), scene.markers.map { it.id })
        assertEquals(listOf("1", "2", "–"), scene.markers.map { it.label })
        assertEquals(listOf(1, 2, 0), scene.markers.map { it.dayNumber })
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

    private fun place(
        id: String,
        latitude: Double,
    ): TravelPlace = TravelPlace(id, id, null, TravelCoordinates(latitude, 135.5), emptyList())

    private fun visit(
        id: String,
        place: TravelPlace?,
    ): TravelVisit = TravelVisit(id, 1, TravelVisitDetails(null, null, null, null, place?.id), 1L)
}
