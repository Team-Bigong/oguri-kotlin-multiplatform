package com.bigong.oguri.controller

import com.bigong.oguri.dto.AdminCountryResponse
import com.bigong.oguri.dto.AdminDestinationResponse
import com.bigong.oguri.dto.AdminDestinationUpsertRequest
import com.bigong.oguri.service.AdminDestinationService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Hidden
@RestController
@RequestMapping("/api/admin/v1")
class AdminDestinationController(
    private val adminDestinationService: AdminDestinationService,
) {
    @GetMapping("/countries")
    fun getCountryList(): List<AdminCountryResponse> = adminDestinationService.getCountryList()

    @GetMapping("/destinations")
    fun getDestinationList(): List<AdminDestinationResponse> = adminDestinationService.getDestinationList()

    @PostMapping("/destinations")
    fun createDestination(
        @RequestBody request: AdminDestinationUpsertRequest,
    ): AdminDestinationResponse = adminDestinationService.createDestination(request)

    @PutMapping("/destinations/{destinationId}")
    fun updateDestination(
        @PathVariable destinationId: Int,
        @RequestBody request: AdminDestinationUpsertRequest,
    ): AdminDestinationResponse = adminDestinationService.updateDestination(destinationId, request)

    @DeleteMapping("/destinations/{destinationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDestination(
        @PathVariable destinationId: Int,
    ) {
        adminDestinationService.deleteDestination(destinationId)
    }
}
