package com.bigong.oguri.service

import com.bigong.oguri.dto.response.ExchangeRateResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 한국수출입은행 Open API를 연동하여 실시간 환율 정보를 제공하는 서비스
 */
@Service
class ExchangeService(
    private val authRestTemplate: RestTemplate,
    @param:Value("\${koreaexim.authkey}") private val rawAuthKey: String,
) {
    private val authKey = rawAuthKey.trim()
    private val logger = LoggerFactory.getLogger(ExchangeService::class.java)

    /**
     * 특정 통화의 환율 정보를 객체 형태로 반환합니다.
     * @param currencyCode 통화 코드 (예: USD, JPY, EUR)
     * @return ExchangeRateResponse (데이터가 없거나 KRW인 경우 null)
     */
    fun getExchangeRateInfo(currencyCode: String?): ExchangeRateResponse? {
        // 한국 여행지(KRW)는 환율 정보가 필요 없으므로 제외
        if (currencyCode.isNullOrBlank() || currencyCode == "KRW") return null
        
        // 수출입은행 API는 주말/공휴일에 데이터를 제공하지 않음
        // 1차 시도: 현재 기준 가장 가까운 영업일의 데이터를 조회
        val targetDate = getLatestBusinessDay(LocalDate.now())
        val rates = fetchExchangeRates(targetDate)

        // 2차 시도: API 응답이 성공(result=1)이 아니거나 데이터가 없으면 전날 데이터 시도 (공휴일 대비)
        if (rates.isNullOrEmpty() || rates[0]["result"]?.toString() != "1") {
            val retryDate = targetDate.minusDays(1)
            val retryRates = fetchExchangeRates(retryDate)
            if (retryRates.isNullOrEmpty() || retryRates[0]["result"]?.toString() != "1") return null
            return convertToResponse(retryRates, currencyCode, retryDate)
        }

        return convertToResponse(rates, currencyCode, targetDate)
    }

    /**
     * 오늘이 주말인 경우 직전 금요일 날짜를 반환합니다.
     */
    private fun getLatestBusinessDay(date: LocalDate): LocalDate {
        return when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date.minusDays(1) // 토요일 -> 금요일
            DayOfWeek.SUNDAY -> date.minusDays(2)   // 일요일 -> 금요일
            else -> date
        }
    }

    /**
     * API 응답 Map 리스트를 DTO 객체로 변환합니다.
     */
    private fun convertToResponse(rates: List<Map<String, String>>, currencyCode: String, date: LocalDate): ExchangeRateResponse? {
        // 수출입은행 API 응답에서 해당 통화 코드를 찾음 (일부 통화는 JPY(100) 처럼 들어오므로 startsWith로 체크)
        val rate = rates.find { it["cur_unit"] == currencyCode || it["cur_unit"]?.startsWith(currencyCode) == true } ?: return null
        
        // tts: 전신환매도율 (송금 보낼 때 환율)
        val ttsStr = rate["tts"]?.replace(",", "") ?: return null
        val tts = ttsStr.toDoubleOrNull()?.toInt() ?: return null
        
        // 일본(JPY), 인도네시아(IDR) 등 100단위 통화 여부 확인
        val curUnit = rate["cur_unit"] ?: ""
        val unit = if (curUnit.contains("(100)")) 100 else 1
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

        return ExchangeRateResponse(
            krwAmount = tts,
            currencyUnit = unit,
            currencyCode = currencyCode,
            date = dateString
        )
    }

    /**
     * 수출입은행 API 서버와 실제 통신을 수행합니다.
     */
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
            // Connection Reset 등의 이슈 발생 시 에러 로그 기록
            logger.error("Failed to fetch exchange rates from KoreaExim for date: {}", dateStr, e)
            null
        }
    }
}
