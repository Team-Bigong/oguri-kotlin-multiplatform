package com.bigong.oguri.controller

import com.bigong.oguri.dto.SaveRecommendationRequest
import com.bigong.oguri.service.SavedRecommendationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/saved-recommendations")
class SavedRecommendationController(
    private val savedRecommendationService: SavedRecommendationService
) {
    /**
     * 연휴 저장 API
     */
    @PostMapping
    fun save(
        @RequestBody request: SaveRecommendationRequest,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") userId: String
    ) {
        savedRecommendationService.save(request, userId)
    }

    /**
     * 연휴 저장 취소(삭제) API
     */
    @DeleteMapping
    fun delete(
        @RequestBody request: SaveRecommendationRequest,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") userId: String
    ) {
        savedRecommendationService.delete(request, userId)
    }
}
