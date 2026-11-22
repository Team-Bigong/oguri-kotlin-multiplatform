package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelDates
import kotlinx.datetime.LocalDate

internal data class TravelDateSelection(val start: LocalDate? = null, val end: LocalDate? = null) {
    val dates: TravelDates?
        get() {
            val first = start ?: return null
            val last = end ?: return null
            return runCatching { TravelDates(first, last) }.getOrNull()
        }

    fun select(date: LocalDate): TravelDateSelection =
        if (start == null || end != null || date < start) TravelDateSelection(date) else copy(end = date)
}
