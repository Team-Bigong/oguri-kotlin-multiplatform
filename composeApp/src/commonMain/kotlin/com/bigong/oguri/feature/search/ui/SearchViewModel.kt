package com.bigong.oguri.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.usecase.AddRecentSearchKeywordUseCase
import com.bigong.oguri.domain.usecase.DeleteRecentSearchKeywordUseCase
import com.bigong.oguri.domain.usecase.GetRecentSearchKeywordListUseCase
import com.bigong.oguri.domain.usecase.GetSearchAutocompleteListUseCase
import com.bigong.oguri.domain.usecase.GetWeeklyTopPlaceListUseCase
import com.bigong.oguri.feature.search.ui.model.SearchUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SEARCH_AUTOCOMPLETE_DEBOUNCE_MILLIS = 350L

@Inject
class SearchViewModel(
    private val getRecentSearchKeywordsUseCase: GetRecentSearchKeywordListUseCase,
    private val getWeeklyTopPlacesUseCase: GetWeeklyTopPlaceListUseCase,
    private val getSearchAutocompletesUseCase: GetSearchAutocompleteListUseCase,
    private val addRecentSearchKeywordUseCase: AddRecentSearchKeywordUseCase,
    private val deleteRecentSearchKeywordUseCase: DeleteRecentSearchKeywordUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRecentSearchKeywords()
        loadRecommendedPlaces()
        observeSearchQueryText()
    }

    fun changeSearchQueryText(searchQueryText: String) {
        val trimmedSearchQueryText = searchQueryText.trim()
        _uiState.update { currentUiState ->
            currentUiState.copy(
                searchQueryText = searchQueryText,
                searchResults = emptyList(),
                isSearchLoading = trimmedSearchQueryText.isNotEmpty(),
                isSearchError = false,
            )
        }
    }

    fun clearSearchQueryText() {
        changeSearchQueryText(searchQueryText = "")
    }

    fun addRecentSearchKeyword(
        keyword: String,
        onCompleted: () -> Unit = {},
    ) {
        viewModelScope.launch {
            val recentSearchKeywords =
                withContext(Dispatchers.Default) {
                    addRecentSearchKeywordUseCase(keyword = keyword)
                }
            _uiState.update { currentUiState ->
                currentUiState.copy(recentSearchKeywords = recentSearchKeywords)
            }
            onCompleted()
        }
    }

    fun deleteRecentSearchKeyword(keyword: String) {
        viewModelScope.launch {
            val recentSearchKeywords =
                withContext(Dispatchers.Default) {
                    deleteRecentSearchKeywordUseCase(keyword = keyword)
                }
            _uiState.update { currentUiState ->
                currentUiState.copy(recentSearchKeywords = recentSearchKeywords)
            }
        }
    }

    private fun loadRecentSearchKeywords() {
        viewModelScope.launch {
            val recentSearchKeywords =
                withContext(Dispatchers.Default) {
                    getRecentSearchKeywordsUseCase()
                }
            _uiState.update { currentUiState ->
                currentUiState.copy(recentSearchKeywords = recentSearchKeywords)
            }
        }
    }

    private fun loadRecommendedPlaces() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    getWeeklyTopPlacesUseCase()
                }
            }.onSuccess { recommendedPlaces ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(recommendedPlaces = recommendedPlaces)
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQueryText() {
        viewModelScope.launch {
            uiState
                .map { searchUiState -> searchUiState.searchQueryText.trim() }
                .distinctUntilChanged()
                .debounce(SEARCH_AUTOCOMPLETE_DEBOUNCE_MILLIS)
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update { currentUiState ->
                            currentUiState.copy(
                                searchResults = emptyList(),
                                isSearchLoading = false,
                                isSearchError = false,
                            )
                        }
                        return@collectLatest
                    }

                    search(query = query)
                }
        }
    }

    fun retrySearch() {
        val query = uiState.value.searchQueryText.trim()
        if (query.isBlank()) {
            return
        }
        viewModelScope.launch {
            search(query = query)
        }
    }

    private suspend fun search(query: String) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                isSearchLoading = true,
                isSearchError = false,
            )
        }

        runCatching {
            withContext(Dispatchers.Default) {
                getSearchAutocompletesUseCase(query = query)
            }
        }.onSuccess { searchResults ->
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    searchResults = searchResults,
                    isSearchLoading = false,
                    isSearchError = false,
                )
            }
        }.onFailure {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    searchResults = emptyList(),
                    isSearchLoading = false,
                    isSearchError = true,
                )
            }
        }
    }
}
