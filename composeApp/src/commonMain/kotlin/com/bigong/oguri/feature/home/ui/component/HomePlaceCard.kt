package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.domain.model.Place

private val HOME_PLACE_CARD_CORNER_RADIUS = 14.dp

@Composable
fun HomePlaceCard(
    place: Place,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = HOME_PLACE_CARD_CORNER_RADIUS))
            .background(color = Color(0xFFDDE7E7), shape = RoundedCornerShape(size = HOME_PLACE_CARD_CORNER_RADIUS)),
    ) {
        NetworkImage(
            imageUrl = place.thumbnailUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 128.dp),
        )
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = place.city,
                    style = OguriTheme.typography.cardTitle,
                    color = Color(0xFF151B21),
                )
                Text(
                    text = place.country,
                    style = OguriTheme.typography.bodySmall,
                    color = Color(0xFF8C98A4),
                )
            }
            Spacer(modifier = Modifier.height(height = 4.dp))
            Text(
                text = place.summary,
                style = OguriTheme.typography.bodySmall,
                color = Color(0xFF60707E),
            )
        }
    }
}
