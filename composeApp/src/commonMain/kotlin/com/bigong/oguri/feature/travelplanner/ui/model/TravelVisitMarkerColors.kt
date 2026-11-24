package com.bigong.oguri.feature.travelplanner.ui.model

import androidx.compose.ui.graphics.Color
import com.bigong.oguri.core.designsystem.LightOguriColorPalette
import com.bigong.oguri.domain.model.TravelVisitCategory

internal data class TravelVisitMarkerColors(
    val fill: Color,
    val number: Color = LightOguriColorPalette.textPrimary,
)

private val restaurantMarker = TravelVisitMarkerColors(Color(0xFFF77765))
private val cafeMarker = TravelVisitMarkerColors(Color(0xFFF0B42F))
private val sightseeingMarker = TravelVisitMarkerColors(Color(0xFF34C2A6))
private val lodgingMarker = TravelVisitMarkerColors(Color(0xFFA182EA))
private val transportMarker = TravelVisitMarkerColors(Color(0xFF4AA8EB))
private val shoppingMarker = TravelVisitMarkerColors(Color(0xFFEA74AB))
private val otherMarker = TravelVisitMarkerColors(Color(0xFF9AA9BB))

internal fun travelVisitMarkerColors(category: TravelVisitCategory?): TravelVisitMarkerColors =
    when (category) {
        TravelVisitCategory.RESTAURANT -> restaurantMarker
        TravelVisitCategory.CAFE -> cafeMarker
        TravelVisitCategory.SIGHTSEEING -> sightseeingMarker
        TravelVisitCategory.LODGING -> lodgingMarker
        TravelVisitCategory.FLIGHT, TravelVisitCategory.TRANSPORT -> transportMarker
        TravelVisitCategory.SHOPPING -> shoppingMarker
        TravelVisitCategory.OTHER, null -> otherMarker
    }
