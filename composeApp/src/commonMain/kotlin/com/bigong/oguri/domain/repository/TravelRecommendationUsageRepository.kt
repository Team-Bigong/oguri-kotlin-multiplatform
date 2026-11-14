package com.bigong.oguri.domain.repository

enum class TravelRecommendationSource { CITY, PERIOD }

enum class TravelRecommendationEntry { EXPOSED, CLICKED, LOGIN_RESUMED }

interface TravelRecommendationUsageRepository {
    suspend fun record(
        source: TravelRecommendationSource,
        entry: TravelRecommendationEntry,
    )
}
