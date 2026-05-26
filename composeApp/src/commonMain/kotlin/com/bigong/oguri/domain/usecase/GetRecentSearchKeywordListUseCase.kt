package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.SearchRepository
import dev.zacsweers.metro.Inject

@Inject
class GetRecentSearchKeywordListUseCase(
    private val searchRepository: SearchRepository,
) {
    operator fun invoke(): List<String> = searchRepository.getRecentSearchKeywords()
}
