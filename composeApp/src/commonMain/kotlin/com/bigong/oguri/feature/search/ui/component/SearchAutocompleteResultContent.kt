package com.bigong.oguri.feature.search.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.SearchAutocomplete
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_right_arrow
import oguri.composeapp.generated.resources.img_oguri_empty
import org.jetbrains.compose.resources.painterResource

@Composable
fun SearchAutocompleteResultContent(
    searchQueryText: String,
    searchResults: List<SearchAutocomplete>,
    isSearchLoading: Boolean,
    isSearchError: Boolean,
    emptyResultMessageText: String,
    suggestDestinationText: String,
    onRetryClick: () -> Unit,
    onSuggestionClick: () -> Unit,
    onSearchResultClick: (SearchAutocomplete) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isSearchError -> {
            NetworkErrorRetryContent(
                onRetryClick = onRetryClick,
                modifier = modifier,
            )
        }

        searchResults.isEmpty() && !isSearchLoading -> {
            SearchEmptyResultContent(
                emptyResultMessageText = emptyResultMessageText,
                suggestDestinationText = suggestDestinationText,
                onSuggestionClick = onSuggestionClick,
                modifier = modifier,
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
            ) {
                items(
                    items = searchResults,
                    key = { searchResult -> searchResult.id },
                ) { searchResult ->
                    SearchAutocompleteResultRow(
                        searchQueryText = searchQueryText,
                        searchResult = searchResult,
                        onClick = { onSearchResultClick(searchResult) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchAutocompleteResultRow(
    searchQueryText: String,
    searchResult: SearchAutocomplete,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = onClick)
                .padding(start = 24.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(weight = 1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    searchResult.destinationName.highlightQuery(
                        query = searchQueryText,
                        baseStyle = OguriTheme.typography.cardSubtitle,
                    ),
                style = OguriTheme.typography.cardSubtitle,
                color = Neutral90,
            )
            Text(
                text =
                    "・ ${searchResult.countryName}".highlightQuery(
                        query = searchQueryText,
                        baseStyle = OguriTheme.typography.labelMedium.copy(color = Neutral50),
                    ),
                style = OguriTheme.typography.labelMedium,
                color = Neutral50,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Image(
            painter = painterResource(Res.drawable.ic_right_arrow),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Neutral90),
            modifier =
                Modifier
                    .size(size = 48.dp)
                    .padding(all = 12.dp),
        )
    }
}

@Composable
private fun SearchEmptyResultContent(
    emptyResultMessageText: String,
    suggestDestinationText: String,
    onSuggestionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_oguri_empty),
            contentDescription = null,
            modifier = Modifier.height(220.dp),
        )
        Spacer(modifier = Modifier.size(size = 12.dp))
        Text(
            text = emptyResultMessageText,
            style = OguriTheme.typography.cardTitle,
            color = Neutral90,
        )
        Spacer(modifier = Modifier.size(size = 24.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height = 48.dp)
                    .background(color = Mint70, shape = RoundedCornerShape(8.dp))
                    .noRippleClickable(onClick = onSuggestionClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = suggestDestinationText,
                style = OguriTheme.typography.cardTitle,
                color = Neutral0,
            )
        }
    }
}

private fun String.highlightQuery(
    query: String,
    baseStyle: TextStyle,
): AnnotatedString {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) {
        return AnnotatedString(this)
    }

    val targetText = this
    val targetTextLowercase = targetText.lowercase()
    val queryLowercase = normalizedQuery.lowercase()
    val highlightStyle = baseStyle.copy(color = Mint70).toSpanStyle()

    return buildAnnotatedString {
        append(targetText)

        var startIndex = targetTextLowercase.indexOf(queryLowercase)
        while (startIndex != -1) {
            addStyle(
                style = highlightStyle,
                start = startIndex,
                end = startIndex + normalizedQuery.length,
            )
            startIndex = targetTextLowercase.indexOf(queryLowercase, startIndex + normalizedQuery.length)
        }
    }
}
