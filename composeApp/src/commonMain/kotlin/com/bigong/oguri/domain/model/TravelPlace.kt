package com.bigong.oguri.domain.model

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.serialization.Serializable

private const val EARTH_RADIUS_KILOMETERS = 6371.0088
private const val RADIANS_PER_DEGREE = PI / 180.0

@Serializable
data class TravelCoordinates(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0 && longitude in -180.0..180.0)
    }

    internal fun distanceTo(other: TravelCoordinates): Double {
        val startLatitude = latitude * RADIANS_PER_DEGREE
        val endLatitude = other.latitude * RADIANS_PER_DEGREE
        val halfLatitude = sin((endLatitude - startLatitude) / 2)
        val halfLongitude = sin((other.longitude - longitude) * RADIANS_PER_DEGREE / 2)
        val haversine = halfLatitude * halfLatitude +
            cos(startLatitude) * cos(endLatitude) * halfLongitude * halfLongitude
        return 2 * EARTH_RADIUS_KILOMETERS * asin(sqrt(haversine.coerceIn(0.0, 1.0)))
    }

    internal fun midpointTo(other: TravelCoordinates): TravelCoordinates {
        val startLatitude = latitude * RADIANS_PER_DEGREE
        val endLatitude = other.latitude * RADIANS_PER_DEGREE
        val deltaLongitude = (other.longitude - longitude) * RADIANS_PER_DEGREE
        val x = cos(startLatitude) + cos(endLatitude) * cos(deltaLongitude)
        val y = cos(endLatitude) * sin(deltaLongitude)
        val z = sin(startLatitude) + sin(endLatitude)
        val midpointLatitude = atan2(z, hypot(x, y)) / RADIANS_PER_DEGREE
        val midpointLongitude = longitude + atan2(y, x) / RADIANS_PER_DEGREE
        return TravelCoordinates(midpointLatitude, (midpointLongitude + 540.0) % 360.0 - 180.0)
    }
}

data class TravelPlace(
    val id: String,
    val name: String,
    val address: String?,
    val coordinates: TravelCoordinates?,
    val attributions: List<TravelPlaceAttribution>,
)

data class TravelPlaceAttribution(
    val provider: String,
    val providerUri: String?,
)

data class TravelDayPlaces(
    val places: Map<String, TravelPlace>,
    val incomplete: Boolean,
)

@Serializable
data class TravelCitySuggestion(
    val id: String,
    val label: String,
    val name: String = label,
    val regionLabel: String? = null,
    val timeZone: String? = null,
    val coordinates: TravelCoordinates? = null,
)
