package com.bigong.oguri.feature.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.PlaceHorizontalCarousel
import com.bigong.oguri.core.util.extension.dismissKeyboardOnOutsideTouch
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.SearchAutocomplete
import com.bigong.oguri.feature.search.ui.component.PopularSearchKeywordChips
import com.bigong.oguri.feature.search.ui.component.RecentSearchKeywordChips
import com.bigong.oguri.feature.search.ui.component.SearchAutocompleteResultContent
import com.bigong.oguri.feature.search.ui.component.SearchSectionHeader
import com.bigong.oguri.feature.search.ui.component.SearchTopBar
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_plane
import oguri.composeapp.generated.resources.ic_search_recent
import oguri.composeapp.generated.resources.ic_search_trend

@Composable
fun SearchScreen(
    searchHintText: String,
    recentSearchTitleText: String,
    emptyRecentSearchText: String,
    popularSearchTitleText: String,
    recommendedPlaceTitleText: String,
    recentSearchKeywords: List<String>,
    popularSearchKeywords: List<String>,
    recommendedPlaces: List<Place>,
    searchResults: List<SearchAutocomplete>,
    isSearchLoading: Boolean,
    isSearchError: Boolean,
    emptyResultMessageText: String,
    suggestDestinationText: String,
    backContentDescriptionText: String,
    searchContentDescriptionText: String,
    deleteRecentSearchContentDescriptionText: String,
    searchQueryText: String,
    onBackClick: () -> Unit,
    onSearchQueryTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSearchRetryClick: () -> Unit,
    onSuggestionClick: () -> Unit,
    onSearchResultClick: (SearchAutocomplete) -> Unit,
    onRecentSearchKeywordClick: (String) -> Unit,
    onRecentSearchDeleteClick: (String) -> Unit,
    onPopularSearchKeywordClick: (String) -> Unit,
    onPlaceClick: (Place) -> Unit,
) {
    val searchTextFieldFocusRequester: FocusRequester = remember { FocusRequester() }
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val onSearchKeywordClick: (String) -> Unit = { keyword ->
        focusManager.clearFocus(force = true)
        softwareKeyboardController?.hide()
        onRecentSearchKeywordClick(keyword)
    }
    val onPopularKeywordClick: (String) -> Unit = { keyword ->
        focusManager.clearFocus(force = true)
        softwareKeyboardController?.hide()
        onPopularSearchKeywordClick(keyword)
    }

    LaunchedEffect(Unit) {
        searchTextFieldFocusRequester.requestFocus()
        softwareKeyboardController?.show()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(color = Neutral5)
                .dismissKeyboardOnOutsideTouch(),
    ) {
        SearchTopBar(
            searchHintText = searchHintText,
            backContentDescriptionText = backContentDescriptionText,
            searchContentDescriptionText = searchContentDescriptionText,
            searchQueryText = searchQueryText,
            searchTextFieldFocusRequester = searchTextFieldFocusRequester,
            onBackClick = onBackClick,
            onSearchQueryTextChange = onSearchQueryTextChange,
            onSearchClick = onSearchClick,
        )
        if (searchQueryText.isBlank()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp),
            ) {
                item {
                    Spacer(modifier = Modifier.size(size = 16.dp))
                }
                item {
                    SearchSectionHeader(
                        titleText = recentSearchTitleText,
                        iconResource = Res.drawable.ic_search_recent,
                    )
                    Spacer(modifier = Modifier.size(size = 16.dp))
                }
                item {
                    if (recentSearchKeywords.isEmpty()) {
                        Text(
                            text = emptyRecentSearchText,
                            style = OguriTheme.typography.bodyMedium,
                            color = Neutral40,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    } else {
                        RecentSearchKeywordChips(
                            recentSearchKeywords = recentSearchKeywords,
                            onRecentSearchKeywordClick = onSearchKeywordClick,
                            onRecentSearchDeleteClick = onRecentSearchDeleteClick,
                            deleteRecentSearchContentDescriptionText = deleteRecentSearchContentDescriptionText,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(size = 26.dp))
                }
                item {
                    SearchSectionHeader(
                        titleText = popularSearchTitleText,
                        iconResource = Res.drawable.ic_search_trend,
                    )
                    Spacer(modifier = Modifier.size(size = 16.dp))
                }
                item {
                    PopularSearchKeywordChips(
                        popularSearchKeywords = popularSearchKeywords,
                        onPopularSearchKeywordClick = onPopularKeywordClick,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    Spacer(modifier = Modifier.size(size = 26.dp))
                }
                item {
                    SearchSectionHeader(
                        titleText = recommendedPlaceTitleText,
                        iconResource = Res.drawable.ic_plane,
                    )
                    Spacer(modifier = Modifier.size(size = 16.dp))
                }
                item {
                    PlaceHorizontalCarousel(
                        places = recommendedPlaces,
                        onPlaceClick = onPlaceClick,
                    )
                }
            }
        } else {
            SearchAutocompleteResultContent(
                searchQueryText = searchQueryText,
                searchResults = searchResults,
                isSearchLoading = isSearchLoading,
                isSearchError = isSearchError,
                emptyResultMessageText = emptyResultMessageText,
                suggestDestinationText = suggestDestinationText,
                onRetryClick = onSearchRetryClick,
                onSuggestionClick = onSuggestionClick,
                onSearchResultClick = onSearchResultClick,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
            )
        }
    }
}
