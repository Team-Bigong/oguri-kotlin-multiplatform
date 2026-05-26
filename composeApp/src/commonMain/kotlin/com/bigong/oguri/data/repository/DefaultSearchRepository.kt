package com.bigong.oguri.data.repository

import com.bigong.oguri.data.local.RecentSearchKeywordLocalDataSource
import com.bigong.oguri.data.remote.SearchRemoteDataSource
import com.bigong.oguri.data.remote.model.response.SearchAutocompleteResponse
import com.bigong.oguri.domain.model.RecentSearchKeywords
import com.bigong.oguri.domain.model.SearchAutocomplete
import com.bigong.oguri.domain.repository.SearchRepository
import dev.zacsweers.metro.Inject

@Inject
class DefaultSearchRepository(
    private val recentSearchKeywordLocalDataSource: RecentSearchKeywordLocalDataSource,
    private val searchRemoteDataSource: SearchRemoteDataSource,
) : SearchRepository {
    override suspend fun getSearchAutocompletes(query: String): List<SearchAutocomplete> =
        searchRemoteDataSource
            .getSearchAutocompleteResponses(query = query)
            .map { response -> response.toDomain() }

    override fun getRecentSearchKeywords(): List<String> {
        recentSearchKeywordLocalDataSource.initialize()
        return RecentSearchKeywords
            .from(recentSearchKeywordLocalDataSource.readRecentSearchKeywords())
            .toList()
    }

    override fun addRecentSearchKeyword(keyword: String): List<String> {
        val updatedKeywords =
            RecentSearchKeywords
                .from(getRecentSearchKeywords())
                .add(keyword = keyword)
                .toList()
        recentSearchKeywordLocalDataSource.writeRecentSearchKeywords(keywords = updatedKeywords)
        return updatedKeywords
    }

    override fun deleteRecentSearchKeyword(keyword: String): List<String> {
        val updatedKeywords =
            RecentSearchKeywords
                .from(getRecentSearchKeywords())
                .delete(keyword = keyword)
                .toList()
        recentSearchKeywordLocalDataSource.writeRecentSearchKeywords(keywords = updatedKeywords)
        return updatedKeywords
    }
}

private fun SearchAutocompleteResponse.toDomain(): SearchAutocomplete =
    SearchAutocomplete(
        id = id,
        destinationName = destinationName,
        countryName = countryName,
    )
