package com.bigong.oguri.controller

import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.dto.response.HomeMonthlyTopPeriodResponse
import com.bigong.oguri.dto.response.HomeRecommendPeriodResponse
import com.bigong.oguri.dto.response.HomeWeeklyTopResponse
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
        summary = "홈 화면 맞춤 추천 조회",
        description = "최적의 연차 사용 기간(Top 3)과 추천 여행지 목록을 반환합니다. advertisements는 places 순서에서 처음 발견되는 플랫폼별 링크를 포함합니다.",
    )
    @GetMapping
    fun getHome(
        @RequestParam(defaultValue = "대한민국") userCountry: String,
        request: HttpServletRequest,
    ): List<HomeRecommendPeriodResponse> = homeService.getHomeData(userCountry, request.resolveMemberId())

    @Operation(
        summary = "이번 주 인기 여행지 조회",
        description = "최근 7일간 사용자들이 가장 많이 저장(찜)한 상위 5개의 여행지를 반환합니다. 동점일 경우 이름 가나다순으로 정렬됩니다.",
    )
    @GetMapping("/weekly-top")
    fun getWeeklyTopPlaces(): HomeWeeklyTopResponse = homeService.getWeeklyTopPlaces()

    @Operation(
        summary = "이번 달 인기 추천 기간 조회",
        description = "이번 달 사용자들이 가장 많이 저장(찜)한 상위 3개의 추천 기간을 반환합니다. 동점일 경우 시작일이 빠른 순으로 정렬됩니다.",
    )
    @GetMapping("/monthly-top-periods")
    fun getMonthlyTopPeriods(): List<HomeMonthlyTopPeriodResponse> = homeService.getMonthlyTopPeriods()

    @Operation(
        summary = "검색어 기록",
        description = "사용자가 입력한 검색어를 서버에 기록합니다. 향후 인기 검색어 산출에 사용됩니다.",
    )
    @PostMapping("/search-logs")
    fun logSearchTerm(
        @RequestParam query: String,
        request: HttpServletRequest,
    ) {
        homeService.logSearchTerm(request.resolveMemberId(), query)
    }

    @Operation(
        summary = "인기 검색어 조회",
        description = "최근 7일간 사용자들이 가장 많이 검색한 상위 7개의 검색어를 반환합니다. 동점일 경우 가장 최근에 검색된 단어가 우선됩니다.",
    )
    @GetMapping("/trending-searches")
    fun getTrendingSearchTerms(): List<String> = homeService.getTrendingSearchTerms()
}
