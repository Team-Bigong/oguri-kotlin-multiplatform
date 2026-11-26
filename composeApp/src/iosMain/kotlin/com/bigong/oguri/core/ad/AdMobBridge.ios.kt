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
private const val IOS_NOTIFICATION_SHOW_INTERSTITIAL = "OguriAdMobShowInterstitial"
private const val IOS_NOTIFICATION_ATTACH_BANNER = "OguriAdMobAttachBanner"
private const val IOS_NOTIFICATION_DETACH_BANNER = "OguriAdMobDetachBanner"

actual fun initializeAdMob() {
    NSNotificationCenter.defaultCenter.postNotificationName(IOS_NOTIFICATION_ADMOB_INITIALIZE, `object` = null)
}

internal actual fun showInterstitialAdIfAvailable() {
    NSNotificationCenter.defaultCenter.postNotificationName(IOS_NOTIFICATION_SHOW_INTERSTITIAL, `object` = null)
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
