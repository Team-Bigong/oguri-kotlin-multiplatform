package com.bigong.oguri.controller

import com.bigong.oguri.dto.RecommendPeriodResponse
import com.bigong.oguri.service.HomeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val homeService: HomeService
) {
    @GetMapping
    fun getHome(
        @RequestParam(defaultValue = "3") dayOffCount: Int
    ): List<RecommendPeriodResponse> {
        return homeService.getHomeData(dayOffCount)
    }
}
