package com.bigong.oguri.service

import com.bigong.oguri.domain.TravelCatalogCity
import com.bigong.oguri.dto.response.TravelCitySuggestion
import com.bigong.oguri.repository.TravelCatalogCityRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.ZoneId

@Service
class TravelCityCatalog(private val cities: TravelCatalogCityRepository) {
    private var currentMinute: Long = Long.MIN_VALUE
    private val memberRequests: MutableMap<String, Int> = mutableMapOf()

    fun search(memberId: String, query: String): List<TravelCitySuggestion> {
        val trimmed = query.trim()
        require(trimmed.length in 2..120)
        reserve(memberId)
        val prefix = trimmed.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%"
        return cities.search(trimmed, prefix).map { it.toSuggestion() }
    }

    fun timeZone(memberId: String, cityId: String): String? {
        val id = cityId.toLongOrNull() ?: return null
        reserve(memberId)
        return cities.findById(id).orElse(null)?.timeZone?.takeIf { it in ZoneId.getAvailableZoneIds() }
    }

    @Synchronized
    private fun reserve(memberId: String) {
        val minute = Instant.now().epochSecond / SECONDS_PER_MINUTE
        if (minute != currentMinute) {
            currentMinute = minute
            memberRequests.clear()
        }
        val requests = memberRequests[memberId] ?: 0
        if (requests >= REQUESTS_PER_MEMBER_PER_MINUTE) throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS)
        memberRequests[memberId] = requests + 1
    }

    private companion object {
        const val SECONDS_PER_MINUTE: Long = 60
        const val REQUESTS_PER_MEMBER_PER_MINUTE: Int = 20
    }
}

internal fun TravelCatalogCity.toSuggestion(label: String = qualifiedLabel()): TravelCitySuggestion =
    TravelCitySuggestion(id.toString(), label, name, regionLabel, timeZone, latitude, longitude)
