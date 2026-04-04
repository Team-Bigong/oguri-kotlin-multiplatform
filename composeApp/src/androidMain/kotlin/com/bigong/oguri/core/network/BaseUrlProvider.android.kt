package com.bigong.oguri.core.network

import android.content.pm.ApplicationInfo
import com.bigong.oguri.core.platform.OguriPlatformContextHolder

internal actual fun isDebugPlatformBuild(): Boolean {
    val applicationContext = OguriPlatformContextHolder.applicationContext ?: return false
    return (applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
