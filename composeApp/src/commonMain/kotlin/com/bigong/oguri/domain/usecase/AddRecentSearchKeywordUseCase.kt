package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.SearchRepository
import dev.zacsweers.metro.Inject

@Inject
class AddRecentSearchKeywordUseCase(
    private val searchRepository: SearchRepository,
) {
    operator fun invoke(keyword: String): List<String> = searchRepository.addRecentSearchKeyword(keyword = keyword)
}
