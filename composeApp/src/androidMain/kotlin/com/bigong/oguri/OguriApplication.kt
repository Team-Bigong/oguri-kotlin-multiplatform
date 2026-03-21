package com.bigong.oguri

import android.app.Application
import android.content.pm.ApplicationInfo
import com.bigong.oguri.core.network.KAKAO_NATIVE_APP_KEY
import com.bigong.oguri.core.platform.OguriPlatformContextHolder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kakao.sdk.common.KakaoSdk

class OguriApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OguriPlatformContextHolder.applicationContext = applicationContext
        val isDebuggableApp: Boolean =
            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        FirebaseCrashlytics
            .getInstance()
            .isCrashlyticsCollectionEnabled = !isDebuggableApp
        if (KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(applicationContext, KAKAO_NATIVE_APP_KEY)
        }
    }
}
