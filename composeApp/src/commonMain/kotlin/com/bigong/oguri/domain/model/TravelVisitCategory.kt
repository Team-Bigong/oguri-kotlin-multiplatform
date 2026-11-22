package com.bigong.oguri.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TravelVisitCategory {
    RESTAURANT,
    CAFE,
    SIGHTSEEING,
    LODGING,
    FLIGHT,
    TRANSPORT,
    SHOPPING,
    OTHER,
}
