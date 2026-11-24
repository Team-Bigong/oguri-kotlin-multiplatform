package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.LightOguriColorPalette
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelVisitCategory
import com.bigong.oguri.feature.travelplanner.ui.model.travelDayColor
import com.bigong.oguri.feature.travelplanner.ui.model.travelVisitMarkerColors

internal val TravelVisitNumberSize: Dp = 24.dp

@Composable
internal fun TravelVisitNumber(label: String, category: TravelVisitCategory?, dayNumber: Int? = null) {
    val colors = travelVisitMarkerColors(category)
    val dayBorder = if (dayNumber == null) {
        Modifier
    } else {
        Modifier.border(2.dp, Color(travelDayColor(dayNumber)), CircleShape)
            .padding(2.dp)
            .border(1.dp, LightOguriColorPalette.surfaceCard, CircleShape)
    }
    Box(
        Modifier.size(TravelVisitNumberSize).then(dayBorder).background(colors.fill, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = OguriTheme.typography.labelMedium, color = colors.number)
    }
}
