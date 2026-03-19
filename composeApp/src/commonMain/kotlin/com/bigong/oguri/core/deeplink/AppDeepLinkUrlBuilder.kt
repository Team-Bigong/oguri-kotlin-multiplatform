package com.bigong.oguri.core.deeplink

private const val APP_SCHEME = "oguri"
private const val DEEP_LINK_HOST = "open"
private const val PLACE_PATH = "place"
private const val PERIOD_PATH = "period"

private const val QUERY_KEY_PLACE_ID = "placeId"
private const val QUERY_KEY_START_DATE = "startDate"
private const val QUERY_KEY_END_DATE = "endDate"

fun buildPlaceDetailDeepLink(
    placeId: Long,
    startDate: String?,
    endDate: String?,
): String {
    val queryParameters =
        mutableListOf(
            "$QUERY_KEY_PLACE_ID=$placeId",
        ).apply {
            if (!startDate.isNullOrBlank()) {
                add("$QUERY_KEY_START_DATE=$startDate")
            }
            if (!endDate.isNullOrBlank()) {
                add("$QUERY_KEY_END_DATE=$endDate")
            }
        }

    return "$APP_SCHEME://$DEEP_LINK_HOST/$PLACE_PATH?${queryParameters.joinToString("&")}"
}

fun buildPeriodDetailDeepLink(
    startDate: String,
    endDate: String,
): String = "$APP_SCHEME://$DEEP_LINK_HOST/$PERIOD_PATH?$QUERY_KEY_START_DATE=$startDate&$QUERY_KEY_END_DATE=$endDate"
