package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.repository.HomeRepository
import dev.zacsweers.metro.Inject

@Inject
class GetWeeklyTopPlaceListUseCase(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(): List<Place> = homeRepository.getWeeklyTopPlaces()
}
