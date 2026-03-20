package com.bigong.oguri.core.ad

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class AdMobBannerPlacement {
    CALENDAR_INLINE,
    PHOTO_DETAIL_BOTTOM,
}

expect fun initializeAdMob()

expect fun preloadAppOpenAd()

expect fun showAppOpenAdIfAvailable(): Boolean

@Composable
expect fun AdMobBanner(
    placement: AdMobBannerPlacement,
    modifier: Modifier = Modifier,
)
