package com.bigong.oguri.domain.model

class RecentSearchKeywords private constructor(
    private val values: List<String>,
) {
    fun add(keyword: String): RecentSearchKeywords {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isEmpty()) {
            return this
        }

        val reorderedValues =
            listOf(normalizedKeyword) +
                values.filterNot { value ->
                    value == normalizedKeyword
                }
        return RecentSearchKeywords(values = reorderedValues.take(MAX_RECENT_SEARCH_KEYWORD_COUNT))
    }

    fun delete(keyword: String): RecentSearchKeywords =
        RecentSearchKeywords(
            values =
                values.filterNot { value ->
                    value == keyword
                },
        )

    fun toList(): List<String> = values

    companion object {
        private const val MAX_RECENT_SEARCH_KEYWORD_COUNT = 7

        fun from(values: List<String>): RecentSearchKeywords =
            RecentSearchKeywords(
                values =
                    values
                        .map { value -> value.trim() }
                        .filter { value -> value.isNotEmpty() }
                        .distinct()
                        .take(MAX_RECENT_SEARCH_KEYWORD_COUNT),
            )
    }
}
