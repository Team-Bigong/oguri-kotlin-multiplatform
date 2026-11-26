package com.bigong.oguri.core.ad

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

private const val ANDROID_BANNER_AD_UNIT_ID_DEBUG = "ca-app-pub-3940256099942544/6300978111"
private const val ANDROID_BANNER_AD_UNIT_ID_RELEASE = "ca-app-pub-9643550840413935/9847973658"
private const val ANDROID_INTERSTITIAL_AD_UNIT_ID_DEBUG = "ca-app-pub-3940256099942544/1033173712"
private const val ANDROID_INTERSTITIAL_AD_UNIT_ID_METADATA = "com.bigong.oguri.ADMOB_INTERSTITIAL_AD_UNIT_ID"
private const val BANNER_MAX_HEIGHT_DP = 50

private object AndroidAdMobManager {
    private var isInitialized: Boolean = false
    private var interstitialAd: InterstitialAd? = null
    private var currentActivity: Activity? = null

    fun initialize() {
        if (isInitialized) {
            return
        }
        val applicationContext: Context = OguriPlatformContextHolder.applicationContext ?: return
        isInitialized = true
        MobileAds.initialize(applicationContext)
        val adUnitId = getAndroidInterstitialAdUnitId(applicationContext) ?: return
        InterstitialAd.load(
            applicationContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loadedAd: InterstitialAd) {
                    interstitialAd = loadedAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                }
            },
        )
    }

    fun showInterstitialAdIfAvailable() {
        val activity = currentActivity ?: return
        val cachedAd = interstitialAd ?: return

        cachedAd.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
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

internal actual fun showInterstitialAdIfAvailable() {
    AndroidAdMobManager.showInterstitialAdIfAvailable()
}

private fun getAndroidBannerAdUnitId(): String =
    if (isDebuggableApplication()) {
        ANDROID_BANNER_AD_UNIT_ID_DEBUG
    } else {
        ANDROID_BANNER_AD_UNIT_ID_RELEASE
    }

@Suppress("DEPRECATION")
private fun getAndroidInterstitialAdUnitId(context: Context): String? {
    if (isDebuggableApplication()) return ANDROID_INTERSTITIAL_AD_UNIT_ID_DEBUG

    return context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString(ANDROID_INTERSTITIAL_AD_UNIT_ID_METADATA)
        ?.takeIf { it.isNotBlank() }
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
