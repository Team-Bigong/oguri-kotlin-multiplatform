package com.bigong.oguri.controller

import com.bigong.oguri.dto.request.AdminCountryUpsertRequest
import com.bigong.oguri.dto.response.AdminCountryResponse
import com.bigong.oguri.dto.response.AdminDestinationResponse
import com.bigong.oguri.dto.request.AdminDestinationUpsertRequest
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

    @PostMapping("/countries")
    fun createCountry(
        @RequestBody request: AdminCountryUpsertRequest,
    ): AdminCountryResponse = adminDestinationService.createCountry(request)

    @PutMapping("/countries/{countryId}")
    fun updateCountry(
        @PathVariable countryId: Int,
        @RequestBody request: AdminCountryUpsertRequest,
    ): AdminCountryResponse = adminDestinationService.updateCountry(countryId, request)

    @DeleteMapping("/countries/{countryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCountry(
        @PathVariable countryId: Int,
    ) {
        adminDestinationService.deleteCountry(countryId)
    }

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
