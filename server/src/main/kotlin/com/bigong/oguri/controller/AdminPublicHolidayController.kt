package com.bigong.oguri.controller

import com.bigong.oguri.dto.AdminPublicHolidayResponse
import com.bigong.oguri.dto.AdminPublicHolidayUpsertRequest
import com.bigong.oguri.service.AdminPublicHolidayService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.*

@Hidden
@RestController
@RequestMapping("/api/admin/v1/public-holidays")
class AdminPublicHolidayController(
    private val adminPublicHolidayService: AdminPublicHolidayService
) {
    @GetMapping
    fun getPublicHolidayList(): List<AdminPublicHolidayResponse> {
        return adminPublicHolidayService.getPublicHolidayList()
    }

    @PostMapping
    fun createPublicHoliday(
        @RequestBody request: AdminPublicHolidayUpsertRequest
    ): AdminPublicHolidayResponse {
        return adminPublicHolidayService.createPublicHoliday(request)
    }

    @PutMapping("/{holidayId}")
    fun updatePublicHoliday(
        @PathVariable holidayId: Int,
        @RequestBody request: AdminPublicHolidayUpsertRequest
    ): AdminPublicHolidayResponse {
        return adminPublicHolidayService.updatePublicHoliday(holidayId, request)
    }

    @DeleteMapping("/{holidayId}")
    fun deletePublicHoliday(
        @PathVariable holidayId: Int
    ) {
        adminPublicHolidayService.deletePublicHoliday(holidayId)
    }
}
