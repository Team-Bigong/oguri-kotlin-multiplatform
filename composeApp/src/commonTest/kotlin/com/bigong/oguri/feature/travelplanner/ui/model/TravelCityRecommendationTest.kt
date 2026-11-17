package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.domain.model.TravelCoordinates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TravelCityRecommendationTest {
    @Test
    fun recommendsOnlyNearbyCitiesAndExcludesSelectedAliases() {
        val result = TravelCity.nearby("東京", listOf("도쿄", "Yokohama"), emptyMap())

        assertEquals(listOf(TravelCity.KAMAKURA), result)
    }

    @Test
    fun usesNearbyPresetsForCatalogCitiesWithMatchingCoordinates() {
        val paris = TravelCitySuggestion(
            id = "paris",
            label = "Paris, France",
            name = "Paris",
            coordinates = TravelCoordinates(48.86, 2.35),
        )

        val result = TravelCity.nearby(paris.label, listOf(paris.label), mapOf(paris.label to paris))

        assertEquals(listOf(TravelCity.VERSAILLES, TravelCity.SAINT_GERMAIN_EN_LAYE), result)
    }

    @Test
    fun excludesCatalogCitiesInAnotherCountryWithTheSameName() {
        val paris = TravelCitySuggestion(
            id = "paris-texas",
            label = "Paris, Texas",
            name = "Paris",
            coordinates = TravelCoordinates(33.66, -95.56),
        )

        assertTrue(TravelCity.nearby(paris.label, listOf(paris.label), mapOf(paris.label to paris)).isEmpty())
    }
}
