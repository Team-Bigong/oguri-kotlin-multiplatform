package com.bigong.oguri.core.deeplink

import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal const val TRAVEL_INVITE_HOST: String = "oguri-kotlin-multiplatform-19hx.onrender.com"
private val inviteTokenPattern: Regex = Regex("[A-Za-z0-9_-]{43}")

internal fun travelInviteLink(token: String): String {
    require(inviteTokenPattern.matches(token))
    return "https://$TRAVEL_INVITE_HOST/trip-invite#$token"
}

internal fun parseTravelInvite(urlText: String): String? {
    val url = runCatching { Url(urlText) }.getOrNull() ?: return null
    val path = url.segments.filter(String::isNotEmpty)
    val isWeb = url.protocol.name == "https" && url.host == TRAVEL_INVITE_HOST && url.port == 443 && path == listOf("trip-invite")
    val isApp = url.protocol.name == "oguri" && url.host == "open" && path == listOf("trip-invite")
    return url.fragment.takeIf { (isWeb || isApp) && inviteTokenPattern.matches(it) }
}

internal object TravelInviteIntent {
    private val mutableToken: MutableStateFlow<String?> = MutableStateFlow(null)
    val token: StateFlow<String?> = mutableToken.asStateFlow()

    fun receive(token: String) {
        mutableToken.value = token
    }

    fun clear(token: String) {
        mutableToken.compareAndSet(token, null)
    }
}
