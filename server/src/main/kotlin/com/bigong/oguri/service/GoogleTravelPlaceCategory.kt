package com.bigong.oguri.service

import com.bigong.oguri.domain.TravelVisitCategory

internal fun googleTravelPlaceCategory(
    primaryType: String?,
    types: List<String>,
): TravelVisitCategory? =
    primaryType?.let(::categoryForGooglePlaceType)
        ?: types.mapNotNull(::categoryForGooglePlaceType).distinct().singleOrNull()

private fun categoryForGooglePlaceType(type: String): TravelVisitCategory? =
    when (type) {
        "bakery", "cafe", "cake_shop", "cat_cafe", "coffee_roastery", "coffee_shop", "coffee_stand", "dessert_restaurant",
        "dessert_shop", "dog_cafe", "donut_shop", "ice_cream_shop", "pastry_shop", "tea_house",
        -> TravelVisitCategory.CAFE

        "bar_and_grill", "bistro", "cafeteria", "diner", "food_court", "noodle_shop", "restaurant", "sandwich_shop", "steak_house",
        -> TravelVisitCategory.RESTAURANT

        "amusement_park", "aquarium", "art_gallery", "beach", "botanical_garden", "buddhist_temple", "castle", "church", "city_park",
        "cultural_landmark", "hiking_area", "hindu_temple", "historical_landmark", "historical_place", "landmark", "monument", "mosque",
        "museum", "national_park", "nature_preserve", "observation_deck", "park", "place_of_worship", "scenic_spot", "shinto_shrine",
        "state_park", "synagogue", "tourist_attraction", "water_park", "zoo",
        -> TravelVisitCategory.SIGHTSEEING

        "bed_and_breakfast", "budget_japanese_inn", "campground", "camping_cabin", "cottage", "farmstay", "guest_house", "hostel", "hotel",
        "inn", "japanese_inn", "lodging", "motel", "private_guest_room", "rv_park",
        -> TravelVisitCategory.LODGING

        "airport", "airstrip", "bike_sharing_station", "bus_station", "bus_stop", "car_rental", "ferry_service", "ferry_terminal",
        "international_airport", "light_rail_station", "parking", "parking_garage", "parking_lot", "subway_station", "taxi_service",
        "taxi_stand", "train_station", "tram_stop", "transit_station", "transit_stop", "transportation_service",
        -> TravelVisitCategory.TRANSPORT

        "discount_supermarket", "farmers_market", "flea_market", "gift_shop", "hypermarket", "market", "shopping_mall", "store", "supermarket",
        -> TravelVisitCategory.SHOPPING

        else ->
            when {
                type.endsWith("_restaurant") -> TravelVisitCategory.RESTAURANT
                type.endsWith("_museum") -> TravelVisitCategory.SIGHTSEEING
                type.endsWith("_hotel") -> TravelVisitCategory.LODGING
                type.endsWith("_store") -> TravelVisitCategory.SHOPPING
                else -> null
            }
    }
