package com.bigong.oguri.core.ad

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bigong.oguri.core.platform.OguriPlatformContextHolder
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlin.time.Duration.Companion.hours

private const val ANDROID_BANNER_AD_UNIT_ID_DEBUG = "ca-app-pub-3940256099942544/6300978111"
private const val ANDROID_BANNER_AD_UNIT_ID_RELEASE = "ca-app-pub-9643550840413935/9847973658"
private const val ANDROID_APP_OPEN_AD_UNIT_ID_DEBUG = "ca-app-pub-3940256099942544/9257395921"
private const val ANDROID_APP_OPEN_AD_UNIT_ID_RELEASE = "ca-app-pub-9643550840413935/2949857964"
private val APP_OPEN_AD_MAX_AGE = 4.hours
private const val BANNER_MAX_HEIGHT_DP = 50

private object AndroidAdMobManager {
    private var isInitialized: Boolean = false
    private var appOpenAd: AppOpenAd? = null
    private var appOpenAdLoadedAt: Long = 0L
    private var currentActivity: Activity? = null

    fun initialize() {
        if (isInitialized) {
            return
        }
        val applicationContext: Context = OguriPlatformContextHolder.applicationContext ?: return
        isInitialized = true
        MobileAds.initialize(applicationContext)
        AppOpenAd.load(
            applicationContext,
            getAndroidAppOpenAdUnitId(),
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(loadedAd: AppOpenAd) {
                    appOpenAdLoadedAt = SystemClock.elapsedRealtime()
                    appOpenAd = loadedAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                }
            },
        )
    }

    fun showAppOpenAdIfAvailable() {
        val activity = currentActivity ?: return
        val cachedAd = appOpenAd ?: return
        if (SystemClock.elapsedRealtime() - appOpenAdLoadedAt >= APP_OPEN_AD_MAX_AGE.inWholeMilliseconds) {
            appOpenAd = null
            return
        }

        cachedAd.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                }
            }
        cachedAd.show(activity)
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

internal actual fun showAppOpenAdIfAvailable() {
    AndroidAdMobManager.showAppOpenAdIfAvailable()
}

private fun getAndroidBannerAdUnitId(): String =
    if (isDebuggableApplication()) {
        ANDROID_BANNER_AD_UNIT_ID_DEBUG
    } else {
        ANDROID_BANNER_AD_UNIT_ID_RELEASE
    }

private fun getAndroidAppOpenAdUnitId(): String =
    if (isDebuggableApplication()) {
        ANDROID_APP_OPEN_AD_UNIT_ID_DEBUG
    } else {
        ANDROID_APP_OPEN_AD_UNIT_ID_RELEASE
    }

private fun isDebuggableApplication(): Boolean {
    val applicationContext: Context = OguriPlatformContextHolder.applicationContext ?: return false
    return (applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}

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
                adUnitId = getAndroidBannerAdUnitId()
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
