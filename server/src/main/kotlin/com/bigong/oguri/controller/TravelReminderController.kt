package com.bigong.oguri.controller

import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.domain.TravelPlanConflict
import com.bigong.oguri.dto.response.TravelPlanErrorResponse
import com.bigong.oguri.service.RegisterTravelReminderDevice
import com.bigong.oguri.service.ScheduleTravelReminder
import com.bigong.oguri.service.TravelReminder
import com.bigong.oguri.service.TravelReminderService
import com.bigong.oguri.service.TravelReminderSettings
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class TravelReminderController(private val service: TravelReminderService) {
    @GetMapping("/trips/{tripId}/reminder")
    fun settings(request: HttpServletRequest, @PathVariable tripId: UUID): ResponseEntity<TravelReminderSettings> =
        noStore(service.settings(request.resolveMemberId(), tripId))

    @PutMapping("/trips/{tripId}/reminder")
    fun schedule(
        request: HttpServletRequest,
        @PathVariable tripId: UUID,
        @RequestBody body: ScheduleTravelReminder,
    ): ResponseEntity<TravelReminder> = noStore(service.schedule(request.resolveMemberId(), tripId, body))

    @DeleteMapping("/trips/{tripId}/reminder/{reminderId}")
    fun cancel(request: HttpServletRequest, @PathVariable tripId: UUID, @PathVariable reminderId: UUID): ResponseEntity<Void> {
        service.cancel(request.resolveMemberId(), tripId, reminderId)
        return noContent()
    }

    @PutMapping("/travel-reminder-devices/{deviceId}")
    fun register(
        request: HttpServletRequest,
        @PathVariable deviceId: UUID,
        @RequestBody body: RegisterTravelReminderDevice,
    ): ResponseEntity<Void> {
        service.registerDevice(request.resolveMemberId(), deviceId, body)
        return noContent()
    }

    @PostMapping("/travel-reminder-devices/{deviceId}/revoke")
    fun revoke(@PathVariable deviceId: UUID, @RequestBody body: RevokeTravelReminderDevice): ResponseEntity<Void> {
        service.revokeDevice(deviceId, body.secret)
        return noContent()
    }

    @GetMapping("/travel-reminders/{reminderId}")
    fun open(request: HttpServletRequest, @PathVariable reminderId: UUID): ResponseEntity<OpenTravelReminder> =
        noStore(OpenTravelReminder(service.open(request.resolveMemberId(), reminderId)))

    @ExceptionHandler(TravelPlanConflict::class)
    fun conflict(error: TravelPlanConflict): ResponseEntity<TravelPlanErrorResponse> =
        ResponseEntity.status(409).cacheControl(CacheControl.noStore()).body(TravelPlanErrorResponse(error.code))

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidRequest(): ResponseEntity<TravelPlanErrorResponse> =
        ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(TravelPlanErrorResponse("INVALID_TRAVEL_REMINDER"))

    private fun <Body> noStore(body: Body): ResponseEntity<Body> = ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body)

    private fun noContent(): ResponseEntity<Void> = ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build()
}

@JvmRecord
data class RevokeTravelReminderDevice(val secret: String)

@JvmRecord
data class OpenTravelReminder(val planId: UUID)
