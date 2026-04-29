package com.bigong.oguri.controller

import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.dto.response.HomeRecommendPeriodResponse
import com.bigong.oguri.service.HomeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*

@Tag(name = "Home API", description = "홈 화면 및 추천 관련 API")
@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val homeService: HomeService,
) {
    @Operation(
        summary = "홈 화면 조회",
        description = "최적의 연차 사용 기간과 추천 여행지 목록을 반환합니다. advertisements는 places 순서에서 처음 발견되는 agoda/klook 액티비티 링크와 skyscanner(flightUrl) 링크만 포함하며, 없는 플랫폼은 제외됩니다.",
    )
    @GetMapping
    fun getHome(
        @RequestParam(defaultValue = "대한민국") userCountry: String,
        request: HttpServletRequest,
    ): List<HomeRecommendPeriodResponse> = homeService.getHomeData(userCountry, request.resolveMemberId())
}
