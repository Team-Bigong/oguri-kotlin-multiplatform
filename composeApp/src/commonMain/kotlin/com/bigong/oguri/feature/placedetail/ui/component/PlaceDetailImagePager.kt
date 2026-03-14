package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.ui.component.NetworkImage

private val PLACE_DETAIL_HERO_HEIGHT = 270.dp

@Composable
fun PlaceDetailImagePager(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { imageUrls.size })

    Box(modifier = modifier.fillMaxWidth().height(PLACE_DETAIL_HERO_HEIGHT)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(PLACE_DETAIL_HERO_HEIGHT),
        ) { page ->
            NetworkImage(
                imageUrl = imageUrls[page],
                modifier = Modifier.fillMaxWidth().height(PLACE_DETAIL_HERO_HEIGHT),
            )
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(imageUrls.size) { index ->
                val isSelected = index == pagerState.currentPage
                DotIndicator(isSelected = isSelected)
            }
        }
    }
}

@Composable
private fun DotIndicator(
    isSelected: Boolean,
) {
    Box(
        modifier =
            Modifier
                .size(8.dp)
                .background(
                    color = if (isSelected) Mint5 else Mint5.copy(alpha = 0.5f),
                    shape = CircleShape,
                ),
    )
}
