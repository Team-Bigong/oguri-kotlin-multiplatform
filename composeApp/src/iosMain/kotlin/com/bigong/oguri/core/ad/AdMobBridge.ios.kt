package com.bigong.oguri.core.ad

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIView

private const val IOS_NOTIFICATION_ADMOB_INITIALIZE = "OguriAdMobInitialize"
private const val IOS_NOTIFICATION_PRELOAD_APP_OPEN = "OguriAdMobPreloadAppOpen"
private const val IOS_NOTIFICATION_SHOW_APP_OPEN = "OguriAdMobShowAppOpen"
private const val IOS_NOTIFICATION_ATTACH_BANNER = "OguriAdMobAttachBanner"
private const val IOS_NOTIFICATION_DETACH_BANNER = "OguriAdMobDetachBanner"

actual fun initializeAdMob() {
    NSNotificationCenter.defaultCenter.postNotificationName(IOS_NOTIFICATION_ADMOB_INITIALIZE, `object` = null)
}

actual fun preloadAppOpenAd() {
    NSNotificationCenter.defaultCenter.postNotificationName(IOS_NOTIFICATION_PRELOAD_APP_OPEN, `object` = null)
}

actual fun showAppOpenAdIfAvailable(): Boolean {
    NSNotificationCenter.defaultCenter.postNotificationName(IOS_NOTIFICATION_SHOW_APP_OPEN, `object` = null)
    return true
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdMobBanner(
    placement: AdMobBannerPlacement,
    modifier: Modifier,
) {
    val containerView =
        remember(placement) {
            UIView().apply {
                clipsToBounds = true
            }
        }

    DisposableEffect(containerView, placement) {
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = IOS_NOTIFICATION_ATTACH_BANNER,
            `object` = containerView,
            userInfo = mapOf("placementKey" to placement.name),
        )

        onDispose {
            NSNotificationCenter.defaultCenter.postNotificationName(
                aName = IOS_NOTIFICATION_DETACH_BANNER,
                `object` = containerView,
            )
        }
    }

    UIKitView(
        factory = { containerView },
        modifier = modifier,
    )
}
