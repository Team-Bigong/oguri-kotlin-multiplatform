package com.bigong.oguri.core.deeplink

import com.bigong.oguri.core.navigation.RouteModel
import io.ktor.http.Url

private const val SCHEME_OGURI = "oguri"
private const val SCHEME_KAKAO_PREFIX = "kakao"
private const val DEEP_LINK_HOST = "open"
private const val KAKAO_LINK_HOST = "kakaolink"
private const val DEEP_LINK_PATH_PLACE = "place"
private const val DEEP_LINK_PATH_PERIOD = "period"

private const val QUERY_KEY_PLACE_ID = "placeId"
private const val QUERY_KEY_START_DATE = "startDate"
private const val QUERY_KEY_END_DATE = "endDate"
private const val QUERY_KEY_DEEP_LINK = "deeplink"

fun parseAppDeepLinkRoute(urlText: String): RouteModel? {
    val url = runCatching { Url(urlText) }.getOrNull() ?: return null
    val scheme = url.protocol.name.lowercase()
    val host = url.host.lowercase()
    val pathSegments = url.segments.filter { pathSegment -> pathSegment.isNotBlank() }

    return when {
        scheme == SCHEME_OGURI -> parseOguriScheme(host = host, pathSegments = pathSegments, url = url)
        inKakaoScheme(scheme) -> parseKakaoScheme(host = host, url = url)
        else -> null
    }
}

private fun inKakaoScheme(scheme: String): Boolean = scheme.startsWith(SCHEME_KAKAO_PREFIX)

private fun parseOguriScheme(
    host: String,
    pathSegments: List<String>,
    url: Url,
): RouteModel? {
    if (host != DEEP_LINK_HOST) {
        return null
    }
    val routeType = pathSegments.firstOrNull() ?: return null
    return parseRouteByType(routeType = routeType, url = url)
}

private fun parseKakaoScheme(
    host: String,
    url: Url,
): RouteModel? {
    if (host != KAKAO_LINK_HOST) {
        return null
    }
    val deepLinkUrl = url.parameters[QUERY_KEY_DEEP_LINK] ?: return null
    return parseAppDeepLinkRoute(urlText = deepLinkUrl)
}

private fun parseRouteByType(
    routeType: String,
    url: Url,
): RouteModel? =
    when (routeType) {
        DEEP_LINK_PATH_PLACE -> {
            val placeId = url.parameters[QUERY_KEY_PLACE_ID]?.toLongOrNull() ?: return null
            val startDate = url.parameters[QUERY_KEY_START_DATE]
            val endDate = url.parameters[QUERY_KEY_END_DATE]
            RouteModel.PlaceDetail(
                placeId = placeId,
                startDate = startDate,
                endDate = endDate,
            )
        }

        DEEP_LINK_PATH_PERIOD -> {
            val startDate = url.parameters[QUERY_KEY_START_DATE] ?: return null
            val endDate = url.parameters[QUERY_KEY_END_DATE] ?: return null
            RouteModel.PeriodDetail(
                startDate = startDate,
                endDate = endDate,
            )
        }

        else -> {
            null
        }
    }
