package com.bigong.oguri.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class PlaceDetailResponse(
    @field:Schema(description = "장소 ID", example = "5")
    val id: Long,
    @field:Schema(description = "국가명", example = "필리핀")
    val country: String,
    @field:Schema(description = "도시명", example = "보라카이")
    val city: String,
    @field:Schema(description = "상세 상단 이미지 URL 목록")
    val thumbnailUrls: List<String>,
    @field:Schema(description = "장소 저장 여부", example = "true")
    val isSaved: Boolean,
    @field:Schema(description = "환율 정보")
    val exchangeRateInfo: ExchangeRateResponse?,
    @field:Schema(description = "체감 물가 지수 (한국 대비 비율)", example = "1.68")
    val relativeCostIndex: Double?,
    @field:Schema(description = "추천 기간 평균 기온 (℃)", example = "16")
    val averageTemperature: Int?,
    @field:Schema(description = "추천 기간 평균 강수량 (mm)", example = "70.5")
    val averagePrecipitation: Double?,
    @field:Schema(description = "장소 상세 설명")
    val description: String,
    @field:Schema(description = "장소별 액티비티/즐길거리 목록")
    val experiences: List<ExperienceResponse>,
    @field:Schema(description = "항공권 검색 링크")
    val flightUrl: String,
    @field:Schema(description = "같은 기간 추천 가능한 다른 장소 목록")
    val relevantPlaces: List<PlaceResponse>,
)

data class ExperienceResponse(
    @field:Schema(description = "액티비티 제목", example = "스노클링 체험")
    val title: String,
    @field:Schema(description = "액티비티 설명", example = "맑은 바다에서 산호초와 열대어를 만나는 인기 체험이에요.")
    val summary: String,
    @field:Schema(description = "액티비티 썸네일 이미지 URL")
    val thumbnailUrl: String,
    @field:Schema(description = "제휴/상세 페이지 이동 링크")
    val advertisementUrl: String,
)
