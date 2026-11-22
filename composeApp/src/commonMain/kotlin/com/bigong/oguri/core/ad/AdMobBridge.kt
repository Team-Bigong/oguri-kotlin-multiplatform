package com.bigong.oguri.core.ad

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class AdMobBannerPlacement {
    CALENDAR_INLINE,
    PHOTO_DETAIL_BOTTOM,
    MYPAGE_BOTTOM,
    TRAVEL_BOTTOM,
}

expect fun initializeAdMob()

expect fun preloadAppOpenAd()

expect fun showAppOpenAdIfAvailable(onImpression: () -> Unit)

@Composable
expect fun AdMobBanner(
    placement: AdMobBannerPlacement,
    modifier: Modifier = Modifier,
)
