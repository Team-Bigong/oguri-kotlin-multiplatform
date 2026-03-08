package com.bigong.oguri.core.util.extension

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.bigong.oguri.core.util.HapticType

actual fun HapticType.perform() {
    val applicationContext: Context = resolveApplicationContext() ?: return
    val vibrator: Vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (!vibrator.hasVibrator()) {
        return
    }

    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(createVibrationEffect(this))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(legacyDurationMillis(this))
        }
    }
}

private fun createVibrationEffect(hapticType: HapticType): VibrationEffect {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        when (hapticType) {
            HapticType.Selection -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            HapticType.ImpactLight -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            HapticType.ImpactMedium -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            HapticType.NotificationSuccess -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
        }
    } else {
        VibrationEffect.createOneShot(legacyDurationMillis(hapticType), VibrationEffect.DEFAULT_AMPLITUDE)
    }
}

private fun legacyDurationMillis(hapticType: HapticType): Long {
    return when (hapticType) {
        HapticType.Selection -> 10L
        HapticType.ImpactLight -> 14L
        HapticType.ImpactMedium -> 20L
        HapticType.NotificationSuccess -> 26L
    }
}

private fun resolveApplicationContext(): Context? {
    return runCatching {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentApplicationMethod = activityThreadClass.getMethod("currentApplication")
        currentApplicationMethod.invoke(null) as? Application
    }.getOrNull()
}
