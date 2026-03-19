package com.bigong.oguri.domain.usecase

import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

@Inject
class CalculateDDayUseCase {
    operator fun invoke(
        todayDate: LocalDate,
        targetDate: LocalDate,
    ): Int = todayDate.daysUntil(targetDate)
}
