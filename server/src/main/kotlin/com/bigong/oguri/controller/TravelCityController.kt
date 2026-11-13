package com.bigong.oguri.controller

import com.bigong.oguri.config.AuthContext
import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.dto.response.TravelCitySuggestion
import com.bigong.oguri.dto.response.TravelCityTimeZoneResponse
import com.bigong.oguri.service.TravelPlacesService
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
    private val places: TravelPlacesService,
) {
    @PostMapping("/search")
    fun search(
        request: HttpServletRequest,
        @RequestBody body: TravelCitySearchRequest,
    ): ResponseEntity<List<TravelCitySuggestion>> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
            places.cities(request.cityLookupKey(), body.query, body.language),
        )

    @PostMapping("/time-zone")
    fun timeZone(
        request: HttpServletRequest,
        @RequestBody body: TravelCityTimeZoneRequest,
    ): ResponseEntity<TravelCityTimeZoneResponse> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
            TravelCityTimeZoneResponse(places.cityTimeZone(request.cityLookupKey(), body.placeId)),
        )

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidRequest(): ResponseEntity<Void> = ResponseEntity.status(HttpStatus.BAD_REQUEST).cacheControl(CacheControl.noStore()).build()

    private fun HttpServletRequest.cityLookupKey(): String {
        val memberId = resolveMemberId()
        return if (memberId == AuthContext.GUEST_MEMBER_ID) "city:guest:$remoteAddr" else "city:member:$memberId"
    }
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
