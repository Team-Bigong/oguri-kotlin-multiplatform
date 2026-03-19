package com.bigong.oguri.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class CalendarResponse(
    @field:Schema(description = "요청에 사용된 연차 일수", example = "3")
    val dayOffCount: Int,
    @field:Schema(description = "현재 페이지 인덱스(0부터 시작)", example = "0")
    val page: Int,
    @field:Schema(description = "페이지 크기", example = "10")
    val size: Int,
    @field:Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean,
    @field:Schema(description = "추천 일정 목록(가장 추천할만한 일정 순)")
    val periods: List<CalendarPeriodRecommendationResponse>
)

data class CalendarPeriodRecommendationResponse(
    @field:Schema(description = "일정 시작일", example = "2026-02-28")
    val startDate: LocalDate,
    @field:Schema(description = "일정 종료일", example = "2026-03-04")
    val endDate: LocalDate,
    @field:Schema(description = "기간 내 총 여행 일수(달력 기준)", example = "5")
    val totalTripCount: Int,
    @field:Schema(description = "기간 내 휴일(주말+공휴일) 일수", example = "3")
    val holidayCount: Int,
    @field:Schema(description = "기간 내 실제 연차 사용 일수", example = "2")
    val dayOffCount: Int,
    @field:Schema(description = "기간 내 포함된 공휴일 이름 목록", example = "[\"삼일절\", \"대체휴일\"]")
    val holidays: List<String>,
    @field:Schema(description = "저장된 일정 여부", example = "false")
    val isSaved: Boolean
)
