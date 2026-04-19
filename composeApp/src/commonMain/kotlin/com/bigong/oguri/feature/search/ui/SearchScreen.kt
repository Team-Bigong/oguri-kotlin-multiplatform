package com.bigong.oguri.feature.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.ui.component.PlaceHorizontalCarousel
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.feature.search.ui.component.PopularSearchKeywordChips
import com.bigong.oguri.feature.search.ui.component.RecentSearchKeywordChips
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
    popularSearchTitleText: String,
    recommendedPlaceTitleText: String,
    recentSearchKeywords: List<String>,
    popularSearchKeywords: List<String>,
    recommendedPlaces: List<Place>,
    backContentDescriptionText: String,
    searchContentDescriptionText: String,
    deleteRecentSearchContentDescriptionText: String,
    searchQueryText: String,
    onBackClick: () -> Unit,
    onSearchQueryTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onRecentSearchKeywordClick: (String) -> Unit,
    onRecentSearchDeleteClick: (String) -> Unit,
    onPopularSearchKeywordClick: (String) -> Unit,
    onPlaceClick: (Place) -> Unit,
) {
    val searchTextFieldFocusRequester: FocusRequester = remember { FocusRequester() }
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        searchTextFieldFocusRequester.requestFocus()
        softwareKeyboardController?.show()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(color = Neutral5),
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
                RecentSearchKeywordChips(
                    recentSearchKeywords = recentSearchKeywords,
                    onRecentSearchKeywordClick = onRecentSearchKeywordClick,
                    onRecentSearchDeleteClick = onRecentSearchDeleteClick,
                    deleteRecentSearchContentDescriptionText = deleteRecentSearchContentDescriptionText,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
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
                    onPopularSearchKeywordClick = onPopularSearchKeywordClick,
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
    }
}
