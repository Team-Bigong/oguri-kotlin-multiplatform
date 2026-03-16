package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.domain.model.Place

@Composable
fun PlaceHorizontalCarousel(
    places: List<Place>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
    onPlaceClick: (Place) -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
    ) {
        items(items = places, key = { place -> place.id }) { place ->
            PlaceCard(
                place = place,
                modifier = Modifier.width(width = 154.dp),
                onClick = onPlaceClick,
            )
        }
    }
}
