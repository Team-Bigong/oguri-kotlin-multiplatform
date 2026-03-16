package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint10
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral60
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.Place

@Composable
fun PlaceCard(
    place: Place,
    modifier: Modifier = Modifier,
    onClick: (Place) -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .clip(shape = RoundedCornerShape(size = 8.dp))
                .background(color = Mint10, shape = RoundedCornerShape(size = 8.dp))
                .noRippleClickable(onClick = { onClick(place) }),
    ) {
        NetworkImage(
            imageUrl = place.thumbnailUrl,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height = 134.dp)
                    .clip(
                        shape =
                            RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                            ),
                    ),
        )
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 10.dp, bottom = 12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = place.city,
                    style = OguriTheme.typography.labelLarge,
                    color = Neutral100,
                )
                Text(
                    text = place.country,
                    style = OguriTheme.typography.labelSmall,
                    color = Neutral40,
                )
            }
            Spacer(modifier = Modifier.height(height = 4.dp))
            Text(
                text = place.summary,
                style = OguriTheme.typography.labelMedium,
                color = Neutral50,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
