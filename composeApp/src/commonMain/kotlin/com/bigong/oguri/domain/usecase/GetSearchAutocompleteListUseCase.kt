package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.SearchAutocomplete
import com.bigong.oguri.domain.repository.SearchRepository
import dev.zacsweers.metro.Inject

@Inject
class GetSearchAutocompleteListUseCase(
    private val searchRepository: SearchRepository,
) {
    suspend operator fun invoke(query: String): List<SearchAutocomplete> = searchRepository.getSearchAutocompletes(query = query)
}
