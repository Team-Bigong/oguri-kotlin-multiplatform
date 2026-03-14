package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.Experience
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.place_detail_experience_detail
import org.jetbrains.compose.resources.stringResource

private val EXPERIENCE_CARD_CORNER_RADIUS = 8.dp

@Composable
fun ExperienceCard(
    experience: Experience,
    modifier: Modifier = Modifier,
    uniformHeight: Dp? = null,
    onMeasuredHeight: (Int) -> Unit = {},
    onClick: (String) -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .width(164.dp)
                .then(
                    if (uniformHeight != null) {
                        Modifier.height(uniformHeight)
                    } else {
                        Modifier
                    },
                ).onGloballyPositioned { coordinates ->
                    onMeasuredHeight(coordinates.size.height)
                }.noRippleClickable(onClick = { onClick(experience.advertisementUrl) }),
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
        Text(
            text = experience.title,
            style = OguriTheme.typography.labelLarge,
            color = Neutral100,
        )
        Text(
            text = experience.summary,
            style = OguriTheme.typography.labelMedium,
            color = Neutral50,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.place_detail_experience_detail),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .border(width = 1.dp, color = Neutral40, shape = RoundedCornerShape(4.dp))
                    .background(Neutral5)
                    .padding(vertical = 6.dp),
            style = OguriTheme.typography.labelSmall,
            color = Neutral70,
            textAlign = TextAlign.Center,
        )
    }
}
