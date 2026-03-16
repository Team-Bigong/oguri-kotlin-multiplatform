package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral10

@Composable
fun AdvertisementCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = Neutral10, shape = RoundedCornerShape(8.dp)),
    ) {
        SkeletonBox(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(128.dp),
            shape =
                RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                ),
        )
        SkeletonBox(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .height(24.dp),
        )
    }
}
