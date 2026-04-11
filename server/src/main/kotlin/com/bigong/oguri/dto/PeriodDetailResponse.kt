package com.bigong.oguri.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class PeriodDetailResponse(
    @field:Schema(description = "일정 시작일", example = "2026-02-28")
    val startDate: LocalDate,
    @field:Schema(description = "일정 종료일", example = "2026-03-04")
    val endDate: LocalDate,
    @field:Schema(description = "포함된 공휴일 목록", example = "[\"삼일절\", \"대체휴일\"]")
    val holiday: List<String>,
    @field:Schema(description = "사용 연차 일수", example = "2")
    val dayOffCount: Int,
    @field:Schema(description = "총 여행 일수", example = "5")
    val totalTripCount: Int,
    @field:Schema(description = "현재 페이지 번호(0부터 시작)", example = "0")
    val page: Int,
    @field:Schema(description = "페이지 크기", example = "10")
    val size: Int,
    @field:Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean,
    val places: List<PlaceResponse>,
)
