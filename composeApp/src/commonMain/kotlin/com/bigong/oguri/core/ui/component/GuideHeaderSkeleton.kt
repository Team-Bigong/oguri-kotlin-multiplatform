package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GuideHeaderSkeleton(
    modifier: Modifier = Modifier,
    withSubtitle: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SkeletonBox(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(6.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SkeletonBox(modifier = Modifier.width(220.dp).height(26.dp))
            if (withSubtitle) {
                SkeletonBox(modifier = Modifier.width(140.dp).height(18.dp))
            }
        }
    }
}
