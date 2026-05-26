package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.SearchAutocomplete

interface SearchRepository {
    suspend fun getSearchAutocompletes(query: String): List<SearchAutocomplete>

    fun getRecentSearchKeywords(): List<String>

    fun addRecentSearchKeyword(keyword: String): List<String>

    fun deleteRecentSearchKeyword(keyword: String): List<String>
}
