package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.Place
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_trashcan
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyPageSavedPlaceCard(
    place: Place,
    onClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(128.dp)
                .noRippleClickable(onClick = { onClick(place.id) }),
    ) {
        NetworkImage(
            imageUrl = place.thumbnailUrl,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(8.dp)),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.padding(start = 2.dp),
            ) {
                Text(
                    text = place.city,
                    style = OguriTheme.typography.cardSubtitle,
                    color = Neutral90,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = place.country,
                    style = OguriTheme.typography.bodyMedium,
                    color = Neutral50,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(Res.drawable.ic_trashcan),
                contentDescription = null,
                modifier =
                    Modifier
                        .noRippleClickable(onClick = { onDeleteClick(place.id) })
                        .size(24.dp)
                        .padding(2.dp),
            )
        }
    }
}
