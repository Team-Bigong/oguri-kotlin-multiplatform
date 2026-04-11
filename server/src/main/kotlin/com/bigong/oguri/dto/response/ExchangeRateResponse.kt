package com.bigong.oguri.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class ExchangeRateResponse(
    @field:Schema(description = "원화 금액 (TTS 기준)", example = "940")
    val krwAmount: Int,
    @field:Schema(description = "외화 단위 (1 또는 100)", example = "100")
    val currencyUnit: Int,
    @field:Schema(description = "통화 코드", example = "JPY")
    val currencyCode: String,
    @field:Schema(description = "환율 기준 날짜", example = "2026.04.10")
    val date: String,
)
