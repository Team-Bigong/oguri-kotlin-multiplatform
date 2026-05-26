package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.MonthlyTopPeriod
import com.bigong.oguri.domain.repository.HomeRepository
import dev.zacsweers.metro.Inject

@Inject
class GetMonthlyTopPeriodListUseCase(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(): List<MonthlyTopPeriod> = homeRepository.getMonthlyTopPeriods()
}
