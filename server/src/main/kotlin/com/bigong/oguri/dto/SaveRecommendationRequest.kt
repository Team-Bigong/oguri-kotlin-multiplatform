package com.bigong.oguri.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class SaveRecommendationRequest(
    @field:Schema(description = "저장할 연휴 시작일", example = "2026-09-17")
    val startDate: LocalDate,
    @field:Schema(description = "저장할 연휴 종료일", example = "2026-09-27")
    val endDate: LocalDate,
)
