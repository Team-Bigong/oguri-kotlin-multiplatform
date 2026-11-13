package com.bigong.oguri.feature.travelplanner.ui.model

import kotlin.time.Instant

internal enum class TravelModifiedUnit { JUST_NOW, MINUTES, HOURS, DAYS }

internal data class TravelModifiedAge(
    val unit: TravelModifiedUnit,
    val count: Long = 0,
)

internal fun travelModifiedAge(
    modifiedAt: Instant,
    now: Instant,
): TravelModifiedAge {
    val age = (now - modifiedAt).inWholeMinutes.coerceAtLeast(0)
    return when {
        age == 0L -> TravelModifiedAge(TravelModifiedUnit.JUST_NOW)
        age < MINUTES_PER_HOUR -> TravelModifiedAge(TravelModifiedUnit.MINUTES, age)
        age < MINUTES_PER_DAY -> TravelModifiedAge(TravelModifiedUnit.HOURS, age / MINUTES_PER_HOUR)
        else -> TravelModifiedAge(TravelModifiedUnit.DAYS, age / MINUTES_PER_DAY)
    }
}

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
