package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.response.SearchAutocompleteResponse

interface SearchRemoteDataSource {
    suspend fun getSearchAutocompleteResponses(query: String): List<SearchAutocompleteResponse>
}
