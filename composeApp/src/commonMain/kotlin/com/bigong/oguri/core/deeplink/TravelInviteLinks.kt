package com.bigong.oguri.core.deeplink

import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal const val TRAVEL_INVITE_HOST: String = "www.oguri.kr"
private val travelInviteHosts = setOf(TRAVEL_INVITE_HOST, "oguri.kr", "oguri-kotlin-multiplatform-19hx.onrender.com")
private val inviteTokenPattern: Regex = Regex("[A-Za-z0-9_-]{43}")

internal fun travelInviteLink(token: String): String {
    require(inviteTokenPattern.matches(token))
    return "https://$TRAVEL_INVITE_HOST/trip-invite/#$token"
}

internal fun parseTravelInvite(urlText: String): String? {
    val incomingUrl = runCatching { Url(urlText) }.getOrNull() ?: return null
    val url =
        if (incomingUrl.protocol.name.startsWith("kakao") && incomingUrl.host == "kakaolink") {
            val nestedUrl = incomingUrl.parameters["deeplink"] ?: return null
            runCatching { Url(nestedUrl) }.getOrNull() ?: return null
        } else {
            incomingUrl
        }
    val path = url.segments.filter(String::isNotEmpty)
    val isWeb = url.protocol.name == "https" && url.host in travelInviteHosts && url.port == 443 && path == listOf("trip-invite")
    val isApp = url.protocol.name == "oguri" && url.host == "open" && path == listOf("trip-invite")
    return url.fragment.takeIf { (isWeb || isApp) && inviteTokenPattern.matches(it) }
}

internal object TravelInviteIntent {
    private val mutableToken: MutableStateFlow<String?> = MutableStateFlow(null)
    val token: StateFlow<String?> = mutableToken.asStateFlow()

    fun receive(token: String) {
        mutableToken.value = token
    }

    fun clear(token: String): Boolean = mutableToken.compareAndSet(token, null)
}
