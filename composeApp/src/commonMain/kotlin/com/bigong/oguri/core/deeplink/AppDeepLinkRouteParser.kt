package com.bigong.oguri.core.deeplink

import com.bigong.oguri.core.navigation.RouteModel
import io.ktor.http.Url

private const val SCHEME_HTTPS = "https"
private const val SCHEME_HTTP = "http"
private const val SCHEME_OGURI = "oguri"
private const val DEEP_LINK_HOST = "open"
private const val DEEP_LINK_PATH_OPEN = "open"
private const val DEEP_LINK_PATH_PLACE = "place"
private const val DEEP_LINK_PATH_PERIOD = "period"

private const val QUERY_KEY_PLACE_ID = "placeId"
private const val QUERY_KEY_START_DATE = "startDate"
private const val QUERY_KEY_END_DATE = "endDate"

private val supportedDeepLinkHosts =
    setOf(
        "oguri-kotlin-multiplatform-wezq.onrender.com",
        "oguri-kotlin-multiplatform.onrender.com",
    )

fun parseAppDeepLinkRoute(urlText: String): RouteModel? {
    val url = runCatching { Url(urlText) }.getOrNull() ?: return null
    val scheme = url.protocol.name.lowercase()
    val host = url.host.lowercase()
    val pathSegments = url.segments.filter { pathSegment -> pathSegment.isNotBlank() }

    return when (scheme) {
        SCHEME_OGURI -> parseOguriScheme(host = host, pathSegments = pathSegments, url = url)
        SCHEME_HTTPS,
        SCHEME_HTTP,
        -> parseHttpScheme(host = host, pathSegments = pathSegments, url = url)
        else -> null
    }
}

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

private fun parseHttpScheme(
    host: String,
    pathSegments: List<String>,
    url: Url,
): RouteModel? {
    if (host !in supportedDeepLinkHosts) {
        return null
    }
    if (pathSegments.size < 2 || pathSegments[0] != DEEP_LINK_PATH_OPEN) {
        return null
    }
    val routeType = pathSegments[1]
    return parseRouteByType(routeType = routeType, url = url)
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

        else -> null
    }
