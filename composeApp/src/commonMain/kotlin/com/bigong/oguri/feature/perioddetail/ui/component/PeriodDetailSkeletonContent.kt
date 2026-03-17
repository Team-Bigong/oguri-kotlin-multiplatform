package com.bigong.oguri.feature.perioddetail.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.ui.component.GuideHeaderSkeleton
import com.bigong.oguri.core.ui.component.PlaceCardSkeleton
import com.bigong.oguri.core.ui.component.SkeletonBox

@Composable
fun PeriodDetailSkeletonContent(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier =
            modifier
                .fillMaxSize()
                .background(Neutral5),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 18.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SkeletonBox(modifier = Modifier.width(190.dp).height(38.dp))
                    SkeletonBox(modifier = Modifier.width(170.dp).height(24.dp))
                    SkeletonBox(modifier = Modifier.width(132.dp).height(18.dp))
                }
                SkeletonBox(modifier = Modifier.width(114.dp).height(120.dp))
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            GuideHeaderSkeleton(withSubtitle = false)
            Spacer(modifier = Modifier.height(18.dp))
        }

        items(4) {
            PlaceCardSkeleton(modifier = Modifier.fillMaxWidth())
        }
    }
}
