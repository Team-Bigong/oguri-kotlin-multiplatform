package com.bigong.oguri.data.local

interface RecentSearchKeywordLocalDataSource {
    fun initialize()

    fun readRecentSearchKeywords(): List<String>

    fun writeRecentSearchKeywords(keywords: List<String>)
}
