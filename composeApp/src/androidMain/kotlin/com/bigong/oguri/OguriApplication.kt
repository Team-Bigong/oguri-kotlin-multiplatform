package com.bigong.oguri

import android.app.Application
import com.bigong.oguri.core.network.KAKAO_NATIVE_APP_KEY
import com.bigong.oguri.core.platform.OguriPlatformContextHolder
import com.kakao.sdk.common.KakaoSdk
import com.google.firebase.crashlytics.FirebaseCrashlytics

class OguriApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OguriPlatformContextHolder.applicationContext = applicationContext
        FirebaseCrashlytics
            .getInstance()
            .setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        if (KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(applicationContext, KAKAO_NATIVE_APP_KEY)
        }
    }
}
