package com.bigong.oguri.service

import com.bigong.oguri.dto.response.ExchangeRateResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.time.ZoneId

/**
 * 전 세계 160개 이상의 통화를 지원하는 ExchangeRate-API (Open)를 연동하는 서비스.
 * API Key 없이 무료로 안정적인 데이터를 제공하며, VND 등 동남아 통화도 완벽히 지원합니다.
 */
@Service
class ExchangeService(
    private val authRestTemplate: RestTemplate,
) {
    private val logger = LoggerFactory.getLogger(ExchangeService::class.java)

    /**
     * 특정 통화의 환율 정보를 객체 형태로 반환합니다.
     */
    fun getExchangeRateInfo(currencyCode: String?): ExchangeRateResponse? {
        if (currencyCode.isNullOrBlank() || currencyCode == "KRW") return null
        
        // CNH 등을 표준 코드인 CNY로 보정
        val standardCode = if (currencyCode == "CNH") "CNY" else currencyCode

        val result = fetchLatestRate(standardCode) ?: return null

        return try {
            val rates = result["rates"] as? Map<*, *>
            val rawKrwRate = (rates?.get("KRW") as? Number)?.toDouble() ?: return null
            
            // 통화별 적절한 단위(Unit) 결정 (예: JPY는 100, VND는 1000 등)
            val (unit, krwAmount) = calculateSmartUnit(standardCode, rawKrwRate)
            
            // API 응답의 타임스탬프를 LocalDate로 변환
            val timestamp = (result["time_last_update_unix"] as? Number)?.toLong() ?: Instant.now().epochSecond
            val updateDate = Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            ExchangeRateResponse(
                krwAmount = krwAmount,
                currencyUnit = unit,
                currencyCode = standardCode,
                date = updateDate
            )
        } catch (e: Exception) {
            logger.error("Failed to parse ExchangeRate-API response for {}", standardCode, e)
            null
        }
    }

    /**
     * 통화의 가치에 따라 사용자에게 친숙한 단위(1, 100, 1000)를 계산합니다.
     */
    private fun calculateSmartUnit(code: String, rate: Double): Pair<Int, Int> {
        return when {
            // 일본 옌, 인도네시아 루피아 등 관습적으로 100단위를 쓰는 경우
            code == "JPY" || code == "IDR" -> 100 to (rate * 100).toInt()
            // 베트남 동 같이 단위가 매우 큰 경우 1,000단위 표시
            code == "VND" -> 1000 to (rate * 1000).toInt()
            // 그 외 달러, 유로 등은 1단위 표시
            else -> 1 to rate.toInt()
        }
    }

    /**
     * ExchangeRate-API 호출 (무인증 오픈 엔드포인트)
     * GET https://open.er-api.com/v6/latest/USD
     */
    private fun fetchLatestRate(baseCurrency: String): Map<String, Any>? {
        val url = "https://open.er-api.com/v6/latest/$baseCurrency"
        
        return try {
            @Suppress("UNCHECKED_CAST")
            authRestTemplate.getForObject(url, Map::class.java) as? Map<String, Any>
        } catch (e: Exception) {
            logger.error("Failed to fetch rate from ExchangeRate-API for base: {}", baseCurrency, e)
            null
        }
    }
}
