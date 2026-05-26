package com.bigong.oguri.data.local

import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

private const val KEY_RECENT_SEARCH_KEYWORDS = "key_recent_search_keywords"

private var recentSearchKeywordLocalDataSourceInstance: RecentSearchKeywordLocalDataSource? = null

private class IosRecentSearchKeywordLocalDataSource : RecentSearchKeywordLocalDataSource {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val json = Json

    override fun initialize() = Unit

    override fun readRecentSearchKeywords(): List<String> {
        val encodedKeywords = userDefaults.stringForKey(KEY_RECENT_SEARCH_KEYWORDS) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<String>>(encodedKeywords)
        }.getOrDefault(emptyList())
    }

    override fun writeRecentSearchKeywords(keywords: List<String>) {
        userDefaults.setObject(json.encodeToString(keywords), forKey = KEY_RECENT_SEARCH_KEYWORDS)
    }
}

actual fun provideRecentSearchKeywordLocalDataSource(): RecentSearchKeywordLocalDataSource {
    val existingInstance = recentSearchKeywordLocalDataSourceInstance
    if (existingInstance != null) {
        return existingInstance
    }

    val newInstance = IosRecentSearchKeywordLocalDataSource()
    recentSearchKeywordLocalDataSourceInstance = newInstance
    return newInstance
}
