package com.bigong.oguri.service

import com.bigong.oguri.domain.TravelVisitCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class GoogleTravelPlaceCategoryTest {
    @ParameterizedTest
    @CsvSource(
        "korean_restaurant, RESTAURANT",
        "coffee_shop, CAFE",
        "art_museum, SIGHTSEEING",
        "resort_hotel, LODGING",
        "international_airport, TRANSPORT",
        "department_store, SHOPPING",
    )
    fun `대표 유형은 대응하는 여행 카테고리로 바뀐다`(type: String, expected: TravelVisitCategory) {
        assertEquals(expected, googleTravelPlaceCategory(type, emptyList()))
    }

    @Test
    fun `매핑되는 대표 유형은 보조 유형보다 우선한다`() {
        assertEquals(TravelVisitCategory.CAFE, googleTravelPlaceCategory("cafe", listOf("cafe", "restaurant", "store")))
    }

    @Test
    fun `보조 유형들이 같은 카테고리면 그 카테고리를 추천한다`() {
        assertEquals(
            TravelVisitCategory.RESTAURANT,
            googleTravelPlaceCategory(null, listOf("seafood_restaurant", "restaurant", "point_of_interest")),
        )
    }

    @Test
    fun `유형 정보가 없거나 보조 유형의 카테고리가 갈리면 추천하지 않는다`() {
        assertNull(googleTravelPlaceCategory(null, emptyList()))
        assertNull(googleTravelPlaceCategory("pharmacy", listOf("pharmacy", "point_of_interest")))
        assertNull(googleTravelPlaceCategory("establishment", listOf("cafe", "restaurant")))
    }
}
