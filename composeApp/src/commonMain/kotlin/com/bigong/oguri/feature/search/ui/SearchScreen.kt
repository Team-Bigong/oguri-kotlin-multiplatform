package com.bigong.oguri.feature.search.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.PlaceHorizontalCarousel
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.Place
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_back
import oguri.composeapp.generated.resources.btn_search
import oguri.composeapp.generated.resources.ic_plane
import oguri.composeapp.generated.resources.ic_search_recent
import oguri.composeapp.generated.resources.ic_search_trend
import oguri.composeapp.generated.resources.ic_trashcan
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
                KeywordSectionHeader(
                    titleText = recentSearchTitleText,
                    iconResource = Res.drawable.ic_search_recent,
                )
                Spacer(modifier = Modifier.size(size = 16.dp))
            }
            item {
                RecentKeywordChips(
                    recentSearchKeywords = recentSearchKeywords,
                    onRecentSearchKeywordClick = onRecentSearchKeywordClick,
                    onRecentSearchDeleteClick = onRecentSearchDeleteClick,
                    deleteRecentSearchContentDescriptionText = deleteRecentSearchContentDescriptionText,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.size(size = 26.dp))
            }
            item {
                KeywordSectionHeader(
                    titleText = popularSearchTitleText,
                    iconResource = Res.drawable.ic_search_trend,
                )
                Spacer(modifier = Modifier.size(size = 16.dp))
            }
            item {
                PopularKeywordChips(
                    popularSearchKeywords = popularSearchKeywords,
                    onPopularSearchKeywordClick = onPopularSearchKeywordClick,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.size(size = 26.dp))
            }
            item {
                KeywordSectionHeader(
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

@Composable
private fun SearchTopBar(
    searchHintText: String,
    backContentDescriptionText: String,
    searchContentDescriptionText: String,
    searchQueryText: String,
    onBackClick: () -> Unit,
    onSearchQueryTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 12.dp),
    ) {
        Image(
            painter = painterResource(resource = Res.drawable.btn_back),
            contentDescription = backContentDescriptionText,
            colorFilter = ColorFilter.tint(color = Neutral100),
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(vertical = 10.dp)
                    .noRippleClickable(onClick = onBackClick),
        )
        BasicTextField(
            value = searchQueryText,
            onValueChange = onSearchQueryTextChange,
            textStyle = OguriTheme.typography.bodyLarge.copy(color = Neutral100),
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 36.dp, end = 48.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (searchQueryText.isBlank()) {
                    Text(
                        text = searchHintText,
                        style = OguriTheme.typography.cardSubtitle,
                        color = Neutral40,
                    )
                }
                innerTextField()
            },
        )
        Image(
            painter = painterResource(resource = Res.drawable.btn_search),
            contentDescription = searchContentDescriptionText,
            colorFilter = ColorFilter.tint(color = Neutral100),
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(size = 32.dp)
                    .noRippleClickable(onClick = onSearchClick)
                    .padding(all = 4.dp),
        )
    }
}

@Composable
private fun KeywordSectionHeader(
    titleText: String,
    iconResource: DrawableResource,
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titleText,
            style = OguriTheme.typography.cardSubtitle,
            color = Neutral100,
        )
        Image(
            painter = painterResource(resource = iconResource),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = Neutral70),
            modifier = Modifier.size(size = 24.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentKeywordChips(
    recentSearchKeywords: List<String>,
    onRecentSearchKeywordClick: (String) -> Unit,
    onRecentSearchDeleteClick: (String) -> Unit,
    deleteRecentSearchContentDescriptionText: String,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
    ) {
        recentSearchKeywords.forEach { keyword ->
            Row(
                modifier =
                    Modifier
                        .border(width = 1.dp, color = Neutral50, shape = OguriChipShape)
                        .noRippleClickable(onClick = { onRecentSearchKeywordClick(keyword) })
                        .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
            ) {
                Text(
                    text = keyword,
                    style = OguriTheme.typography.bodyMedium,
                    color = Neutral70,
                )
                Image(
                    painter = painterResource(resource = Res.drawable.ic_trashcan),
                    contentDescription = deleteRecentSearchContentDescriptionText,
                    colorFilter = ColorFilter.tint(color = Neutral50),
                    modifier =
                        Modifier
                            .size(size = 14.dp)
                            .noRippleClickable(onClick = { onRecentSearchDeleteClick(keyword) }),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PopularKeywordChips(
    popularSearchKeywords: List<String>,
    onPopularSearchKeywordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
    ) {
        popularSearchKeywords.forEach { keyword ->
            Text(
                text = keyword,
                style = OguriTheme.typography.bodyMedium,
                color = Neutral70,
                modifier =
                    Modifier
                        .border(width = 1.dp, color = Neutral50, shape = OguriChipShape)
                        .noRippleClickable(onClick = { onPopularSearchKeywordClick(keyword) })
                        .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

private val OguriChipShape = RoundedCornerShape(size = 8.dp)
