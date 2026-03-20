package com.bigong.oguri.controller

import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.dto.PlaceDetailResponse
import com.bigong.oguri.service.DestinationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Destination API", description = "여행지 상세 및 관련 정보 API")
@RestController
@RequestMapping("/api/v1/destinations")
class DestinationController(
    private val destinationService: DestinationService
) {
    @Operation(
        summary = "장소 상세 조회",
        description = "특정 여행지의 상세 정보, 이미지, 장소별 액티비티(썸네일 URL, 제목, 설명, 링크), 관련 장소 등을 반환합니다."
    )
    @GetMapping("/{id}")
    fun getDestinationDetail(
        @PathVariable id: Int,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(defaultValue = "대한민국") userCountry: String,
        request: HttpServletRequest
    ): PlaceDetailResponse {
        return destinationService.getDestinationDetail(id, startDate, endDate, userCountry, request.resolveMemberId())
    }
}
