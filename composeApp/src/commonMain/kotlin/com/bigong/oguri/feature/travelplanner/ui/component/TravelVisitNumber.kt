package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitMarkerColors

internal val TravelVisitNumberSize: Dp = 24.dp

@Composable
internal fun TravelVisitNumber(label: String, colors: TravelVisitMarkerColors) {
    Box(
        Modifier.size(TravelVisitNumberSize).background(colors.fill, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = OguriTheme.typography.labelMedium, color = colors.number)
    }
}
