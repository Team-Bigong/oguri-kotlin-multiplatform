package com.bigong.oguri.core.deeplink

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface DeepLinkStore {
    val incomingUrl: StateFlow<String?>

    fun publish(urlText: String)

    fun clearConsumed(urlText: String)
}

class DefaultDeepLinkStore : DeepLinkStore {
    private val mutableIncomingUrl = MutableStateFlow<String?>(null)
    override val incomingUrl: StateFlow<String?> = mutableIncomingUrl.asStateFlow()

    override fun publish(urlText: String) {
        if (urlText.isBlank()) {
            return
        }
        mutableIncomingUrl.value = urlText
    }

    override fun clearConsumed(urlText: String) {
        if (mutableIncomingUrl.value == urlText) {
            mutableIncomingUrl.value = null
        }
    }
}
