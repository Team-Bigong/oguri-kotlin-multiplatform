package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint10
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral60
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.Experience

private val EXPERIENCE_CARD_CORNER_RADIUS = 8.dp

@Composable
fun ExperienceCard(
    experience: Experience,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .width(164.dp)
                .clip(shape = RoundedCornerShape(EXPERIENCE_CARD_CORNER_RADIUS))
                .background(color = Mint10)
                .noRippleClickable(onClick = { onClick(experience.advertisementUrl) }),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NetworkImage(
            imageUrl = experience.thumbnailUrl,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(98.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = EXPERIENCE_CARD_CORNER_RADIUS,
                            topEnd = EXPERIENCE_CARD_CORNER_RADIUS,
                        ),
                    ),
        )
        Column(modifier = Modifier.padding(horizontal = 10.dp).padding(bottom = 10.dp)) {
            Text(
                text = experience.title,
                style = OguriTheme.typography.labelLarge,
                color = Neutral100,
            )
            Text(
                text = experience.summary,
                style = OguriTheme.typography.labelMedium,
                color = Neutral60,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
