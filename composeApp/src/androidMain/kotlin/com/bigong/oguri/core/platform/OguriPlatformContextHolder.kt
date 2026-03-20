package com.bigong.oguri.core.platform

import android.content.Context
import androidx.activity.ComponentActivity

object OguriPlatformContextHolder {
    var applicationContext: Context? = null
    var currentActivity: ComponentActivity? = null
}
