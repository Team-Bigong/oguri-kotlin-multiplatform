package com.bigong.oguri.core.ad

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bigong.oguri.core.platform.OguriPlatformContextHolder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd

private const val ANDROID_BANNER_AD_UNIT_ID = "ca-app-pub-2833810411143763/1194589445"
private const val ANDROID_APP_OPEN_AD_UNIT_ID = "ca-app-pub-2833810411143763/1849090361"
private const val BANNER_MAX_HEIGHT_DP = 50

private object AndroidAdMobManager {
    private var isInitialized: Boolean = false
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAppOpenAd: Boolean = false
    private var isShowingAppOpenAd: Boolean = false
    private var currentActivity: Activity? = null

    fun initialize() {
        if (isInitialized) {
            return
        }
        val applicationContext: Context = OguriPlatformContextHolder.applicationContext ?: return
        isInitialized = true
        MobileAds.initialize(applicationContext)
        preloadAppOpenAd()
    }

    fun preloadAppOpenAd() {
        if (isLoadingAppOpenAd || appOpenAd != null) {
            return
        }
        val applicationContext: Context = OguriPlatformContextHolder.applicationContext ?: return
        isLoadingAppOpenAd = true
        AppOpenAd.load(
            applicationContext,
            ANDROID_APP_OPEN_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(loadedAd: AppOpenAd) {
                    appOpenAd = loadedAd
                    isLoadingAppOpenAd = false
                }

                override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                    appOpenAd = null
                    isLoadingAppOpenAd = false
                }
            },
        )
    }

    fun showAppOpenAdIfAvailable(): Boolean {
        if (isShowingAppOpenAd) {
            return false
        }
        val activity: Activity = currentActivity ?: return false
        val cachedAd = appOpenAd
        if (cachedAd == null) {
            preloadAppOpenAd()
            return false
        }

        cachedAd.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isShowingAppOpenAd = false
                    appOpenAd = null
                    preloadAppOpenAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    isShowingAppOpenAd = false
                    appOpenAd = null
                    preloadAppOpenAd()
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAppOpenAd = true
                }
            }

        cachedAd.show(activity)
        return true
    }

    fun setCurrentActivity(activity: Activity?) {
        currentActivity = activity
    }
}

fun setCurrentAdMobActivity(activity: Activity?) {
    AndroidAdMobManager.setCurrentActivity(activity)
}

actual fun initializeAdMob() {
    AndroidAdMobManager.initialize()
}

actual fun preloadAppOpenAd() {
    AndroidAdMobManager.preloadAppOpenAd()
}

actual fun showAppOpenAdIfAvailable(): Boolean = AndroidAdMobManager.showAppOpenAdIfAvailable()

@Composable
actual fun AdMobBanner(
    placement: AdMobBannerPlacement,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val adWidthDp = configuration.screenWidthDp

    val adView =
        remember(placement, adWidthDp) {
            AdView(context).apply {
                adUnitId = ANDROID_BANNER_AD_UNIT_ID
                setAdSize(
                    AdSize.getInlineAdaptiveBannerAdSize(
                        adWidthDp,
                        BANNER_MAX_HEIGHT_DP,
                    ),
                )
                loadAd(AdRequest.Builder().build())
            }
        }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier,
    )
}
