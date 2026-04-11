package com.bigong.oguri.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.DecimalFormat

@Service
class ExchangeService(
    private val authRestTemplate: RestTemplate,
    @param:Value("\${koreaexim.authkey}") private val rawAuthKey: String,
) {
    private val authKey = rawAuthKey.trim()
    private val logger = LoggerFactory.getLogger(ExchangeService::class.java)
    private val decimalFormat = DecimalFormat("#,###")

    /**
     * 특정 통화의 환율 정보를 문자열로 반환합니다.
     * 예: "1,400원 / 1USD (2026.04.11)"
     */
    fun getExchangeRateInfo(currencyCode: String?): String? {
        if (currencyCode.isNullOrBlank() || currencyCode == "KRW") return null
        
        // 데이터가 존재하는 가장 가까운 영업일 계산
        val targetDate = getLatestBusinessDay(LocalDate.now())
        val rates = fetchExchangeRates(targetDate)

        if (rates.isNullOrEmpty() || rates[0]["result"]?.toString() != "1") {
            // 여전히 데이터가 없는 경우 (공휴일 등) 한 번만 더 어제를 시도
            val retryDate = targetDate.minusDays(1)
            val retryRates = fetchExchangeRates(retryDate)
            if (retryRates.isNullOrEmpty() || retryRates[0]["result"]?.toString() != "1") return null
            return formatRateInfo(retryRates, currencyCode, retryDate)
        }

        return formatRateInfo(rates, currencyCode, targetDate)
    }

    private fun getLatestBusinessDay(date: LocalDate): LocalDate {
        return when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date.minusDays(1) // 토요일 -> 금요일
            DayOfWeek.SUNDAY -> date.minusDays(2)   // 일요일 -> 금요일
            else -> date
        }
    }

    private fun formatRateInfo(rates: List<Map<String, String>>, currencyCode: String, date: LocalDate): String? {
        val rate = rates.find { it["cur_unit"] == currencyCode || it["cur_unit"]?.startsWith(currencyCode) == true } ?: return null
        val ttsStr = rate["tts"]?.replace(",", "") ?: return null
        val tts = ttsStr.toDoubleOrNull() ?: return null
        
        val curUnit = rate["cur_unit"] ?: ""
        val unitLabel = if (curUnit.contains("(100)")) "100" else "1"
        val formattedRate = decimalFormat.format(tts)
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

        return "${formattedRate}원 / ${unitLabel}${currencyCode} ($dateString)"
    }

    private fun fetchExchangeRates(date: LocalDate): List<Map<String, String>>? {
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val url = "https://www.koreaexim.go.kr/site/program/financial/exchangeJSON?authkey=$authKey&searchdate=$dateStr&data=AP01"
        
        return try {
            val responseEntity = authRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<Map<String, String>>>() {}
            )
            responseEntity.body
        } catch (e: Exception) {
            logger.error("Failed to fetch exchange rates from KoreaExim for date: {}", dateStr, e)
            null
        }
    }
}
