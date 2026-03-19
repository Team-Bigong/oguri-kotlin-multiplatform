package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.util.extension.skeletonShimmer

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    Spacer(
        modifier =
            modifier
                .skeletonShimmer(shape = shape),
    )
}
