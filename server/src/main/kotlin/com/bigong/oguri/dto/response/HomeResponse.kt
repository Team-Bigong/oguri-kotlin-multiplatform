package com.bigong.oguri.dto.response

import java.time.LocalDate

/**
 * 홈 화면 이번 주 인기 여행지 응답 객체
 */
data class HomeWeeklyTopResponse(
    val weeklyTopPlaces: List<HomeWeeklyTopPlaceResponse>,
)

/**
 * 홈 화면 이번 주 인기 여행지 개별 장소 응답 객체 (경량화 버전)
 */
data class HomeWeeklyTopPlaceResponse(
    val id: Long,
    val country: String,
    val city: String,
    val thumbnailUrl: String,
)

/**
 * 홈 화면 이번 달 인기 추천 기간 응답 객체 (경량화 버전)
 */
data class HomeMonthlyTopPeriodResponse(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalTripCount: Int,
    val holidayCount: Int,
    val dayOffCount: Int,
)

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
