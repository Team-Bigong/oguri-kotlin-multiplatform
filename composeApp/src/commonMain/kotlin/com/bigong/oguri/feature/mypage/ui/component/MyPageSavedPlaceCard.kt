package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.Place
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_trashcan
import org.jetbrains.compose.resources.painterResource

private val SAVED_PLACE_CARD_SHAPE = RoundedCornerShape(size = 8.dp)

@Composable
fun MyPageSavedPlaceCard(
    place: Place,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(130.dp),
    ) {
        NetworkImage(
            imageUrl = place.thumbnailUrl,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(SAVED_PLACE_CARD_SHAPE),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = place.city,
                style = OguriTheme.typography.cardSubtitle,
                color = Neutral90,
            )
            Image(
                painter = painterResource(Res.drawable.ic_trashcan),
                contentDescription = null,
                modifier = Modifier.noRippleClickable(onClick = { onDeleteClick(place.id) }),
            )
        }
        Text(
            text = place.country,
            style = OguriTheme.typography.bodyMedium,
            color = Neutral40,
        )
    }
}
