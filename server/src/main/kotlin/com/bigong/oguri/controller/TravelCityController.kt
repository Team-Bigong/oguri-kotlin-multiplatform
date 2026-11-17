package com.bigong.oguri.controller

import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.dto.response.TravelCitySuggestion
import com.bigong.oguri.dto.response.TravelCityTimeZoneResponse
import com.bigong.oguri.service.TravelCityCatalog
import com.bigong.oguri.service.TravelPlanService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/travel-places/cities")
class TravelCityController(
    private val cities: TravelCityCatalog,
    private val plans: TravelPlanService,
) {
    @PostMapping("/search")
    fun search(
        request: HttpServletRequest,
        @RequestBody body: TravelCitySearchRequest,
    ): ResponseEntity<List<TravelCitySuggestion>> {
        val memberId = request.resolveMemberId()
        plans.requireMember(memberId)
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(cities.search(memberId, body.query))
    }

    @PostMapping("/time-zone")
    fun timeZone(
        request: HttpServletRequest,
        @RequestBody body: TravelCityTimeZoneRequest,
    ): ResponseEntity<TravelCityTimeZoneResponse> {
        val memberId = request.resolveMemberId()
        plans.requireMember(memberId)
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(TravelCityTimeZoneResponse(cities.timeZone(memberId, body.placeId)))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidRequest(): ResponseEntity<Void> = ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noStore()).build()
}

@JvmRecord
data class TravelCitySearchRequest(
    val query: String,
    val language: String,
)

@JvmRecord
data class TravelCityTimeZoneRequest(
    val placeId: String,
)
