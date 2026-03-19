package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral10

@Composable
fun PlaceCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = Neutral10, shape = RoundedCornerShape(8.dp)),
    ) {
        SkeletonBox(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(134.dp),
            shape =
                RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                ),
        )
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SkeletonBox(modifier = Modifier.width(72.dp).height(22.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(18.dp))
            SkeletonBox(modifier = Modifier.width(96.dp).height(18.dp))
        }
    }
}
