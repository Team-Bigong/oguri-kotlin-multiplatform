package com.bigong.oguri.feature.search.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.platform.PlatformBackHandler
import dev.zacsweers.metro.Provider
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.search_content_description_back
import oguri.composeapp.generated.resources.search_content_description_delete_recent
import oguri.composeapp.generated.resources.search_content_description_search
import oguri.composeapp.generated.resources.search_empty_recent_keyword
import oguri.composeapp.generated.resources.search_empty_result_message
import oguri.composeapp.generated.resources.search_hint_place
import oguri.composeapp.generated.resources.search_section_popular
import oguri.composeapp.generated.resources.search_section_recent
import oguri.composeapp.generated.resources.search_section_recommended_week
import oguri.composeapp.generated.resources.search_suggest_destination_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchRoute(
    searchViewModelProvider: Provider<SearchViewModel>,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPlaceClick: (Long) -> Unit,
    onSuggestionClick: () -> Unit,
) {
    val searchViewModel =
        remember {
            searchViewModelProvider()
        }
    val searchUiState = searchViewModel.uiState.collectAsStateWithLifecycle().value
    val popularSearchKeywords: List<String> =
        listOf(
            "보라카이",
            "멜버른",
            "바르셀로나",
            "싸이판",
            "샌프란시스코",
            "니스",
        )

    PlatformBackHandler(
        enabled = searchUiState.searchQueryText.isNotBlank(),
        onBack = searchViewModel::clearSearchQueryText,
    )

    SearchScreen(
        searchHintText = stringResource(Res.string.search_hint_place),
        recentSearchTitleText = stringResource(Res.string.search_section_recent),
        emptyRecentSearchText = stringResource(Res.string.search_empty_recent_keyword),
        popularSearchTitleText = stringResource(Res.string.search_section_popular),
        recommendedPlaceTitleText = stringResource(Res.string.search_section_recommended_week),
        recentSearchKeywords = searchUiState.recentSearchKeywords,
        popularSearchKeywords = popularSearchKeywords,
        recommendedPlaces = searchUiState.recommendedPlaces,
        searchResults = searchUiState.searchResults,
        isSearchLoading = searchUiState.isSearchLoading,
        isSearchError = searchUiState.isSearchError,
        emptyResultMessageText = stringResource(Res.string.search_empty_result_message),
        suggestDestinationText = stringResource(Res.string.search_suggest_destination_button),
        backContentDescriptionText = stringResource(Res.string.search_content_description_back),
        searchContentDescriptionText = stringResource(Res.string.search_content_description_search),
        deleteRecentSearchContentDescriptionText =
            stringResource(Res.string.search_content_description_delete_recent),
        searchQueryText = searchUiState.searchQueryText,
        onBackClick = {
            if (searchUiState.searchQueryText.isNotBlank()) {
                searchViewModel.clearSearchQueryText()
            } else {
                onBackClick()
            }
        },
        onSearchQueryTextChange = searchViewModel::changeSearchQueryText,
        onSearchClick = {
            searchViewModel.retrySearch()
            onSearchClick()
        },
        onSearchRetryClick = searchViewModel::retrySearch,
        onSuggestionClick = onSuggestionClick,
        onSearchResultClick = { searchResult ->
            searchViewModel.addRecentSearchKeyword(keyword = searchResult.destinationName) {
                onPlaceClick(searchResult.id)
            }
        },
        onRecentSearchKeywordClick = { keyword ->
            searchViewModel.changeSearchQueryText(searchQueryText = keyword)
            searchViewModel.addRecentSearchKeyword(keyword = keyword)
        },
        onRecentSearchDeleteClick = searchViewModel::deleteRecentSearchKeyword,
        onPopularSearchKeywordClick = { keyword ->
            searchViewModel.changeSearchQueryText(searchQueryText = keyword)
        },
        onPlaceClick = { place ->
            searchViewModel.addRecentSearchKeyword(keyword = place.city) {
                onPlaceClick(place.id)
            }
        },
    )
}
