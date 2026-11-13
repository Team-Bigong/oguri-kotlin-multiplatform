package com.bigong.oguri.service

import com.bigong.oguri.dto.response.TravelCitySuggestion
import com.bigong.oguri.dto.response.TravelDayPlacesResponse
import com.bigong.oguri.dto.response.TravelPlaceResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletionException

@Service
class TravelPlacesService(
    private val plans: TravelPlanService,
    private val google: GoogleTravelPlacesClient,
    @param:Value("\${GOOGLE_PLACES_REQUESTS_PER_MINUTE:300}") private val requestsPerMinute: Int,
) {
    private var currentMinute: Long = Long.MIN_VALUE
    private var totalRequests: Int = 0
    private var cityRequests: Int = 0
    private val memberRequests: MutableMap<String, Int> = mutableMapOf()

    fun search(
        memberId: String,
        tripId: UUID,
        query: String,
        language: String,
    ): List<TravelPlaceResponse> {
        val trimmed = query.trim()
        require(trimmed.length in 2..120)
        val city = plans.cityForPlaces(memberId, tripId)
        reserve(memberId, 1)
        return googleCall { google.search("$trimmed $city", supportedLanguage(language)) }
    }

    fun cities(
        clientKey: String,
        query: String,
        language: String,
    ): List<TravelCitySuggestion> {
        val trimmed = query.trim()
        require(trimmed.length in 2..120)
        reserve(clientKey, 1, cityLookup = true)
        return googleCall { google.cities(trimmed, supportedLanguage(language)) }
    }

    fun cityTimeZone(
        clientKey: String,
        placeId: String,
    ): String? {
        require(CITY_PLACE_ID.matches(placeId))
        reserve(clientKey, 1, cityLookup = true)
        return googleCall { google.cityTimeZone(placeId) }
    }

    private fun <Result> googleCall(block: () -> Result): Result =
        try {
            block()
        } catch (failure: CompletionException) {
            throw failure.cause as? ResponseStatusException ?: ResponseStatusException(HttpStatus.BAD_GATEWAY)
        }

    fun dayPlaces(
        memberId: String,
        tripId: UUID,
        dayNumber: Int,
        language: String,
    ): TravelDayPlacesResponse {
        val placeIds = plans.placeIdsForDay(memberId, tripId, dayNumber)
        if (placeIds.isEmpty()) return TravelDayPlacesResponse(emptyList(), false)
        reserve(memberId, placeIds.size)
        val locale = supportedLanguage(language)
        val results =
            placeIds
                .map { google.details(it, locale).handle { place, error -> place to (error != null) } }
                .map { it.join() }
        return TravelDayPlacesResponse(results.mapNotNull { it.first }, results.any { it.second })
    }

    @Synchronized
    private fun reserve(
        memberId: String,
        count: Int,
        cityLookup: Boolean = false,
    ) {
        val minute = Instant.now().epochSecond / SECONDS_PER_MINUTE
        if (minute != currentMinute) {
            currentMinute = minute
            totalRequests = 0
            cityRequests = 0
            memberRequests.clear()
        }
        val memberTotal = memberRequests[memberId] ?: 0
        val memberLimit = if (cityLookup) CITY_REQUESTS_PER_CLIENT else MEMBER_REQUESTS_PER_MINUTE
        if (totalRequests + count > requestsPerMinute ||
            memberTotal + count > memberLimit ||
            (cityLookup && cityRequests + count > CITY_REQUESTS_PER_MINUTE)
        ) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS)
        }
        totalRequests += count
        if (cityLookup) cityRequests += count
        memberRequests[memberId] = memberTotal + count
    }

    private fun supportedLanguage(language: String): String =
        when (language.substringBefore('-').lowercase()) {
            "ko" -> "ko"
            "ja" -> "ja"
            "zh" -> "zh-CN"
            else -> "en"
        }

    private companion object {
        val CITY_PLACE_ID: Regex = Regex("[A-Za-z0-9_-]{1,512}")
        const val CITY_REQUESTS_PER_CLIENT: Int = 20
        const val CITY_REQUESTS_PER_MINUTE: Int = 90
        const val SECONDS_PER_MINUTE: Long = 60
        const val MEMBER_REQUESTS_PER_MINUTE: Int = 90
    }
}
