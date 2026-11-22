package com.bigong.oguri.core.ad

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val BottomTabBannerHeight: Dp = 60.dp
internal val BottomTabBannerContentSpacing: Dp = 24.dp

@Composable
internal fun BottomTabBanner(placement: AdMobBannerPlacement) {
    AdMobBanner(
        placement = placement,
        modifier = Modifier.fillMaxWidth().height(BottomTabBannerHeight),
    )
}
