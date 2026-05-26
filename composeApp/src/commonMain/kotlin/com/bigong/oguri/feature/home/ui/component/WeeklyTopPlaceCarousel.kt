package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.Place

@Composable
fun WeeklyTopPlaceCarousel(
    places: List<Place>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
    onPlaceClick: (Place) -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
    ) {
        itemsIndexed(items = places, key = { _, place -> place.id }) { index, place ->
            WeeklyTopPlaceCard(
                rank = index + 1,
                place = place,
                onClick = onPlaceClick,
            )
        }
    }
}

@Composable
private fun WeeklyTopPlaceCard(
    rank: Int,
    place: Place,
    modifier: Modifier = Modifier,
    onClick: (Place) -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .width(width = 80.dp)
                .noRippleClickable(onClick = { onClick(place) }),
    ) {
        Box(
            modifier =
                Modifier
                    .size(size = 80.dp)
                    .clip(shape = RoundedCornerShape(size = 8.dp)),
        ) {
            NetworkImage(
                imageUrl = place.thumbnailUrl,
                contentDescription = place.city,
                modifier =
                    Modifier
                        .matchParentSize(),
            )
            if (rank <= TOP_RANK_BADGE_MAX_RANK) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .size(size = 24.dp)
                            .background(
                                color = Mint70,
                                shape = RoundedCornerShape(bottomEnd = 8.dp),
                            ),
                ) {
                    Text(
                        text = rank.toString(),
                        style = OguriTheme.typography.labelLarge,
                        color = Neutral0,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(height = 8.dp))
        Text(
            text = place.city,
            style = OguriTheme.typography.labelLarge,
            color = Neutral100,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(height = 4.dp))
        Text(
            text = place.country,
            style = OguriTheme.typography.labelSmall,
            color = Neutral40,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

private const val TOP_RANK_BADGE_MAX_RANK = 3
