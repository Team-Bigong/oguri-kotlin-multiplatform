package com.bigong.oguri.dto.response

import java.time.LocalDate

/**
 * 홈 화면 및 추천 목록의 최상위 응답 객체
 */
data class HomeRecommendPeriodResponse(
    val rank: Int,
    val isSaved: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val holiday: List<String>,
    val dayOffCount: Int,
    val totalTripCount: Int,
    val places: List<PlaceResponse>,
    val advertisements: List<AdvertisementResponse>,
)

/**
 * 여행지 요약 정보 응답 객체
 */
data class PlaceResponse(
    val id: Long,
    val country: String,
    val city: String,
    val summary: String,
    val thumbnailUrl: String,
    val isSaved: Boolean, // 찜 여부 추가
)

/**
 * 광고 정보 응답 객체
 */
data class AdvertisementResponse(
    val platform: String,
    val url: String,
)
