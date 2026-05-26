package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.BASE_URL
import com.bigong.oguri.data.remote.model.response.SearchAutocompleteResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

@Inject
class KtorSearchRemoteDataSource(
    private val httpClient: HttpClient,
    private val authRequestExecutor: AuthRequestExecutor,
) : SearchRemoteDataSource {
    override suspend fun getSearchAutocompleteResponses(query: String): List<SearchAutocompleteResponse> {
        val requestUrl = "$BASE_URL$HOME_SEARCH_AUTOCOMPLETE_API_PATH"
        return authRequestExecutor.execute {
            httpClient
                .get(requestUrl) {
                    parameter(SEARCH_QUERY_NAME, query)
                }.body()
        }
    }

    private companion object {
        private const val HOME_SEARCH_AUTOCOMPLETE_API_PATH = "/api/v1/home/search/autocomplete"
        private const val SEARCH_QUERY_NAME = "query"
    }
}
