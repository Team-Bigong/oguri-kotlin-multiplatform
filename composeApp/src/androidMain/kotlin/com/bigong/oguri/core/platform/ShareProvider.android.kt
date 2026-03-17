package com.bigong.oguri.core.platform

import android.content.Intent

actual fun shareText(text: String) {
    if (text.isBlank()) {
        return
    }

    val applicationContext = OguriPlatformContextHolder.applicationContext ?: return
    val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    val chooserIntent =
        Intent.createChooser(shareIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    applicationContext.startActivity(chooserIntent)
}
