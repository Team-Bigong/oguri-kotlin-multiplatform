package com.bigong.oguri.controller

import com.bigong.oguri.dto.RecommendPeriodResponse
import com.bigong.oguri.service.HomeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Home API", description = "홈 화면 및 추천 관련 API")
@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val homeService: HomeService
) {
    @Operation(summary = "홈 화면 조회", description = "최적의 연차 사용 기간과 추천 여행지 목록을 반환합니다.")
    @GetMapping
    fun getHome(
        @RequestParam(defaultValue = "대한민국") userCountry: String,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") userId: String
    ): List<RecommendPeriodResponse> {
        return homeService.getHomeData(userCountry, userId)
    }
}
