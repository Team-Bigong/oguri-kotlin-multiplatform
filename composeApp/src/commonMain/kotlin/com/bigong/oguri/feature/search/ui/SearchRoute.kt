package com.bigong.oguri.feature.search.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bigong.oguri.domain.model.Place
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.search_content_description_back
import oguri.composeapp.generated.resources.search_content_description_delete_recent
import oguri.composeapp.generated.resources.search_content_description_search
import oguri.composeapp.generated.resources.search_hint_place
import oguri.composeapp.generated.resources.search_section_popular
import oguri.composeapp.generated.resources.search_section_recent
import oguri.composeapp.generated.resources.search_section_recommended_month
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchRoute(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPlaceClick: (Long) -> Unit,
) {
    var searchQueryText by remember { mutableStateOf("") }
    val thumbnailImageUrl: String = DUMMY_PLACE_IMAGE_URL
    val recentSearchKeywords: List<String> = listOf("보라카이", "멜버른", "바르셀로나")
    val popularSearchKeywords: List<String> =
        listOf(
            "보라카이",
            "멜버른",
            "바르셀로나",
            "싸이판",
            "샌프란시스코",
            "니스",
        )
    val recommendedPlaces: List<Place> =
        listOf(
            Place(
                id = 1L,
                country = "필리핀",
                city = "보라카이",
                summary = "화이트 비치 물빛이 가장 또렷해지는 시기예요",
                thumbnailUrl = thumbnailImageUrl,
            ),
            Place(
                id = 2L,
                country = "스페인",
                city = "바르셀로나",
                summary = "가우디 건축과 바다 산책을 함께 즐기기 좋아요",
                thumbnailUrl = thumbnailImageUrl,
            ),
            Place(
                id = 3L,
                country = "미국",
                city = "샌프란시스코",
                summary = "언덕과 바다 풍경이 가장 또렷해지는 시기예요",
                thumbnailUrl = thumbnailImageUrl,
            ),
        )

    SearchScreen(
        searchHintText = stringResource(Res.string.search_hint_place),
        recentSearchTitleText = stringResource(Res.string.search_section_recent),
        popularSearchTitleText = stringResource(Res.string.search_section_popular),
        recommendedPlaceTitleText = stringResource(Res.string.search_section_recommended_month),
        recentSearchKeywords = recentSearchKeywords,
        popularSearchKeywords = popularSearchKeywords,
        recommendedPlaces = recommendedPlaces,
        backContentDescriptionText = stringResource(Res.string.search_content_description_back),
        searchContentDescriptionText = stringResource(Res.string.search_content_description_search),
        deleteRecentSearchContentDescriptionText =
            stringResource(Res.string.search_content_description_delete_recent),
        searchQueryText = searchQueryText,
        onBackClick = onBackClick,
        onSearchQueryTextChange = { searchQueryText = it },
        onSearchClick = onSearchClick,
        onRecentSearchKeywordClick = {},
        onRecentSearchDeleteClick = {},
        onPopularSearchKeywordClick = {},
        onPlaceClick = { place ->
            onPlaceClick(place.id)
        },
    )
}

private const val DUMMY_PLACE_IMAGE_URL: String =
    "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg"
