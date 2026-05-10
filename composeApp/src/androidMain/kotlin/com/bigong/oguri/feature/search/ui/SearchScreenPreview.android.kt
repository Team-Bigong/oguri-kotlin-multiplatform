package com.bigong.oguri.feature.search.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.SearchAutocomplete

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SearchScreenPreview() {
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

    OguriTheme {
        SearchScreen(
            searchHintText = "가고 싶은 장소를 검색해보세요",
            recentSearchTitleText = "최근 검색어",
            emptyRecentSearchText = "아직 최근 검색한 여행지가 없어요.",
            popularSearchTitleText = "인기 검색어",
            recommendedPlaceTitleText = "이번 주의 추천 장소",
            recentSearchKeywords = recentSearchKeywords,
            popularSearchKeywords = popularSearchKeywords,
            recommendedPlaces = recommendedPlaces,
            searchResults = emptyList(),
            isSearchLoading = false,
            isSearchError = false,
            emptyResultMessageText = "해당 여행지를 찾지 못했어요",
            suggestDestinationText = "여행지 건의하기",
            backContentDescriptionText = "뒤로가기",
            searchContentDescriptionText = "검색",
            deleteRecentSearchContentDescriptionText = "최근 검색어 삭제",
            searchQueryText = "",
            onBackClick = {},
            onSearchQueryTextChange = {},
            onSearchClick = {},
            onSearchRetryClick = {},
            onSuggestionClick = {},
            onSearchResultClick = {},
            onRecentSearchKeywordClick = {},
            onRecentSearchDeleteClick = {},
            onPopularSearchKeywordClick = {},
            onPlaceClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SearchScreenResultPreview() {
    OguriTheme {
        SearchScreen(
            searchHintText = "가고 싶은 장소를 검색해보세요",
            recentSearchTitleText = "최근 검색어",
            emptyRecentSearchText = "아직 최근 검색한 여행지가 없어요.",
            popularSearchTitleText = "인기 검색어",
            recommendedPlaceTitleText = "이번 주의 추천 장소",
            recentSearchKeywords = emptyList(),
            popularSearchKeywords = emptyList(),
            recommendedPlaces = emptyList(),
            searchResults =
                listOf(
                    SearchAutocomplete(
                        id = 1L,
                        destinationName = "오사카",
                        countryName = "일본",
                    ),
                    SearchAutocomplete(
                        id = 2L,
                        destinationName = "오키나와",
                        countryName = "일본",
                    ),
                ),
            isSearchLoading = false,
            isSearchError = false,
            emptyResultMessageText = "해당 여행지를 찾지 못했어요",
            suggestDestinationText = "여행지 건의하기",
            backContentDescriptionText = "뒤로가기",
            searchContentDescriptionText = "검색",
            deleteRecentSearchContentDescriptionText = "최근 검색어 삭제",
            searchQueryText = "오",
            onBackClick = {},
            onSearchQueryTextChange = {},
            onSearchClick = {},
            onSearchRetryClick = {},
            onSuggestionClick = {},
            onSearchResultClick = {},
            onRecentSearchKeywordClick = {},
            onRecentSearchDeleteClick = {},
            onPopularSearchKeywordClick = {},
            onPlaceClick = {},
        )
    }
}

private const val DUMMY_PLACE_IMAGE_URL: String =
    "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg"
