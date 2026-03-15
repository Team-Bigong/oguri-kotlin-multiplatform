package com.bigong.oguri.controller

import com.bigong.oguri.dto.PlaceDetailResponse
import com.bigong.oguri.service.DestinationService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/destinations")
class DestinationController(
    private val destinationService: DestinationService
) {
    /**
     * 장소 상세 정보 조회 API
     * @param id 장소 ID
     * @param startDate 당시 추천받았던 휴가 시작일 (관련 장소 계산용)
     * @param endDate 당시 추천받았던 휴가 종료일 (관련 장소 계산용)
     */
    @GetMapping("/{id}")
    fun getDestinationDetail(
        @PathVariable id: Int,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(defaultValue = "대한민국") userCountry: String,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ): PlaceDetailResponse {
        return destinationService.getDestinationDetail(id, startDate, endDate, userCountry, memberId)
    }
}
