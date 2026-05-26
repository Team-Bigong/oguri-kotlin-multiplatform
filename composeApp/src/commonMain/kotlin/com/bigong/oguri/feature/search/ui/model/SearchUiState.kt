package com.bigong.oguri.feature.search.ui.model

import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.SearchAutocomplete

data class SearchUiState(
    val searchQueryText: String = "",
    val recentSearchKeywords: List<String> = emptyList(),
    val recommendedPlaces: List<Place> = emptyList(),
    val searchResults: List<SearchAutocomplete> = emptyList(),
    val isSearchLoading: Boolean = false,
    val isSearchError: Boolean = false,
)
