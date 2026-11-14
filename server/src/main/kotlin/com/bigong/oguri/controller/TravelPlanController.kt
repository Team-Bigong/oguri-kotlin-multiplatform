package com.bigong.oguri.controller

import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.domain.TravelPlanConflict
import com.bigong.oguri.dto.request.CreateTravelPlanRequest
import com.bigong.oguri.dto.request.RecordTravelPlanViewRequest
import com.bigong.oguri.dto.request.TravelPlanCommand
import com.bigong.oguri.dto.response.TravelDayPlacesResponse
import com.bigong.oguri.dto.response.TravelPlaceResponse
import com.bigong.oguri.dto.response.TravelPlanErrorResponse
import com.bigong.oguri.dto.response.TravelPlanResponse
import com.bigong.oguri.dto.response.TravelPlanResumeResponse
import com.bigong.oguri.dto.response.TravelPlanSummaryResponse
import com.bigong.oguri.service.TravelPlacesService
import com.bigong.oguri.service.TravelPlanService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/trips")
class TravelPlanController(
    private val travelPlanService: TravelPlanService,
    private val travelPlacesService: TravelPlacesService,
) {
    @PostMapping
    fun create(
        request: HttpServletRequest,
        @RequestBody body: CreateTravelPlanRequest,
    ): ResponseEntity<TravelPlanSummaryResponse> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(travelPlanService.create(request.resolveMemberId(), body))

    @GetMapping
    fun list(request: HttpServletRequest): ResponseEntity<List<TravelPlanSummaryResponse>> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(travelPlanService.list(request.resolveMemberId()))

    @GetMapping("/resume")
    fun resume(request: HttpServletRequest): ResponseEntity<TravelPlanResumeResponse> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
            TravelPlanResumeResponse(travelPlanService.resume(request.resolveMemberId())),
        )

    @PutMapping("/{tripId}/view")
    fun recordView(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @RequestBody body: RecordTravelPlanViewRequest,
    ): ResponseEntity<Void> {
        travelPlanService.recordView(request.resolveMemberId(), tripId, requireNotNull(body.dayNumber))
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build()
    }

    @GetMapping("/{tripId}")
    fun get(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
    ): ResponseEntity<TravelPlanResponse> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(travelPlanService.get(request.resolveMemberId(), tripId))

    @PostMapping("/{tripId}/commands")
    fun execute(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @RequestBody body: TravelPlanCommand,
    ): ResponseEntity<TravelPlanResponse> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(travelPlanService.execute(request.resolveMemberId(), tripId, body))

    @PostMapping("/{tripId}/places/search")
    fun searchPlaces(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @RequestBody body: TravelPlaceSearchRequest,
    ): ResponseEntity<List<TravelPlaceResponse>> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
            travelPlacesService.search(request.resolveMemberId(), tripId, body.query, body.language),
        )

    @GetMapping("/{tripId}/days/{dayNumber}/places")
    fun dayPlaces(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @PathVariable dayNumber: Int,
        @RequestParam(defaultValue = "en") language: String,
    ): ResponseEntity<TravelDayPlacesResponse> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
            travelPlacesService.dayPlaces(request.resolveMemberId(), tripId, dayNumber, language),
        )

    @GetMapping("/{tripId}/places")
    fun planPlaces(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @RequestParam(defaultValue = "en") language: String,
    ): ResponseEntity<TravelDayPlacesResponse> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
            travelPlacesService.planPlaces(request.resolveMemberId(), tripId, language),
        )

    @PostMapping("/{tripId}/places/details")
    fun planPlaceBatch(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @RequestBody body: TravelPlaceBatchRequest,
    ): ResponseEntity<TravelDayPlacesResponse> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
            travelPlacesService.planPlaceBatch(request.resolveMemberId(), tripId, body.placeIds, body.language),
        )

    @ExceptionHandler(IllegalArgumentException::class, HttpMessageNotReadableException::class)
    fun invalidRequest(): ResponseEntity<TravelPlanErrorResponse> =
        ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(TravelPlanErrorResponse("INVALID_TRAVEL_PLAN_REQUEST"))

    @ExceptionHandler(TravelPlanConflict::class)
    fun conflict(exception: TravelPlanConflict): ResponseEntity<TravelPlanErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CacheControl.noStore()).body(TravelPlanErrorResponse(exception.code))

    @ExceptionHandler(ResponseStatusException::class)
    fun accessError(exception: ResponseStatusException): ResponseEntity<TravelPlanErrorResponse> {
        val code =
            when (exception.statusCode) {
                HttpStatus.UNAUTHORIZED -> "TRAVEL_AUTH_REQUIRED"
                HttpStatus.NOT_FOUND -> "TRAVEL_PLAN_NOT_FOUND"
                HttpStatus.TOO_MANY_REQUESTS -> "TRAVEL_PLACES_LIMIT"
                else -> "TRAVEL_PLACES_UNAVAILABLE"
            }
        return ResponseEntity.status(exception.statusCode).cacheControl(CacheControl.noStore()).body(TravelPlanErrorResponse(code))
    }
}

@JvmRecord
data class TravelPlaceSearchRequest(
    val query: String,
    val language: String,
)

@JvmRecord
data class TravelPlaceBatchRequest(
    val placeIds: List<String>,
    val language: String,
)
