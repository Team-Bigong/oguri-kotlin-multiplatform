package com.bigong.oguri.core.deeplink

private val deepLinkStoreSingleton: DeepLinkStore = DefaultDeepLinkStore()

fun provideDeepLinkStore(): DeepLinkStore = deepLinkStoreSingleton

fun handleIncomingAppUrl(urlText: String) {
    provideDeepLinkStore().publish(urlText = urlText)
}
