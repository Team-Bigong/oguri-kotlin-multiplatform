package com.bigong.oguri.controller

import com.bigong.oguri.dto.AdminPublicHolidayResponse
import com.bigong.oguri.dto.AdminPublicHolidayUpsertRequest
import com.bigong.oguri.service.AdminPublicHolidayService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Hidden
@RestController
@RequestMapping("/api/admin/v1/public-holidays")
class AdminPublicHolidayController(
    private val adminPublicHolidayService: AdminPublicHolidayService,
) {
    @GetMapping
    fun getPublicHolidayList(): List<AdminPublicHolidayResponse> = adminPublicHolidayService.getPublicHolidayList()

    @PostMapping
    fun createPublicHoliday(
        @RequestBody request: AdminPublicHolidayUpsertRequest,
    ): AdminPublicHolidayResponse = adminPublicHolidayService.createPublicHoliday(request)

    @PutMapping("/{holidayId}")
    fun updatePublicHoliday(
        @PathVariable holidayId: Int,
        @RequestBody request: AdminPublicHolidayUpsertRequest,
    ): AdminPublicHolidayResponse = adminPublicHolidayService.updatePublicHoliday(holidayId, request)

    @DeleteMapping("/{holidayId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePublicHoliday(
        @PathVariable holidayId: Int,
    ) {
        adminPublicHolidayService.deletePublicHoliday(holidayId)
    }
}
